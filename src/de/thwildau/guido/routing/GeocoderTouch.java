package de.thwildau.guido.routing;

import java.io.IOException;
import java.util.List;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import de.thwildau.guido.RouteCreatorMap;

import android.location.Address;
import android.os.AsyncTask;

public class GeocoderTouch extends AsyncTask<GeoPoint, Void, List<Address>[]>{

	public static final String NOMINATIM_SERVICE_URL = "http://nominatim.openstreetmap.org/search?countrycodes=de";
	public final int MAX_LIST_VALUES = 5;
	private GeocoderNominatim gcNominatim;
	private RouteCreatorMap activity;
	private GuidoPoint point;
	private int index = 0;

	public GeocoderTouch(RouteCreatorMap activity){
		this.activity = activity;
		gcNominatim = new GeocoderNominatim(activity);
	}
	public GeocoderTouch(MapView map){
		//Get GeoLocation from Address
		gcNominatim = new GeocoderNominatim(map.getContext());
	}	
	@Override
	protected List<Address>[] doInBackground(GeoPoint... locations) {
		List<Address>[] addresses = new List[locations.length];
		try {
			addresses = fromLocation(locations);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return addresses;
	}
	protected void onPostExecute(List<Address>[] result){
		if(result != null){ 
			//			activity.setStartAddress(result[0].get(0));
			buildListOutput(result[0]);
		}

	}

	private List<Address>[] fromLocation(GeoPoint[] locationPoints) throws IOException{	
		List<Address> addressListForLocation = null;
		List<Address>[] addresses = new List[locationPoints.length];
		index = 0;
		for(GeoPoint loc : locationPoints){
			if(loc != null){
				addressListForLocation = gcNominatim.getFromLocation(loc.getLatitude(), loc.getLongitude(), MAX_LIST_VALUES);
				if(addressListForLocation.size() == 0)
					return null;
				addresses[index++] = addressListForLocation;
			}
		}
		return addresses;
	}
	private void buildListOutput(List<Address> list){
		activity.createPOIWithTouch(list);
	}
}