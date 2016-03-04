package de.thwildau.guido.routing;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import de.thwildau.guido.RouteCreatorMap;

import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;


public class Geocoder extends AsyncTask<String, Void, List<Address>[]>{

	public static final String NOMINATIM_SERVICE_URL = "http://nominatim.openstreetmap.org/";
	public final int MAX_LIST_VALUES = 5;
	private GeocoderNominatim gcNominatim;
	private RouteCreatorMap activity;
	private GuidoPoint point;
	private int index = 0;

	public Geocoder(RouteCreatorMap activity){
		this.activity = activity;
		gcNominatim = new GeocoderNominatim(activity);
		gcNominatim.setService(NOMINATIM_SERVICE_URL);
	}
	public Geocoder(MapView map){
		//Get GeoLocation from Address
		gcNominatim = new GeocoderNominatim(map.getContext(), Locale.GERMANY);
		gcNominatim.setService(NOMINATIM_SERVICE_URL);

	}	
	@Override
	protected List<Address>[] doInBackground(String... locations) {
		List<Address>[] addresses = new List[locations.length];
		try {
			addresses = fromLocationName(locations);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		String[] locationNames = null;
		//		GeoPoint[] locationPoints = null;


		//		try {
		//			locationNames = convertToStringArray(locations);
		//			addresses = fromLocationName(locationNames);
		//		} catch (Exception e) {
		//			try {
		//				locationPoints = convertToGeoPointArray(locations);
		//				addresses = fromLocation(locationPoints);
		//			} catch (IOException e1) {
		////				e1.printStackTrace();
		//			}
		//			//			e.printStackTrace();
		//		}
		return addresses;
	}
	protected void onPostExecute(List<Address>[] result){
		if(result!=null && result[0] != null){ 
			//			activity.setStartAddress(result[0].get(0));
			buildListOutput(result[0]);
		}

	}
	private List<Address>[] fromLocationName(String[] locationNames) throws IOException{
		List<Address> addressListForLocation = null;
		List<Address>[] addresses = new List[locationNames.length];
		index = 0;
		for(String loc : locationNames){
			if(loc != null && loc.length() != 0){
				addressListForLocation = getFromLocationName(loc, MAX_LIST_VALUES);
				if(addressListForLocation.size() == 0)
					return null;
				addresses[index++] = addressListForLocation;
			}
		}
		return addresses;
	}
	public List<Address> getFromLocationName(String locationName, int maxResults)
			throws IOException {
		return getFromLocationName(locationName, maxResults, 0.0, 0.0, 0.0, 0.0);
	}
	public List<Address> getFromLocationName(String locationName, int maxResults, 
			double lowerLeftLatitude, double lowerLeftLongitude, 
			double upperRightLatitude, double upperRightLongitude)
					throws IOException {
		String url = NOMINATIM_SERVICE_URL
				+ "search?"
				+ "countrycodes=de"
				+ "&format=json"
				+ "&accept-language=" + Locale.GERMAN
				+ "&addressdetails=1"
				+ "&limit=" + maxResults
				+ "&q=" + URLEncoder.encode(locationName);
		if (lowerLeftLatitude != 0.0 && lowerLeftLongitude != 0.0){
			//viewbox = left, top, right, bottom:
			url += "&viewbox=" + lowerLeftLongitude
					+ "," + upperRightLatitude
					+ "," + upperRightLongitude
					+ "," + lowerLeftLatitude
					+ "&bounded=1";
		}

		Log.d(BonusPackHelper.LOG_TAG, "GeocoderNominatim::getFromLocationName:"+url);
		String result = BonusPackHelper.requestStringFromUrl(url);
		//Log.d(BonusPackHelper.LOG_TAG, result);
		if (result == null)
			throw new IOException();
		try {
			JSONArray jResults = new JSONArray(result);
			List<Address> list = new ArrayList<Address>();
			for (int i=0; i<jResults.length(); i++){
				JSONObject jResult = jResults.getJSONObject(i);
				Address gAddress = buildAndroidAddress(jResult);
				list.add(gAddress);
			}
			return list;
		} catch (JSONException e) {
			throw new IOException();
		}
	}

	protected Address buildAndroidAddress(JSONObject jResult) throws JSONException{
		GuidoAddress gAddress = new GuidoAddress(Locale.GERMAN);
		gAddress.setLatitude(jResult.getDouble("lat"));
		gAddress.setLongitude(jResult.getDouble("lon"));

		JSONObject jAddress = jResult.getJSONObject("address");

		int addressIndex = 0;
		if (jAddress.has("road")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("road"));
			gAddress.setThoroughfare(jAddress.getString("road"));
		}
		if (jAddress.has("suburb")){
			//gAddress.setAddressLine(addressIndex++, jAddress.getString("suburb"));
			//not kept => often introduce "noise" in the address.
			gAddress.setSubLocality(jAddress.getString("suburb"));
		}
		if (jAddress.has("postcode")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("postcode"));
			gAddress.setPostalCode(jAddress.getString("postcode"));
		}

