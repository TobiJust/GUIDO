package de.thwildau.guido;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.vending.billing.BillingManager;

import de.thwildau.guido.model.POI;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.routing.AddressDialogFragment;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.GuidoMapView;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;

/**
 * This class shows a map where POI can be added to create a new Route.
 * @author Guido
 * @see Activity
 * @see Observer
 */
public class RouteCreatorMap extends Activity implements Observer {

	/**
	 * Dialog to add a new POI by address
	 */
	private AddressDialogFragment diagFrag;
	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;
	/**
	 * The Route which will be created
	 */
	private Route r;
	/**
	 * Instance of the OSM MapView
	 */
	private MapView mapView;
	/**
	 * Instance of a custom MapView
	 */
	private GuidoMapView guidoMap;
	/**
	 * Product ID of the Route to purchase in the Play Store
	 */
	private String productID;
	/**
	 * This class' context
	 */
	private Context context = this;

	/**
	 * First Method called after activity start.
	 * Build the GUI and inherits the map skills.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_creator_map);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Shows a dummy settings button and a logout button which logs the user out.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_menu_logout_address, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Called when a menu item was selected. 
	 * @param item The selected menu item.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			DatabaseInteractor.logout(this, PreferenceData.getUserId(this));
			break;
		case R.id.action_prev:
			prev();
			break;
		case R.id.action_address:
			createPOIWithAdress();
			break;
		case R.id.action_finish:
			if(guidoMap.getPOIs().size()<2){
				Toast.makeText(this, "Es m¸ssen mindestens zwei Stationen angegeben werden.", Toast.LENGTH_LONG).show();
				break;
			}
			BillingManager bm = BillingManager.getReference();
			bm.establish();
			bm.deleteObservers();
			bm.addObserver(this);
			bm.setActivity(this);
			if(r.getMaxPart()>10 && r.getMaxPart()<=20){
				productID = "route_s";
				bm.setItemToPurchase(productID);
				this.getApplicationContext().bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), 
						bm.getServiceConn(), Context.BIND_AUTO_CREATE);
			}
			else if(r.getMaxPart()>20 && r.getMaxPart()<=50){
				productID = "route_m";
				bm.setItemToPurchase(productID);
				this.getApplicationContext().bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), 
						bm.getServiceConn(), Context.BIND_AUTO_CREATE);
			}
			else if(r.getMaxPart()>50){
				productID = "route_l";
				bm.setItemToPurchase(productID);
				this.getApplicationContext().bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), 
						bm.getServiceConn(), Context.BIND_AUTO_CREATE);
			}
			else{
				onFinish();
			}
			break;
		case R.id.action_undo:
			guidoMap.deleteLastPOI();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Executed when the back button is pressed.
	 */
	@Override
	public void onBackPressed() {
		prev();
	}

	/**
	 * Dialog to create a POI by address
	 */
	private void createPOIWithAdress() {
		diagFrag = new AddressDialogFragment();
		diagFrag.setPOI(new POI());
		diagFrag.show(getFragmentManager(), "AddressDialog");	
	}

	/**
	 * Creates a POI by touch event.
	 * @param uiList The addresses.
	 */
	public void createPOIWithTouch(List<Address> uiList){
		diagFrag = new AddressDialogFragment();
		diagFrag.setPOI(new POI());
		diagFrag.show(getFragmentManager(), "AddressDialog");
		diagFrag.updateAddressView(uiList);
	}

	/**
	 * Update the ListView with a new address list
	 * @param uiList The List to show on the ListView.
	 */
	public void updateUI(List<Address> uiList) {
		diagFrag.updateDropDown(uiList);
	}

	/**
	 * Saves all changes and goes to step 1 of the Route creation.
	 */
	public void prev() {
		r.setPois(guidoMap.getPOIs());
		Log.i("PREV", "SETTING POIS "+r.getPois().size());
		Intent intent = new Intent(this, RouteCreator.class);
		intent.putExtra("route", r);
		intent.putExtra("password", getIntent().getStringExtra("password"));
		startActivity(intent);
		finish();
	}

