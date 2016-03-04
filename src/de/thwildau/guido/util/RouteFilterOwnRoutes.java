package de.thwildau.guido.util;

import java.util.ArrayList;

import android.util.Log;

import de.thwildau.guido.model.Route;

public class RouteFilterOwnRoutes extends RouteFilter{

	String creatorID;
	
	public RouteFilterOwnRoutes(String userId){
		creatorID = userId;
	}
	
	@Override
	protected ArrayList<Route> filter(ArrayList<Route> route) {
		Log.i("F_OWNROUTES", "filter started... remaining list size: "+route.size());
		ArrayList<Route> returnList = new ArrayList<Route>();
		for(Route r:route){
			Log.i("F_OWNROUTES", "checking "+r.getName()+": "+r.getCreator().getId()+" "+creatorID);
			if(r.getCreator().getId().equals(creatorID))
				returnList.add(r);
		}
		if(next!=null)
			return next.filter(returnList);
		return returnList;
	}

}
