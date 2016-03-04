package de.thwildau.guido.routing;

import java.util.List;

import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import de.thwildau.guido.ActiveRoute;
import de.thwildau.guido.model.POI;

public class GuidoPoint extends ItemizedIconOverlay<OverlayItem> {
	private static boolean isPOI = false;
	protected Context mContext;
	private POI poi;
	private MapView map;
	private boolean editable;
	private boolean tapable;


	public GuidoPoint(final Context context, List<OverlayItem> aList, final MapView mapView, final InfoWindow bubble, POI poi, boolean editable, boolean tapable) {
		super(context, aList, new OnItemGestureListener<OverlayItem>() {
			@Override 
			public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
				return false;
			}
			@Override 
			public boolean onItemLongPress(final int index, final OverlayItem item) {
				return false;
			}
		});
		this.map = mapView;
		this.poi = poi;
		this.editable = editable;
		this.tapable = tapable;
		mContext = context;
	}
	public GuidoPoint(final Context context, final List<OverlayItem> aList, final MapView mapView, POI poi, boolean editable, boolean tapable) {
		this(context, aList, mapView, null, poi, editable, tapable);
	}

	@Override 
	protected boolean onSingleTapUpHelper(final int index, final OverlayItem item, final MapView mapView) {
		if(!tapable)
			return true;
		isPOI = true;
		AddressDialogFragment editDialog = new AddressDialogFragment();
		editDialog.setPOI(poi);
		editDialog.setShowAddressLine(false);
		if(!editable)
			editDialog.setEditable(false);
		editDialog.show(((Activity) mContext).getFragmentManager(), "EditDialog");
		
		return true;
	}
	@Override
	/**
	 * Long Press on POI
	 */
	public boolean onLongPressHelper(final int index, final OverlayItem item) {
		if(!tapable)
			return false;
		map.setMapListener(new MapListener() {
			@Override
			public boolean onZoom(ZoomEvent arg0) {
				return false;
			}
			
			@Override
			public boolean onScroll(ScrollEvent arg0) {
				System.out.println("SCROLLL");
				return false;
			}
		});
		poi.getLat();
		return false;
	}
	public static boolean isGuidoPOI(){
		return isPOI;
	}
	public static void setPOI(boolean isPOI) {
		GuidoPoint.isPOI = isPOI;
	}
	public GeoPoint getGeoPoint() {
		return new GeoPoint(poi.getLat(), poi.getLng());
	}
}