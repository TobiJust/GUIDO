package de.thwildau.guido.routing;

import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import de.thwildau.guido.util.Constants;

public class GuidoRangeCircle extends ItemizedIconOverlay<OverlayItem> {

	private GeoPoint loc;
	
	public GuidoRangeCircle(
			Context pContext,
			List<OverlayItem> pList,
			org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener) {
		super(pContext, pList, pOnItemGestureListener);
		// TODO Auto-generated constructor stub
	}
	
	public void draw(Canvas canvas, MapView mapView, boolean shadow){
        super.draw(canvas, mapView, shadow); 
        Point point = new Point();

        Projection projection = mapView.getProjection();
        projection.toPixels(loc, point);
        float projectedRadius = projection.metersToEquatorPixels((float)Constants.ACTIVE_ROUTE_MAX_DISTANCE*2);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(100, 127, 190, 229);
        canvas.drawCircle((float)point.x, (float)point.y, projectedRadius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setARGB(0, 127, 190, 229);
        canvas.drawCircle((float)point.x, (float)point.y, projectedRadius, paint);
    }
	
	public void setLocation(GeoPoint l){
		loc = l;
	}

}