	/**
	 * This method is called whenever the observed object {@link HttpConnector}
	 * is changed via its notify() method when the data exchange is finished. 
	 * It passes the response gotten by the server to {@link ResponseHandler#handleResponse(Activity, String)}.
	 * @param oberservable The observable object.
	 * @param data An argument passed to the notifyObservers method.
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		if(progress != null)
			progress.hide();
		if(DatabaseInteractor.getResponse()==null){
			Toast.makeText(this, "Netzwerkprobleme...", Toast.LENGTH_LONG).show();
			return;
		}
		if(arg1==null){
			Response resp = ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
			switch(resp.getId()) {
			case Constants.RESP_CREATE_ROUTE:
				Intent intent = new Intent(this, Lobby.class);
				startActivity(intent);
				Toast.makeText(this, resp.getObject().toString(), Toast.LENGTH_LONG).show();
				break;
			case Constants.RESP_ERROR:
				Builder alert = new AlertDialog.Builder(this);
				GuidoError err = (GuidoError)resp.getObject();
				alert.setTitle("Error #"+err.getErrorCode());
				alert.setMessage(err.getMessage());
				alert.setPositiveButton("Ok", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog alertDiag = alert.create();
				alertDiag.show();
				break;
			}
		}
		else{
			Response resp = (Response)arg1;
			switch(resp.getId()){
			case Constants.REQ_PROD_DETAILS:
				guidoMap.setActivity(this);
				guidoMap.onPurchase((Bundle)resp.getObject());
				break;
			case Constants.REQ_PROD_PURCHASE:
				PendingIntent pi = ((Bundle)resp.getObject()).getParcelable("BUY_INTENT");
				try {
					startIntentSenderForResult(pi.getIntentSender(),
							1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
				} catch (SendIntentException e) {
					e.printStackTrace();
				}
				break;
			case Constants.REQ_PROD_CONSUMED:
				finishPurchase(getIntent().getStringExtra("password"));
				break;
			case -1:
				Log.i("UPDATE", "UPDATE");
				Response response = new Response();
				response.setId(Constants.REQ_PROD_DETAILS);
				response.setObject(productID);
				BillingManager.getReference().executeRequest(response);
				break;
			}
		}
	}

	/**
	 * Called when the Activity gets visible to the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		
		r = (Route) getIntent().getSerializableExtra("route");

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//checks if GPS is activated
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//new alert Dialog for activating GPS
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(false);
			builder.setTitle("GPS ist nicht aktiviert!");  // GPS not found
			builder.setMessage("Wollen sie die GPS-Ortung aktivieren?"); // Want to enable?
			builder.setIcon(R.drawable.needle_mini);
			builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {
					//changes intent to android location settings to enable GPS
					startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));  
				}
			});

			builder.create().show();
		} 
		mapView = (MapView) findViewById(R.id.activeMap);
		guidoMap = new GuidoMapView(this, mapView, r, false, true, true);
		//gets the last known position 
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location != null) {
			//focus map on current position
			guidoMap.getMapController().animateTo(new GeoPoint(location));
		}
	}

	/**
	 * Returns the custom MapView.
	 * @return The custom MapView.
	 */
	public GuidoMapView getMapView() {
		return guidoMap;
	}
	
	/**
	 * Called when the Activity stops.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		if(progress != null){
			progress.dismiss();
			progress = null;
		}
	}

	/**
	 * Called when an Intent was started by startActivityForResult(Intent).
	 * @param requestCode  The integer request code originally supplied to startActivityForResult(), 
	 * allowing you to identify who this result came from. 
	 * @param resultCode  The integer result code returned by the child activity through its setResult(). 
	 * @param data  An Intent, which can return result data to the caller 
	 * (various data can be attached to Intent "extras"). 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1001 && resultCode == RESULT_OK){
			int consume;
			try {
				String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
				JSONObject purchase = new JSONObject(purchaseData);
				consume = BillingManager.getReference().getService().consumePurchase(3, getPackageName(), purchase.getString("purchaseToken"));
				if(consume==0){
					finishPurchase(getIntent().getStringExtra("password"));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when the Activity is destroyed. Unbinds the billing services 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(BillingManager.getReference().getServiceConn()!=null){
			try{
				unbindService(BillingManager.getReference().getServiceConn());
			}catch(Exception e){}
		}
	}
	
	/**
	 * Creates a Dialog to finish the Route creation.
	 */
	public void onFinish() {
		AlertDialog.Builder finishDialog = new AlertDialog.Builder(this);
		finishDialog.setTitle("Route Abschlieﬂen?");
		finishDialog.setIcon(R.drawable.ic_launcher);

		finishDialog.setPositiveButton("Abschlieﬂen", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
					r.setPois(guidoMap.getPOIs());
					progress.show();
					DatabaseInteractor.createRoute((Activity)context, PreferenceData.getUserId(context), r, getIntent().getStringExtra("password"));
			}
		});

		finishDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		finishDialog.show();
	}
	
	/**
	 * Finishes the purchase and creates the Route.
	 * @param password The optional Routes password.
	 */
	public void finishPurchase(String password){
		r.setPois(guidoMap.getPOIs());
		DatabaseInteractor.createRoute(this, PreferenceData.getUserId(this), r, password);
	}

}
