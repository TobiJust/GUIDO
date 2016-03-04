package de.thwildau.guido;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.model.User;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.GuidoLocationListener;
import de.thwildau.guido.util.GuidoMapView;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.LocationBroadcastReceiver;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;

/**
 * This class displays an active route with its participants and the guido and
 * the navigation hints provided by OSM. It observes the {@link HttpConnector} to be informed
 * when data exchange finished.
 * @author Guido
 * @see Activity
 * @see Observer
 */
public class ActiveRoute extends Activity implements Observer {

	/**
	 * An instance of the OSM map
	 */
	private MapView map;
	/**
	 * An instance of the custom map
	 */
	private GuidoMapView guidoMap;
	/**
	 * Static instance of the location manager to get the location
	 */
	private static LocationManager locationManager;
	/**
	 * Flag to determine the autofocus, default is true
	 */
	private boolean focus = true;
	/**
	 * An Instance of the displayed Route
	 */
	private static Route route;
	/**
	 * Custom location listener
	 */
	private static GuidoLocationListener locationListener;
	/**
	 * Instance of this Activity
	 */
	private static Activity act;
	/**
	 * Instance of the LocationBroadcastReceiver
	 */
	private static LocationBroadcastReceiver loc;
	/**
	 * An ArrayList containing all participants outside of the guide sphere
	 */
	private ArrayList<User> missingParticipators = new ArrayList<User>();
	/**
	 * Determines if the ActionBar icon of missing participants is shown
	 */
	private boolean notificationSetup = false;
	/**
	 * The item displaying the auto focus
	 */
	private MenuItem focusItem;
	/**
	 * Dialog which is opened when the missing participants icon is pressed
	 */
	private ParticipatorsDialogFragment partDialog;
	/**
	 * The component name receiver
	 */
	private ComponentName receiver;

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;

