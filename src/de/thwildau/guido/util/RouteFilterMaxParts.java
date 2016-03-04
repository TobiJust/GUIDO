package de.thwildau.guido.util;

import java.util.ArrayList;

import android.util.Log;

import de.thwildau.guido.model.Route;

public class RouteFilterMaxParts extends RouteFilter{

	int maxParts;
	
	public RouteFilterMaxParts(int max){
		maxParts = max;
	}
	
	@Override
	protected ArrayList<Route> filter(ArrayList<Route> route) {
		Log.i("F_MAXPARTS", "filter started... remaining list size: "+route.size());
		ArrayList<Route> returnList = new ArrayList<Route>();
		for(Route r:route){
			Log.i("F_MAXPARTS", "checking "+r.getName()+": "+r.getMaxPart()+" "+maxParts);
			if(r.getMaxPart()<=maxParts)
				returnList.add(r);
		}
		if(next!=null)
			return next.filter(returnList);
		return returnList;
	}

}
