package de.thwildau.guido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;

/**
 * This Activity shows a lobby where the user can navigate to 
 * the programs functions. 
 * @author GUIDO
 * @version 2013-12-18
 * @see Activity
 */
public class Lobby extends ListActivity implements Observer {

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;

	/**
	 * Counts the number of times the back button was pressed.
	 */
	private static int backButtonCount = 0;

	/**
	 * Instance of this class
	 */
	private ListView lobbyListView;
	
	/**
	 * An ArrayList containing this Lists contents
	 */
	private ArrayList<HashMap<String, String>> listContent;

	/**
	 * Called when the Activity is first created. Sets the content view.
	 * @param savedInstanceState If the activity is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
	}

	/**
	 * Called when the Activity gets visible to the user. Initializes the components dependent of
	 * the user is in an active route or not.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		lobbyListView = getListView();
		lobbyListView.setBackgroundResource(R.drawable.lobby_back);
		listContent = new ArrayList<HashMap<String, String>>();
		Log.i("ROUTEID PREF", PreferenceData.getActiveRoute(this) + "");
		if(PreferenceData.getActiveRoute(this) == null)
			prepareLobbyItems();
		else 
			prepareLobbyActiveRouteItems();
		lobbyListView.setAdapter(new SimpleAdapter(this, listContent, R.layout.activity_lobby_listview_item, new String[] {"item"}, new int[] {R.id.lobby_routesoverview_textview}));
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Shows a dummy settings button and a logout button which logs the user out.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(PreferenceData.getActiveRoute(this) == null)
			getMenuInflater().inflate(R.menu.app_menu_logout, menu);
		return true;
	}

	/**
	 * Called when a menu item was selected. If the logout item was selected,
	 * the user will be logged out.
	 * @param item The selected menu item.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			DatabaseInteractor.logout(this, PreferenceData.getUserId(this));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Executed when the back button is pressed.
	 * If the user presses the back button twice the application is minimized.
	 */
	@Override
	public void onBackPressed() {
		if(backButtonCount >= 1) {
			backButtonCount = 0;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		else {
			Toast.makeText(this, "Noch einmal drücken, um Anwendung zu schließen", Toast.LENGTH_SHORT).show();
			backButtonCount++;
		}
	}

	/**
	 * Prepares the Lobby when the user is not in an active route.
	 */
	public void prepareLobbyItems() {
		HashMap<String, String> content = new HashMap<String, String>();
		content.put("item", "Routenübersicht");
		listContent.add(content);
		content = new HashMap<String, String>();
		content.put("item", "Route erstellen");
		listContent.add(content);
		content = new HashMap<String, String>();
		content.put("item", "Nachrichten");
		listContent.add(content);
		content = new HashMap<String, String>();
		content.put("item", "Kontakte");
		listContent.add(content);
		content = new HashMap<String, String>();
		content.put("item", "Profil");
		listContent.add(content);
		lobbyListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
				switch (position) {
				case 0:
					openRoutesOverview();
					break;
				case 1:
					createRoute();
					break;
				case 2:
					showMessages();
					break;
				case 3:
					showContacts();
					break;
				case 4:
					showProfile();
					break;
				}
			}
		});
	}

	/**
	 * Prepares the when the user is in an active route.
	 */
	public void prepareLobbyActiveRouteItems() {
		HashMap<String, String> content = new HashMap<String, String>();
		content.put("item", "Laufende Route");
		listContent.add(content);
		content = new HashMap<String, String>();
		content.put("item", "Nachrichten");
		listContent.add(content);
		content = new HashMap<String, String>();
		content.put("item", "Kontakte");
		listContent.add(content);
		content = new HashMap<String, String>();
		content.put("item", "Profil");
		listContent.add(content);
		lobbyListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
				switch (position) {
				case 0:
					openActiveRoute();
					break;
				case 1:
					showMessages();
					break;
				case 2:
					showContacts();
					break;
				case 3:
					showProfile();
					break;
				}
			}
		});
	}

	/**
	 * Opens {@link RoutesOverview} via an intent.
	 */
	public void openRoutesOverview() {
		backButtonCount = 0;
		progress.show();
		DatabaseInteractor.getRoutes(this, PreferenceData.getUserId(this));
	}

	/**
	 * Opens the {@link RouteCreator}
	 */
	public void createRoute() {
		backButtonCount = 0;
		Intent intent = new Intent(this, RouteCreator.class);
		startActivity(intent);
	}
	
	/**
	 * Opens the {@link MessageOverview}.
	 */
	public void showMessages() {
		backButtonCount = 0;
		Intent intent = new Intent(this, MessageOverview.class);
		this.startActivity(intent);
	}
	
	/**
	 * Opens {@link ProfileInformation}
	 */
	public void showProfile() {
		backButtonCount = 0;
		Intent intent = new Intent(this, ProfileInformation.class);
		this.startActivity(intent);
	}

	/**
	 * Opens {@link ContactList}
	 */
	public void showContacts() {
		backButtonCount = 0;
		Intent intent = new Intent(this, ContactList.class);
		this.startActivity(intent);
	}
	
	/**
	 * Opens {@link ActiveRoute}
	 */
	public void openActiveRoute() {
		backButtonCount = 0;
		Intent intent = new Intent(this, ActiveRoute.class);
		this.startActivity(intent);
	}

	/**
	 * This method is called whenever the observed object {@link HttpConnector}
	 * is changed via its notify() method when the data exchange is finished. 
	 * It hides the progress bar and passes the response gotten by the server
	 * to {@link ResponseHandler#handleResponse(Activity, String)}.
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
		Response resp = ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
		switch(resp.getId()) {
		case Constants.RESP_LIST_ROUTES:
			// pass the Routes to RoutesOverview and start the Activity
			Intent intent = new Intent(this, RoutesOverview.class);
			intent.putExtra("routes", (ArrayList<Route>) resp.getObject());
			startActivity(intent);
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
}