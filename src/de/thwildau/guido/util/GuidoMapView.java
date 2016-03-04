package de.thwildau.guido.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.BillingManager;

import de.thwildau.guido.R;
import de.thwildau.guido.RouteCreatorMap;
import de.thwildau.guido.RouteDetail;
import de.thwildau.guido.RouteDetailMap;
import de.thwildau.guido.model.POI;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.model.User;
import de.thwildau.guido.routing.GeocoderTouch;
import de.thwildau.guido.routing.GuidoPoint;
import de.thwildau.guido.routing.GuidoRangeCircle;
import de.thwildau.guido.routing.InstructionViewAdapter;
import de.thwildau.guido.routing.ItemizedOverlayWithBubbleGuido;

/**
 * This class shows the OSM MapViews in a customized map.
 * @author Guido
 * @see MapEventsReceiver
 */
public class GuidoMapView implements MapEventsReceiver{

	private IMapController mapController;
	private MapEventsOverlay mapOverlay;
	private ArrayList<GeoPoint> geoPoints;
	private MyLocationNewOverlay posOverlay;
	private ArrayList<ItemizedIconOverlay<OverlayItem>> partOverlays = new ArrayList<ItemizedIconOverlay<OverlayItem>>();
	private ArrayList<POI> routePoints = new ArrayList<POI>();
	private Road road;
	private ListView instructionView;
	private boolean toggle = true;
	private int bubbleToggle = -1;
	private Route r;
	private MapView guidoMap;
	private Activity act;
	private boolean showHints;
	private boolean focus;
	private boolean editable;
	private JSONObject resp;
	private ItemizedOverlayWithBubbleGuido roadNodes;
	private PathOverlay roadOverlay;
	private boolean tapable;

	/**
	 * Constructor assigning the given parameter.
	 * @param act The calling Activity.
	 * @param map The map to show.
	 * @param r The Route to show.
	 * @param showHints True if navigation hints shall be shown.
	 * @param editable True if POI shall be editable.
	 * @param tapable True if POI shall be tapable.
	 */
	public GuidoMapView(Activity act, MapView map, Route r, boolean showHints, boolean editable, boolean tapable) {
		this.r = r;
		this.act = act;
		this.showHints = showHints;
		this.editable = editable;
		this.tapable = tapable;
		geoPoints = new ArrayList<GeoPoint>();
		map.setTileSource(TileSourceFactory.MAPQUESTOSM);	//View of the Map, e.g. Satellite, etc.
		map.setBuiltInZoomControls(false);
		map.setMultiTouchControls(true);				//Zoom with two Fingers, Scroll by one Finger
		map.setMaxZoomLevel(19);
		mapController = map.getController();
		mapController.setZoom(16);
		mapOverlay = new MapEventsOverlay(act, this);
		map.getOverlays().add(mapOverlay);
		guidoMap = map;
		if(showHints) 
			instructionView = (ListView) act.findViewById(R.id.listView1);
		if(r.getPois() != null) {
			for(POI poi : r.getPois()) {
				updateUI(poi);
			}
		}
		if(showHints || editable)
			showMyLocation();
	}

	/**
	 * Called when a long press event was fired.
	 */
	@Override
	public boolean longPressHelper(IGeoPoint point) {
		if(editable) {
			Vibrator vib = (Vibrator)act.getSystemService(Context.VIBRATOR_SERVICE);
			vib.vibrate(100);
			GeoPoint gPoint = new GeoPoint((GeoPoint) point);
			new GeocoderTouch((RouteCreatorMap)act).execute(gPoint);
		}
		return false;
	}

	/**
	 * Called on a single tap.
	 */
	@Override
	public boolean singleTapUpHelper(IGeoPoint arg0) {
		if(act instanceof RouteDetail) {
			Intent intent = new Intent(act, RouteDetailMap.class);
			intent.putExtra("route", r);
			act.startActivity(intent);
		}
		return false;
	}

