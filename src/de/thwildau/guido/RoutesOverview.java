package de.thwildau.guido;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;
import de.thwildau.guido.util.RouteListAdapter;
import de.thwildau.guido.util.RouteListFilter;
import de.thwildau.guido.util.RouteSortDate;

/**
 * This Activity displays an overview about all created Routes
 * in a {@link ListView}. The lists items are LinearLayouts 
 * described in the layout xml. Selecting one opens the 
 * {@link RouteDetail}. It observes {@link HttpConnector} to be
 * notified when the data exchange finishes.
 * @author GUIDO
 * @version 2013-12-20
 * @see ListActivity
 * @see Observer
 */
public class RoutesOverview extends ListActivity implements Observer {

	/**
	 * A List containing the routes to display.
	 */
	private static ArrayList<Route> routenList;

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;

	/**
	 * Called when the activity is first created.
	 * Currently shows the up button in the action bar, eventually not needed.
	 * @param savedInstanceState If the activity is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_route_2lined_overview);
		// Show the Up button in the action bar.
		setupActionBar();

		routenList = (ArrayList<Route>) getIntent().getSerializableExtra("routes");
		if(routenList != null && (routenList.size() > 0))
			provideTwoLineData(routenList);
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
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
		getMenuInflater().inflate(R.menu.app_menu_logout_filter, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Called when a menu item was selected. If the logout item was selected,
	 * the user will be logged out. If the filter icon was selected a 
	 * {@link FilterDialog} will be opened.
	 * @param item The selected menu item.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			DatabaseInteractor.logout(this, PreferenceData.getUserId(this));
			break;
		case R.id.action_filter:
			FilterDialogFragment diagFrag = new FilterDialogFragment();
			Bundle args = new Bundle();
			args.putString("name", RouteListFilter.filter_name);
			args.putString("category", RouteListFilter.filter_category);
			args.putString("traveltype", RouteListFilter.filter_traveltype);
			args.putBoolean("public", RouteListFilter.filter_publicOnly);
			args.putBoolean("notFull", RouteListFilter.filter_notFull);
			args.putBoolean("ownRoutes", RouteListFilter.filter_ownOnly);
			args.putInt("maxParts", (RouteListFilter.filter_maxParts==null?0:RouteListFilter.filter_maxParts));
			if(RouteListFilter.filter_startDate!=null)
				args.putString("date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMAN).format(RouteListFilter.filter_startDate));
			diagFrag.setArguments(args);
			diagFrag.show(getFragmentManager(), "Filter");
			break;
		case R.id.action_refreshRoutes:
			progress.show();
			DatabaseInteractor.getRoutes(this, PreferenceData.getUserId(this));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Provides the data for a ListView which two lined items. It also handles
	 * the selection of an item by opening the {@link RouteDetail}. The displayed
	 * List of items is sorted by starting date.
	 * @param displayedRoutes The Routes which shall be listed in the ListView.
	 */
	public void provideTwoLineData(ArrayList<Route> routesList) {
		final ArrayList<Route> filteredRoutesList = RouteListFilter.filterRoutes(this, routesList);
		Collections.sort(filteredRoutesList, new RouteSortDate());
		ArrayList<HashMap<String, String>> listContent = new ArrayList<HashMap<String, String>>();
		for(Route r : filteredRoutesList) {
			HashMap<String, String> temp = new HashMap<String, String>();
			temp.put("line1", r.getName());
			temp.put("line2", new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN).format(r.getDate()) + " - " + r.getCategory());
			temp.put("id", r.getId());
			listContent.add(temp);
		}
		final ListView routesListView = getListView();

		// using custom adapter

		RouteListAdapter adapter = new RouteListAdapter(this, listContent, R.layout.activity_route_2lined_overview, new String[] {"line1", "line2"}, new int[] {R.id.list_item_line1, R.id.list_item_line2});
		routesListView.setBackgroundResource(R.drawable.general_back);
		routesListView.setAdapter(adapter);

		final Activity act = this;
		routesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				Route selectedRoute = filteredRoutesList.get(position);
				progress.show();
				DatabaseInteractor.getRouteDetails(act, PreferenceData.getUserId(act), selectedRoute.getId());
			}
		});
	}


	/**
	 * This method is called whenever the observed object {@link HttpConnector}
	 * is changed via its notify() method when the data exchange is finished. 
	 * It passes the response gotten by the server to {@link ResponseHandler#handleResponse(Activity, String)}.
	 * @param oberservable The observable object.
	 * @param data An argument passed to the notifyObservers method.
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
		case Constants.RESP_GET_ROUTE_DETAILS:
			Intent intent = new Intent(this, RouteDetail.class);
			intent.putExtra("route", (Route) resp.getObject());
			startActivity(intent);
			break;
		case Constants.RESP_LIST_ROUTES:
			routenList = (ArrayList<Route>)resp.getObject();
			if(routenList != null && (routenList.size() > 0))
				provideTwoLineData(routenList);
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
	 * Get the List of routes.
	 * @return The List of Routes.
	 */
	public static ArrayList<Route> getRoutes() {
		return routenList;
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
	 * Called when the Activity gets visible to the user.
	 */
	@Override
	protected void onResume(){
		super.onResume();
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		DatabaseInteractor.getRoutes(this, PreferenceData.getUserId(this));
	}
}
