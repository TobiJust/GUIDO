package de.thwildau.guido.util;

import java.util.ArrayList;

import android.util.Log;

import de.thwildau.guido.model.Route;

public class RouteFilterPublic extends RouteFilter{

	@Override
	protected ArrayList<Route> filter(ArrayList<Route> route) {
		Log.i("F_PUBLIC", "filter started... remaining list size: "+route.size());
		ArrayList<Route> returnList = new ArrayList<Route>();
		for(Route r:route){
			Log.i("F_PUBLIC", "checking "+r.getName()+": "+r.getPassword());
			if(!r.getPassword())
				returnList.add(r);
		}
		if(next!=null)
			return next.filter(returnList);
		return returnList;
	}

}
