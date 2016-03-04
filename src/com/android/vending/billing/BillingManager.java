package com.android.vending.billing;

import java.util.ArrayList;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.Response;

/**
 * This class manages a ServiceConnection to the Google Play Store
 * for the inapp billing. It implements the Singleton design pattern.
 * It notifies the observer when the billing finished.
 * @author Guido
 * @see Observable 
 */
public class BillingManager extends Observable {
	
	/**
	 * The inapp billing service
	 */
	private IInAppBillingService service;
	/**
	 * Connection to the Play Store
	 */
	private ServiceConnection serviceConn;
	/**
	 * the instance of this class for the Singleton design pattern
	 */
	private static BillingManager instance;
	/**
	 * Instance of the calling Activity
	 */
	private Activity activity;
	/**
	 * The item (Route) to purchase
	 */
	private String itemToPurchase;
	/**
	 * Empty private constructor for the singleton design pattern
	 */
	private BillingManager(){}
	/**
	 * Returns the reference of this class.
	 * @return The reference of this class.
	 */
	public static BillingManager getReference(){
		if(instance == null)
			instance = new BillingManager();
		return instance;
	}

	/**
	 * Establishes a new ServiceConnection and overrides its methods in an anonymous inner class. 
	 */
	public void establish(){
		serviceConn = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				service = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder binderService) {
				Log.i("SERVICE", "CONNECTED");
				service = IInAppBillingService.Stub.asInterface(binderService);
				setChanged();
				notifyObservers(new Response(-1, null));
			}
		};
	}

	/**
	 * Sets the calling Activities instance.
	 * @param act The calling Activities instance.
	 */
	public void setActivity(Activity act){
		activity = act;
	}

	/**
	 * Returns the ServiceConnection.
	 * @return The ServiceConnection.
	 */
	public ServiceConnection getServiceConn() {
		return serviceConn;
	}

	/**
	 * Returns the IInAppBillingService.
	 * @return The IInAppBillingService.
	 */
	public IInAppBillingService getService() {
		return service;
	}

	/**
	 * Executes the request to the Google Play Store by passing
	 * the assigned Response object to the AsyncTask handling the 
	 * communication.
	 * @param req The assigned Response object.
	 */
	public void executeRequest(Response req){
		new BillingManager.BillingRequest().execute(req);
	}

	/**
	 * An AsyncTask handling the communication with the Google Play Store.
	 * @author Guido
	 * @see AsyncTask Communication is conducted in an extra Thread.
	 */
	private class BillingRequest extends AsyncTask<Response, Void, Response>{

		/**
		 * The computation which shall be processed in the background.
		 * Puts the received response into Bundle which is part of a new Response object.
		 * @param params The params that have been passed to execute().
		 * @return The new Response object containing the Bundle.
		 */
		@Override
		protected Response doInBackground(Response... params) {
			Response resp = new Response();
			switch(params[0].getId()){
			case Constants.REQ_PROD_DETAILS:
				resp.setId(Constants.REQ_PROD_DETAILS);
				ArrayList<String> productList = new ArrayList<String>();
				productList.add(params[0].getObject().toString());
				Bundle queryProducts = new Bundle();
				queryProducts.putStringArrayList("ITEM_ID_LIST", productList);
				Bundle productDetails = null;
				try {
					productDetails = service.getSkuDetails(3, activity.getPackageName(), "inapp", queryProducts);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				resp.setObject(productDetails);
				break;
			case Constants.REQ_PROD_LIST:
				resp.setId(Constants.REQ_PROD_LIST);
				Bundle ownedItems = null;
				try {
					ownedItems = service.getPurchases(3, activity.getPackageName(), "inapp", null);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				resp.setObject(ownedItems);
				break;
			case Constants.REQ_PROD_PURCHASE:
				resp.setId(Constants.REQ_PROD_PURCHASE);
				String purchaseID = params[0].getObject().toString();
				Bundle buyIntentBundle = null;
				try {
					buyIntentBundle = service.getBuyIntent(3, activity.getPackageName(), purchaseID, "inapp", "empty");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				resp.setObject(buyIntentBundle);
			}
			return resp;
		}
		
		/**
		 * Called when doInBackground finished. The parameter is the
		 * return value of doInBackground.
		 * @param response The return value of doInBackground.
		 */
		@Override
		protected void onPostExecute(Response response) {
			switch(response.getId()){
			case Constants.REQ_PROD_DETAILS:
				setChanged();
				notifyObservers(response);
				break;
			case Constants.REQ_PROD_LIST:
				boolean alreadyPurchased = false;
				Bundle ownedItems = (Bundle) response.getObject();
				int resp = ownedItems.getInt("RESPONSE_CODE");
				if (resp == 0) {
					ArrayList<String> ownedSkus =
							ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
					ArrayList<String>  purchaseDataList =
							ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
					for (int i = 0; i < purchaseDataList.size(); ++i) {
						String purchaseData = purchaseDataList.get(i);
						String sku = ownedSkus.get(i);
						Log.i("PURCHASED ITEMS", sku);
						try {
							JSONObject purchase = new JSONObject(purchaseData);
							Log.i("ITEM TO PURCHASE", purchase.getString("productId"));
							if(sku.equals(purchase.get("productId"))){
								int consume = service.consumePurchase(3, activity.getPackageName(), purchase.getString("purchaseToken"));
								Log.i("CONSUME", consume+"");
								if(consume==0){
									response.setId(Constants.REQ_PROD_CONSUMED);
									setChanged();
									notifyObservers(response);
									alreadyPurchased = true;
								}
								break;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						catch (RemoteException e) {
							e.printStackTrace();
						}
					} 
					if(!alreadyPurchased){
						Response r = new Response(Constants.REQ_PROD_PURCHASE, itemToPurchase);
						BillingManager.getReference().executeRequest(r);
					}
				}
				break;
			case Constants.REQ_PROD_PURCHASE:
				setChanged();
				notifyObservers(response);
				break;
			}

		}
	}
	
	/**
	 * Sets the given item.
	 * @param item The given item.
	 */
	public void setItemToPurchase(String item){
		itemToPurchase = item;
	}
}
