package de.thwildau.guido;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.osmdroid.views.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.model.User;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.GuidoMapView;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;

/**
 * An Activity displaying the selected Routes details.
 * @author GUIDO
 * @version 2013-12-20
 * @see Activity
 */
public class RouteDetail extends Activity implements Observer {

	/**
	 * The Route which details are displayed
	 */
	private Route displayedRoute;

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;
	
	/**
	 * Called when the activity is first created. Sets the content view with layout xml
	 * and sets the Route which was put as extra to the intent.
	 * @param savedInstanceState If the activity is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_detail);
	}

	/**
	 * Called when the Activity gets visible to the user. Initializes the Activities components.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		displayedRoute = (Route) getIntent().getSerializableExtra("route");
		MapView map = (MapView)findViewById(R.id.route_detail_map);
		new GuidoMapView(this, map, displayedRoute, false, false, false);
		ArrayList<User> participators = displayedRoute.getParticipators();
		if(PreferenceData.getUserId(this).equals(displayedRoute.getCreator().getId())) {
			((Button)findViewById(R.id.start_route_button)).setVisibility(View.VISIBLE);
			((Button)findViewById(R.id.delete_route_button)).setVisibility(View.VISIBLE);
		}
		else {
			boolean participating = false;
			for(User part : participators) {
				if(part.getId().equals(PreferenceData.getUserId(this))) {
					((Button)findViewById(R.id.leave_route_button)).setVisibility(View.VISIBLE);
					((Button)findViewById(R.id.join_route_button)).setVisibility(View.GONE);
					participating = true;
				}
			}
			if(!participating) {
				((Button)findViewById(R.id.join_route_button)).setVisibility(View.VISIBLE);
				((Button)findViewById(R.id.leave_route_button)).setVisibility(View.GONE);
			}
		}
		setupData();
	}


	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Shows a dummy settings button and a logout button which logs the user out.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.route_detail, menu);
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
		case R.id.action_refreshDetails:
			progress.show();
			DatabaseInteractor.getRouteDetails(this, PreferenceData.getUserId(this), displayedRoute.getId());
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * Sets the Routes properties to the TextViews described in the layout xml.
	 */
	public void setupData() {
		TextView routeName = (TextView) findViewById(R.id.route_name_target);
		TextView routeDesc = (TextView) findViewById(R.id.route_desc_target);
//		TextView routeStart = (TextView) findViewById(R.id.route_start);
//		TextView routeEnd = (TextView) findViewById(R.id.route_end);
		TextView routeMaxPart = (TextView) findViewById(R.id.route_maxparticipators_target);
		TextView routeDate = (TextView) findViewById(R.id.route_date_target);
		TextView routeCategory = (TextView) findViewById(R.id.route_category_target);
		TextView routeTravelType = (TextView) findViewById(R.id.route_traveltype_target);

		routeName.setText(displayedRoute.getName());
		routeDesc.setText(displayedRoute.getDescription());
		//		routeStart.setText(displayedRoute.getStart().getName());
		//		routeEnd.setText(displayedRoute.getEnd().getName());
		routeMaxPart.setText(displayedRoute.getMaxPart()+"");
		routeDate.setText(displayedRoute.getDate().toString());
		routeCategory.setText(displayedRoute.getCategory());
		routeTravelType.setText(Constants.ROUTE_TRAVELTYPES[displayedRoute.getTravelType()-1]);
		
		TableLayout partTable = (TableLayout) findViewById(R.id.part_table);
		partTable.removeAllViews();
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TableRow part =  new TableRow(this);
        TextView partText = (TextView) inflater.inflate(R.layout.route_details_participator_tablerow, null, false);
        partText.setText(displayedRoute.getCreator().getName());
        part.addView(partText);
        partTable.addView(part);
		for(User u : displayedRoute.getParticipators()) {
			part =  new TableRow(this);
	        partText = (TextView) inflater.inflate(R.layout.route_details_participator_tablerow, null, false);
	        partText.setText(u.getName());
	        part.addView(partText);
	        partTable.addView(part);
		}
	}

	/**
	 * Starts a Route (only visible as Guido).
	 * @param view The touched View.
	 */
	public void startRoute(View view) {
		progress.show();
		DatabaseInteractor.startRoute(this, PreferenceData.getUserId(this), displayedRoute.getId());
	}
	
	/**
	 * Leaves a Route.
	 * @param view The touched View.
	 */
	public void leaveRoute(View view) {
		progress.show();
		DatabaseInteractor.leaveRoute(this, PreferenceData.getUserId(this), displayedRoute.getId());
	}
	
	/**
	 * Deletes a Route (only visible as Guido).
	 * @param view The touched View.
	 */
	public void deleteRoute(View view) {
		progress.show();
		DatabaseInteractor.endRoute(this, PreferenceData.getUserId(this), displayedRoute.getId());
	}

	/**
	 * Joins a Route.
	 * @param view The touched View.
	 */
	public void joinRoute(View view) {
		progress.show();
		if(!displayedRoute.getPassword()) 
			DatabaseInteractor.joinPublic(this, PreferenceData.getUserId(this), displayedRoute.getId());
		else {
			AlertDialog.Builder passwordDialog = new AlertDialog.Builder(this);
			passwordDialog.setTitle("Diese Route erfordert ein Passwort");
			final EditText passwordInput = new EditText(this);
			passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
			passwordDialog.setView(passwordInput);
			final Activity act = this;
			passwordDialog.setPositiveButton("Ok", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.i("ROUTE PASSWORD", passwordInput.getText().toString());
					DatabaseInteractor.joinPrivate(act, PreferenceData.getUserId(act), displayedRoute.getId(), passwordInput.getText().toString());
				}
			});
			passwordDialog.setNegativeButton("Cancel", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					progress.hide();
					dialog.dismiss();
				}
			});
			AlertDialog diag = passwordDialog.create();
			diag.show();
		}
	}
	
	/**
	 * Opens a full size map when touching the mini map.
	 * @param view The touched View.
	 */
	public void openFullSizeMap(View view) {
		System.out.println("BLABLA");
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
		Intent intent;
		Response resp = ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
		switch (resp.getId()) {
		case Constants.RESP_START_ROUTE:
			intent = new Intent(this, ActiveRoute.class);
			intent.putExtra("route", displayedRoute);
			startActivity(intent);
			break;
		case Constants.RESP_JOIN_PUBLIC:
			DatabaseInteractor.getRouteDetails(this, PreferenceData.getUserId(this), resp.getObject().toString());
			break;
		case Constants.RESP_JOIN_PRIVATE:
			DatabaseInteractor.getRouteDetails(this, PreferenceData.getUserId(this), resp.getObject().toString());
			break;
		case Constants.RESP_GET_ROUTE_DETAILS:
			intent = new Intent(this, RouteDetail.class);
			intent.putExtra("route", (Route) resp.getObject());
			startActivity(intent);
			finish();
			break;
		case Constants.RESP_LEAVE_ROUTE:
			DatabaseInteractor.getRouteDetails(this, PreferenceData.getUserId(this), resp.getObject().toString());
			break;
		case Constants.RESP_END_ROUTE:
			intent = new Intent(this, RoutesOverview.class);
			startActivity(intent);
			finish();
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
