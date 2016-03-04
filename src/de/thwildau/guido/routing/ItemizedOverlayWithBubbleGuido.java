package de.thwildau.guido.routing;

import java.util.List;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.views.MapView;

import android.content.Context;

public class ItemizedOverlayWithBubbleGuido extends ItemizedOverlayWithBubble<ExtendedOverlayItem>{

	public ItemizedOverlayWithBubbleGuido(Context context,
			List<ExtendedOverlayItem> aList, MapView mapView) {
		super(context, aList, mapView);
	}

	@Override
	protected boolean onSingleTapUpHelper(int index, ExtendedOverlayItem item,
			MapView mapView) {
		return true;
	}
	
	
}