	/**
	 * POST Request via AsynTask with explicit points to get directions (routes).
	 */
	public void createRoute(){
		new DownloadRoutingTask().execute(geoPoints);
	}
	
	/**
	 * Center the map to my current position.
	 */
	public void showMyLocation(){
		if(posOverlay == null) {
			posOverlay = new MyLocationNewOverlay(act, guidoMap);
			posOverlay.enableMyLocation();
			posOverlay.setDrawAccuracyEnabled(false);
		}
		// TODO: Catch no location found
		posOverlay.runOnFirstFix(new Runnable() {
			public void run(){
				GeoPoint geo = null;
				geo = posOverlay.getMyLocation();
				if(showHints) {
					if(focus)
						mapController.animateTo(geo);
				}
				else
					mapController.animateTo(geo);
			}
		});
		guidoMap.getOverlays().add(posOverlay);
	}
	
	/**
	 * Update the map with a new GeoPoint
	 *  
	 * @param poi Point to add on the map.
	 */
	public void updateUI(POI poi){
		ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
		GuidoPoint guidoPOI = new GuidoPoint(act, list, guidoMap, poi, editable, tapable);
		OverlayItem overlayItem = new OverlayItem("Guido POI", "Guido POI", new GeoPoint(poi.getLat(), poi.getLng()));
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		Drawable marker = guidoMap.getResources().getDrawable(R.drawable.mapneedle);
		overlayItem.setMarker(marker);
		guidoPOI.addItem(overlayItem);
		guidoMap.getOverlays().add(guidoPOI);      
		guidoMap.invalidate();
		if(showHints) {
			if(focus) {
				mapController.animateTo(new GeoPoint(poi.getLat(), poi.getLng()));
			}
		}
		else
			mapController.animateTo(new GeoPoint(poi.getLat(), poi.getLng()));

		routePoints.add(poi);
		geoPoints.add(guidoPOI.getGeoPoint());
		if(routePoints.size() > 1)
			createRoute();
	}
	
	/**
	 * Update the map with a new Road (Route)
	 * 
	 * @param uiRoad The road to add on the map.
	 */
	public void updateUI(Road uiRoad){
		List<Overlay> mapOverlays = guidoMap.getOverlays();
		if(mapOverlays.contains(roadOverlay))
			mapOverlays.remove(roadOverlay);
		roadOverlay = RoadManager.buildRoadOverlay(uiRoad, act);
		////		Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
		mapOverlays.add(roadOverlay);
		guidoMap.invalidate();
		showRouteSteps(showHints);
	}

