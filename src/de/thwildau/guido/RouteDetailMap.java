package de.thwildau.guido;

import org.osmdroid.views.MapView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoMapView;
import de.thwildau.guido.util.PreferenceData;

/**
 * This Activity shows a full size map accessible from {@link RouteDetail}.
 * @author Guido
 * @see Activity
 */
public class RouteDetailMap extends Activity {

	/**
	 * The displayed Route
	 */
	Route displayedRoute;
	
	/**
	 * Called when the Activity is created. Inflates the layout.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_detail_map);
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Shows a dummy settings button and a logout button which logs the user out.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_menu_logout, menu);
		return true;
	}

	/**
	 * Called when the Activity gets visible to the user. Initializes the Route and the map.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		displayedRoute = (Route) getIntent().getSerializableExtra("route");
		MapView map = (MapView)findViewById(R.id.route_detail_map);
		new GuidoMapView(this, map, displayedRoute, false, false, true);
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
}
