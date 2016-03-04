package de.thwildau.guido.util;

import java.util.ArrayList;

import android.util.Log;

import de.thwildau.guido.model.Route;

public class RouteFilterName extends RouteFilter{

	String pattern;
	
	public RouteFilterName(String pat){
		pattern = pat.toLowerCase();
	}
	@Override
	protected ArrayList<Route> filter(ArrayList<Route> route) {
		Log.i("F_NAME", "filter started... remaining list size: "+route.size());
		ArrayList<Route> returnList = new ArrayList<Route>();
		for(Route r:route){
			Log.i("F_NAME", "checking "+r.getName()+": "+pattern);
			if(r.getName().toLowerCase().contains(pattern))
				returnList.add(r);
		}
		if(next!=null)
			return next.filter(returnList);
		return returnList;
	}

}