	/**
	 * Updates the UI with the participants and guides new positions.
	 * @param r
	 */
	public void updateUI(Route r) {
		for(ItemizedIconOverlay<OverlayItem> iio:partOverlays){
			guidoMap.getOverlays().remove(iio);
		}
		if(PreferenceData.getUserId(act).equals(r.getCreator().getId())){
			for(User u : r.getParticipators()) {
				ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
				ItemizedIconOverlay<OverlayItem> overlayIcon = new ItemizedIconOverlay<OverlayItem>(act, list, null);
				OverlayItem overlayItem = new OverlayItem("Guido POI", "Guido POI", new GeoPoint(u.getLat(), u.getLng()));
				overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
				Drawable marker = guidoMap.getResources().getDrawable(R.drawable.ic_part_icon);
				Bitmap bitmap = ((BitmapDrawable) marker).getBitmap();
				Drawable d = new BitmapDrawable(act.getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
				overlayItem.setMarker(d);
				overlayIcon.addItem(overlayItem);
				guidoMap.getOverlays().add(overlayIcon);
				guidoMap.invalidate();
				partOverlays.add(overlayIcon);
			}
			if(r.getCreator()!=null){
				Log.i("DRAWING GUIDO CIRCLE", "AT: "+r.getCreator().getLat()+" "+r.getCreator().getLng());
				ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
				GuidoRangeCircle overlayIcon = new GuidoRangeCircle(act, list, null);
				overlayIcon.setLocation(new GeoPoint(r.getCreator().getLat(), r.getCreator().getLng()));
				guidoMap.getOverlays().add(overlayIcon);
				guidoMap.invalidate();
				partOverlays.add(overlayIcon);
			}
		}
		else{
			ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
			GuidoRangeCircle overlayIcon = new GuidoRangeCircle(act, list, null);
			overlayIcon.setLocation(new GeoPoint(r.getCreator().getLat(), r.getCreator().getLng()));
			OverlayItem overlayItem = new OverlayItem("Guido POI", "Guido POI", new GeoPoint(r.getCreator().getLat(), r.getCreator().getLng()));
			overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
			Drawable marker = guidoMap.getResources().getDrawable(R.drawable.ic_guido_icon);
			Bitmap bitmap = ((BitmapDrawable) marker).getBitmap();
			Drawable d = new BitmapDrawable(act.getResources(), Bitmap.createScaledBitmap(bitmap, 150, 100, true));
			overlayItem.setMarker(d);
			overlayIcon.addItem(overlayItem);
			guidoMap.getOverlays().add(overlayIcon);      
			guidoMap.invalidate();
			partOverlays.add(overlayIcon);
		}

	}

	/**
	 * By OnTouch on route markers, the next route episode will be characterized
	 * @param show 
	 */
	public void showRouteSteps(boolean show){
		if(show) {
			final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
			roadNodes = new ItemizedOverlayWithBubbleGuido(act, roadItems, guidoMap);
			guidoMap.getOverlays().add(roadNodes);
			InstructionViewAdapter iva = new InstructionViewAdapter(act, android.R.layout.simple_dropdown_item_1line, road.mNodes);
			iva.setGuidoMap(this);
			instructionView.setAdapter(iva);
			instructionView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					LayoutParams lparamsShow = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, instructionView.getChildAt(0).getHeight() * 4);
					lparamsShow.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					LayoutParams lparamsHide= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, instructionView.getChildAt(0).getHeight());
					lparamsHide.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

					if(toggle) {
						instructionView.setLayoutParams(lparamsShow);
						toggle = false;
					}
					else {
						instructionView.setLayoutParams(lparamsHide);
						toggle = true;
					}
				}
			});

			Drawable marker = guidoMap.getResources().getDrawable(R.drawable.nav_hint_overlay);
			System.out.println(road.mNodes.size());
			for(int i=0; i < road.mNodes.size(); i++){
				RoadNode node = road.mNodes.get(i);
				ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Schritt " + (i+1), "", node.mLocation, act);
				nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
				nodeMarker.setMarker(marker);

				nodeMarker.setDescription(node.mInstructions);
				nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
				Drawable icon = guidoMap.getResources().getDrawable(R.drawable.ic_continue);
				nodeMarker.setImage(icon);

				roadNodes.addItem(nodeMarker);
			}
		}
	}
	/**
	 * Async Task to get the route from the OSM Server
	 * @author Guido
	 * @see AsyncTask
	 */
	public class DownloadRoutingTask extends AsyncTask<Object, Void, Road>{

		@Override
		protected Road doInBackground(Object... way) {
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)way[0];
			RoadManager roadManager = null;
			Locale locale = Locale.getDefault();
			roadManager = new MapQuestRoadManager();
			roadManager.addRequestOption("locale=" + locale.getLanguage() + "_" + locale.getCountry());
			switch(r.getTravelType()){ 
			case Constants.TRAVEL_AFOOT:
				roadManager.addRequestOption("key=Fmjtd%7Cluubn1a7n5%2C2w%3Do5-90rx1w");
				roadManager.addRequestOption("routeType=pedestrian");
				break;
			case Constants.TRAVEL_CAR:
				roadManager.addRequestOption("key=Fmjtd%7Cluubn1a7n5%2C2w%3Do5-90rx1w");
				roadManager.addRequestOption("routeType=fastest");
				break;
			case Constants.TRAVEL_BIKE:
				roadManager.addRequestOption("key=Fmjtd%7Cluubn1a7n5%2C2w%3Do5-90rx1w");
				roadManager.addRequestOption("routeType=bicycle");
				break;
			}
			ArrayList<GeoPoint> clonedList = (ArrayList<GeoPoint>)waypoints.clone();
			Collections.reverse(clonedList);
			for(GeoPoint g:clonedList){
				Log.i("REVERTED LIST GEOPOINT", g.toString());
			}
			Road road = roadManager.getRoad(clonedList);
			Log.i("DOINBACKGROUND", road.mStatus+"");
			if(road.mStatus==Road.STATUS_OK){
				return road;
			}
			else{
				return null;
			}
		}

		/**
		 * Runs on the UI thread after doInBackground(Params...)
		 * @param result The return value of doInBackground.
		 */
		protected void onPostExecute(Road result){
			if(result==null){
				Toast.makeText(guidoMap.getContext(), "Keine Route zum angegebenen Punkt möglich!", Toast.LENGTH_LONG).show();
				guidoMap.getOverlays().remove(guidoMap.getOverlays().size()-1);
				routePoints.remove(routePoints.size()-1);
				geoPoints.remove(geoPoints.size()-1);
				guidoMap.invalidate();
			}
			else{
				road = result;
				updateUI(result);
			}
		}
	}

	/**
	 * Initializes a purchase.
	 * @param details The purchases item details.
	 */
	public void onPurchase(Bundle details){
		AlertDialog.Builder finishDialog = new AlertDialog.Builder(act);
		if(details.getInt("RESPONSE_CODE")==0 && !act.isFinishing()){
			ArrayList<String> responseList = details.getStringArrayList("DETAILS_LIST");
			try {
				resp = new JSONObject(responseList.get(0));
				finishDialog.setTitle("Kostenpflichtiges Item \""+resp.getString("title")+"\" kaufen?");
				TextView desc = new TextView(act);
				desc.setText(resp.getString("description"));
				desc.append("\n\n"+resp.getString("price"));
				finishDialog.setView(desc);
				finishDialog.setPositiveButton("Bezahlen", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Response r;
						try {
							r = new Response(Constants.REQ_PROD_LIST, resp.getString("productId"));
							BillingManager.getReference().executeRequest(r);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

				finishDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				finishDialog.show();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			finishDialog.setTitle("Fehler bei der Verbindung mit dem Playstore");
			finishDialog.setIcon(R.drawable.ic_launcher);

			finishDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			finishDialog.show();
		}
	}

	public ArrayList<POI> getPOIs() {
		return routePoints;
	}

	public IMapController getMapController() {
		return mapController;
	}

	public void setFocus(boolean focus) {
		this.focus = focus;
	}

	/**
	 * Deletes the last set POI.
	 */
	public void deleteLastPOI(){
		if(guidoMap.getOverlays().size()>0)
			guidoMap.getOverlays().remove(guidoMap.getOverlays().size()-1);
		if(routePoints.size()>0){
			routePoints.remove(routePoints.size()-1);
			if(routePoints.size()>0)
				guidoMap.getOverlays().remove(guidoMap.getOverlays().size()-1);
		}
		if(geoPoints.size()>0)
			geoPoints.remove(geoPoints.size()-1);
		guidoMap.invalidate();
	}

	public void setActivity(Activity activity){
		act = activity;
	}

	public ItemizedOverlayWithBubbleGuido getRoadNodes() {
		return roadNodes;
	}

	public void showBubbleHint(int position){
		if(bubbleToggle != position){
			roadNodes.showBubbleOnItem(position, guidoMap, true);
			bubbleToggle = position;
		}
		else{
			roadNodes.hideBubble();
			bubbleToggle = -1;
		}
	}
}
