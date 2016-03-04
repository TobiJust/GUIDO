package de.thwildau.guido.util;

import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * A custom location listener to get the user current location. It implements the 
 * Singleton design pattern.
 * @author Guido
 * @see LocationListener
 */
public class GuidoLocationListener implements LocationListener {

	/**
	 * The user current location
	 */
	GeoPoint currentLocation;
	/**
	 * The calling Activity
	 */
	Activity act;
	/**
	 * A custom MapView
	 */
	GuidoMapView guidoMap;
	/**
	 * The location manager
	 */
	LocationManager lm;
	/**
	 * Static instance of this class for the Singleton design pattern
	 */
	static GuidoLocationListener instance = null;
	
	/**
	 * Private constructor to implement the Singleton design pattern.
	 */
	private GuidoLocationListener() {}
	
	/**
	 * Sets the calling Activity.
	 * @param act The Activity to set.
	 */
	public void setAct(Activity act) {
		this.act = act;
	}

	/**
	 * Sets the custom MapView.
	 * @param guidoMap The custom MapView.
	 */
	public void setGuidoMap(GuidoMapView guidoMap) {
		this.guidoMap = guidoMap;
	}

	/**
	 * @param lm the lm to set
	 */
	public void setLm(LocationManager lm) {
		this.lm = lm;
	}
	
	/**
	 * Returns the LocationManager.
	 * @return The LocationManager.
	 */
	public LocationManager getLm() {
		if(lm == null)
			 lm = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
		return lm;
	}

	/**
	 * Returns the reference of this class.
	 * @return The reference of this class.
	 */
	public static GuidoLocationListener getReference(){
		if(instance == null)
			instance = new GuidoLocationListener();
		return instance;
	}
	
	/**
	 * Sends the new current location to the database and shows it on the map.
	 */
    public void onLocationChanged(Location location) {
        currentLocation = new GeoPoint(location);
        DatabaseInteractor.sendPosition(act, PreferenceData.getUserId(act), currentLocation.getLatitude(), currentLocation.getLongitude());
		guidoMap.showMyLocation();
    }

    /**
     * Ends the listeners.
     */
    public void endListener(){
    	lm.removeUpdates(instance);
    }

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}