	/**
	 * Called when the Activity is created. Inflates the layout and initializes
	 * variables. 
	 * @param savedInstanceState   If the activity is being re-initialized 
	 * after previously being shut down then this Bundle contains the data 
	 * it most recently supplied, otherwise its null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_active_route);
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		act = this;
		route = (Route) getIntent().getSerializableExtra("route");
	}

	/**
	 * Called when an Activity becomes visible.  
	 */
	@Override
	protected void onResume() {
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		super.onResume();
		// Route is null if the Activity was started from the Lobby. In this case the user
		// already is in an active Route and has its id in the SharedPreferences.
		if(route == null) {
			progress.show();
			DatabaseInteractor.getRouteDetails(this, PreferenceData.getUserId(this), PreferenceData.getActiveRoute(this));
		}
		else if(route != null) {
			PreferenceData.setActiveRoute(this, route.getId());
			prepareMap();
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
		getMenuInflater().inflate(R.menu.active_route, menu);
		focusItem = menu.findItem(R.id.active_route_action_position_focus);
		View count = menu.findItem(R.id.active_route_action_notification).getActionView();
		MenuItem countItem = menu.getItem(2);
		if(focus) 
			focusItem.setIcon(R.drawable.ic_action_location_found);
		else 
			focusItem.setIcon(R.drawable.ic_action_location_off);
		if(route != null && PreferenceData.getUserId(this).equals(route.getCreator().getId())){
			Button notifCount = (Button) count.findViewById(R.id.notif_count);
			notifCount.setText(String.valueOf(0));
			count.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {   
					partDialog = new ParticipatorsDialogFragment();
					Bundle args = new Bundle();
					ArrayList<String> partNames = new ArrayList<String>();
					for(User u:missingParticipators){
						partNames.add(u.getName());
					}
					args.putSerializable("users", missingParticipators);
					args.putStringArrayList("missing_participators",partNames);
					partDialog.setArguments(args);
					partDialog.show(getFragmentManager(), "Teilnehmer außer Reichtweite");
				}
			});
		}
		else if(route != null && !PreferenceData.getUserId(this).equals(route.getCreator().getId())) {
			countItem.setVisible(false);
		}
		return super.onCreateOptionsMenu(menu);
		//		return true;
	}

	/**
	 * Called when a menu item was selected. If the logout item was selected,
	 * the user will be logged out.
	 * @param item The selected menu item.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.active_route_action_leave:
			Builder alertLeave = new AlertDialog.Builder(act);
			alertLeave.setTitle("Warnung");
			alertLeave.setMessage("Möchten Sie die Route wirklich verlassen?");
			alertLeave.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DatabaseInteractor.leaveRoute(act, PreferenceData.getUserId(act), route.getId());
					dialog.dismiss();
				}
			});
			alertLeave.setNegativeButton("Abbrechen", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog alertDiagLeave = alertLeave.create();
			alertDiagLeave.show();
			break;
		case R.id.active_route_action_end:
			Builder alertEnd = new AlertDialog.Builder(act);
			alertEnd.setTitle("Warnung");
			alertEnd.setMessage("Möchten Sie die Route wirklich beenden?");
			alertEnd.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					progress.show();
					DatabaseInteractor.endRoute(act, PreferenceData.getUserId(act), route.getId());
					dialog.dismiss();
				}
			});
			alertEnd.setNegativeButton("Abbrechen", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog alertDiagEnd = alertEnd.create();
			alertDiagEnd.show();
			break;
		case R.id.active_route_action_position_focus:
			setPositionFocus(item);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Called when the back button is pressed and opens the Lobby.
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, Lobby.class);
		startActivity(intent);
	}

	/**
	 * Called when the observed class notifies. Evaluates the Response object.
	 * @param observable The observed class.
	 * @param data The data passed to notifyObservers(Object).  
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(progress != null)
			progress.hide();
		if(DatabaseInteractor.getResponse()==null){
			Toast.makeText(this, "Netzwerkprobleme...", Toast.LENGTH_LONG).show();
			return;
		}
		Response resp = ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
		switch(resp.getId()) {
		case Constants.RESP_GET_POS_ALL:
			setMissingParticipators((ArrayList<User>) resp.getObject());
			updatePos((ArrayList<User>) resp.getObject());
			System.out.println("ROUTECREATOR" + route.getCreator().getId());
			System.out.println("PREF" + PreferenceData.getUserId(this));
			break;
		case Constants.RESP_GET_POS_GUIDO:
			updatePosGuido((ArrayList<User>) resp.getObject());
			if(!notificationSetup)
				invalidateOptionsMenu();
			break;
		case Constants.RESP_END_ROUTE:
			leaveRoute();
			break;
		case Constants.RESP_LEAVE_ROUTE:
			leaveRoute();
			break;
		case Constants.RESP_GET_ROUTE_DETAILS:
			route = (Route) resp.getObject();
			PreferenceData.setActiveRoute(this, route.getId());
			prepareMap();
			break;
		case Constants.RESP_GET_CONTACTS:
			MessageCreatorFragment msgFrag = partDialog.getMessageCreatorFragment();
			msgFrag.updateDropDown((List<User>)resp.getObject());
			break;
		case Constants.RESP_ERROR:
			Builder alert = new AlertDialog.Builder(this);
			final GuidoError err = (GuidoError)resp.getObject();
			alert.setTitle("Error #"+err.getErrorCode());
			alert.setMessage(err.getMessage());
			alert.setPositiveButton("Ok", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(err.getErrorCode()){
					case Constants.ERR_ROUTE_NOT_STARTED:
					case Constants.ERR_ROUTE_NOT_EXISTING:
					case Constants.ERR_ROUTE_NOT_PARTICIPATING:
					case Constants.ERR_ROUTE_NOT_PARTICIPATING_ANY:
						PreferenceData.setActiveRoute(act, null);
						leaveRoute();
						break;
					}
					dialog.dismiss();
				}
			});
			AlertDialog alertDiag = alert.create();
			try{
				alertDiag.show();
			}
			catch (Exception e) {}
			break;
		}
	}

	/**
	 * Initializes the map view.
	 */
	public void prepareMap() {
		map = (MapView)findViewById(R.id.activeMap);
		if(guidoMap == null) {
			guidoMap = new GuidoMapView(this, map, route, true, false, true);
			guidoMap.setFocus(focus);
		}

		locationListener = GuidoLocationListener.getReference(); 
		locationListener.setAct(this);
		locationManager = locationListener.getLm();
		locationListener.setGuidoMap(guidoMap);
		locationListener.setLm(locationManager);
		Log.i("LOCATIONREFERENCES", locationManager + "");
		Log.i("LOCATIONREFERENCES", locationListener + "");
		//checks if GPS is activated
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//new alert Dialog for activating GPS
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			builder.setTitle("GPS ist nicht aktiviert");  // GPS not found
			builder.setMessage("Wollen Sie die GPS-Ortung aktivieren?"); // Want to enable?
			builder.setIcon(R.drawable.needle_mini);
			builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {
					//changes intent to android location settings to enable GPS
					startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));  
				}
			});
			builder.create().show();
		} 
		locationListener.endListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location != null) {
			DatabaseInteractor.sendPosition(this, PreferenceData.getUserId(this), location.getLatitude(), location.getLongitude());
			guidoMap.getMapController().animateTo(new GeoPoint(location));
		}
		loc = new LocationBroadcastReceiver();
		loc.cancelAlarm(this);
		loc.setAlarm(this);
		PackageManager pm = this.getPackageManager();
		if(receiver==null)
			receiver = new ComponentName(this, LocationBroadcastReceiver.class);
		if (PackageManager.COMPONENT_ENABLED_STATE_ENABLED != pm.getComponentEnabledSetting(receiver)){
			Log.i("RECEIVER", "RECEIVER SET ENABLED");
			pm.setComponentEnabledSetting(receiver,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
		}
	}

	/**
	 * Sets the ActionBar button for auto focus.
	 * @param item
	 */
	public void setPositionFocus(MenuItem item) {
		focus = !focus;
		if(focus) {
			item.setIcon(R.drawable.ic_action_location_found);
			Toast.makeText(this, "Autofokus an", Toast.LENGTH_LONG).show();
		}
		else {
			item.setIcon(R.drawable.ic_action_location_off);
			Toast.makeText(this, "Autofokus aus", Toast.LENGTH_LONG).show();
		}
		guidoMap.setFocus(focus);
	}

	/**
	 * Starts a database interaction to get the participants positions.
	 */
	public static void getPosAll() {
		if(PreferenceData.getUserId(act).equals(route.getCreator().getId())) {
			DatabaseInteractor.getPosAll(act, PreferenceData.getUserId(act), route.getId());
		}
		else {
			DatabaseInteractor.getPosGuido(act, PreferenceData.getUserId(act), route.getId());
		}
	}

	/**
	 * Updates the map with the guides and the participants position for the guido.
	 * @param parts The participants.
	 */
	public void updatePos(ArrayList<User> parts) {
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		User guido = route.getCreator();
		guido.setLat(location.getLatitude());
		guido.setLng(location.getLongitude());
		route.setParticipators(parts);
		guidoMap.updateUI(route);
	}

	/**
	 * Updates the guides position for the participants.
	 * @param guidos The guide.
	 */
	public void updatePosGuido(ArrayList<User> guidos){
		if(guidos.size()>0){
			route.setCreator(guidos.get(0));
			guidoMap.updateUI(route);
		}
	}

	/**
	 * Cancels the alarm to stop polling the position.
	 */
	public static void cancelAlarm(){
		PreferenceData.setActiveRoute(act, null);
		locationListener.endListener();
		loc.cancelAlarm(act);
	}

	/**
	 * Leave the Route. Cancels the alarm and returns to the lobby.
	 */
	public static void leaveRoute(){
		cancelAlarm();
		Intent intent = new Intent(act, Lobby.class);
		act.startActivity(intent);
		act.finish();
	}
	
	/**
	 * Sets the ActionBar icon for missing participants.
	 * @param partList The missing participants.
	 */
	private void setMissingParticipators(ArrayList<User> partList){
		Location guidoLocation = new Location("guidoLocation");
		if(route==null)
			return;
		guidoLocation.setLatitude(route.getCreator().getLat());
		guidoLocation.setLongitude(route.getCreator().getLng());
		missingParticipators = new ArrayList<User>();
		Log.i("MISSINGPARTICIPATORS", partList.size()+"");
		for(User u:partList){
			Location userLocation = new Location("userLocation");
			userLocation.setLatitude(u.getLat());
			userLocation.setLongitude(u.getLng());
			double distance = guidoLocation.distanceTo(userLocation);
			Log.i("MISSINGPARTICIPATORS", distance+"");
			Log.i("MISSINGPARTICIPATORS", distance+">"+Constants.ACTIVE_ROUTE_MAX_DISTANCE+"?: "+(distance>Constants.ACTIVE_ROUTE_MAX_DISTANCE)+"");
			if(distance>Constants.ACTIVE_ROUTE_MAX_DISTANCE){
				Log.i("MISSINGPARTICIPATORS", "adding user");
				missingParticipators.add(u);
			}
		}
		invalidateOptionsMenu();
	}

	/**
	 * Prepares the options menu and the ActionBar icons.
	 * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.i("PREPAREOPTIONS", "setting notification count to"+missingParticipators.size());
		View count = menu.findItem(R.id.active_route_action_notification).getActionView();
		MenuItem countItem = menu.getItem(2);
		Button notifCount = (Button) count.findViewById(R.id.notif_count);
		notifCount.setText(String.valueOf(missingParticipators.size()));
		if(route != null && PreferenceData.getUserId(this).equals(route.getCreator().getId())&& !notificationSetup){
			count.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {   
					ParticipatorsDialogFragment partDialog = new ParticipatorsDialogFragment();
					Bundle args = new Bundle();
					ArrayList<String> partNames = new ArrayList<String>();
					for(User u:missingParticipators){
						Log.i("ADDING PARTICIPATOR", u.getName());
						partNames.add(u.getName());
					}
					args.putStringArrayList("missing_participators",partNames);
					args.putSerializable("users", missingParticipators);
					partDialog.setArguments(args);
					partDialog.show(getFragmentManager(), "Teilnehmer außer Reichtweite");
				}
			});
			notificationSetup = true;
		}
		else if(route != null && !PreferenceData.getUserId(this).equals(route.getCreator().getId())) {
			Log.i("PARTICIPATOR", "REMOVING NOTIFICATION");
			Log.i("PARTICIPATOR", countItem.toString());
			countItem.setVisible(false);
			notificationSetup = true;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Called when the Activity is stopped. 
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