		if (jAddress.has("city")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("city"));
			gAddress.setLocality(jAddress.getString("city"));
		} else if (jAddress.has("town")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("town"));
			gAddress.setLocality(jAddress.getString("town"));
		} else if (jAddress.has("village")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("village"));
			gAddress.setLocality(jAddress.getString("village"));
		}

		if (jAddress.has("county")){ //France: departement
			gAddress.setSubAdminArea(jAddress.getString("county"));
		}
		if (jAddress.has("state")){ //France: region
			gAddress.setAdminArea(jAddress.getString("state"));
		}
		if (jAddress.has("country")){
			gAddress.setAddressLine(addressIndex++, jAddress.getString("country"));
			gAddress.setCountryName(jAddress.getString("country"));
		}
		if (jAddress.has("country_code"))
			gAddress.setCountryCode(jAddress.getString("country_code"));

		if (jAddress.has("house_number"))
			gAddress.setHouseNumber(jAddress.getString("house_number"));
		/* Other possible OSM tags in Nominatim results not handled yet: 
		 * subway, golf_course, bus_stop, parking,...
		 * house, house_number, building
		 * city_district (13e Arrondissement)
		 * road => or highway, ...
		 * sub-city (like suburb) => locality, isolated_dwelling, hamlet ...
		 * state_district
		 */

		//Add non-standard (but very useful) information in Extras bundle:
		Bundle extras = new Bundle();
		if (jResult.has("polygonpoints")){
			JSONArray jPolygonPoints = jResult.getJSONArray("polygonpoints");
			ArrayList<GeoPoint> polygonPoints = new ArrayList<GeoPoint>(jPolygonPoints.length());
			for (int i=0; i<jPolygonPoints.length(); i++){
				JSONArray jCoords = jPolygonPoints.getJSONArray(i);
				double lon = jCoords.getDouble(0);
				double lat = jCoords.getDouble(1);
				GeoPoint p = new GeoPoint(lat, lon);
				polygonPoints.add(p);
			}
			extras.putParcelableArrayList("polygonpoints", polygonPoints);
		}
		if (jResult.has("boundingbox")){
			JSONArray jBoundingBox = jResult.getJSONArray("boundingbox");
			BoundingBoxE6 bb = new BoundingBoxE6(
					jBoundingBox.getDouble(1), jBoundingBox.getDouble(2), 
					jBoundingBox.getDouble(0), jBoundingBox.getDouble(3));
			extras.putParcelable("boundingbox", bb);
		}
		if (jResult.has("osm_id")){
			long osm_id = jResult.getLong("osm_id");
			extras.putLong("osm_id", osm_id);
		}
		if (jResult.has("osm_type")){
			String osm_type = jResult.getString("osm_type");
			extras.putString("osm_type", osm_type);
		}
		gAddress.setExtras(extras);

		return gAddress;
	}
	private void buildListOutput(List<Address> list){
		activity.updateUI(list);
		//point.updateAddressDropdown(list.get(0));
	}
}