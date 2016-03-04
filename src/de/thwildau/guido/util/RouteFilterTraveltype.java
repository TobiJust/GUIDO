package de.thwildau.guido.util;

import java.util.ArrayList;

import android.util.Log;

import de.thwildau.guido.model.Route;

public class RouteFilterTraveltype extends RouteFilter{

	String traveltype;
	
	public RouteFilterTraveltype(String trav){
		traveltype = trav;
	}
	
	@Override
	protected ArrayList<Route> filter(ArrayList<Route> route) {
		Log.i("F_TRAVELTYPE", "filter started... remaining list size: "+route.size());
		ArrayList<Route> returnList = new ArrayList<Route>();
		for(Route r:route){
			Log.i("F_TRAVELTYPE", "checking "+r.getName()+": "+Constants.ROUTE_TRAVELTYPES[r.getTravelType()-1]+" "+traveltype);
			if(Constants.ROUTE_TRAVELTYPES[r.getTravelType()-1].equals(traveltype))
				returnList.add(r);
		}
		if(next!=null)
			return next.filter(returnList);
		return returnList;
	}

}
