package de.thwildau.guido.util;

import java.util.ArrayList;

import android.util.Log;

import de.thwildau.guido.model.Route;

public class RouteFilterNotFull extends RouteFilter{

	@Override
	protected ArrayList<Route> filter(ArrayList<Route> route) {
		Log.i("F_NOTFULL", "filter started... remaining list size: "+route.size());
		ArrayList<Route> returnList = new ArrayList<Route>();
		for(Route r:route){
			Log.i("F_NOTFULL", "checking "+r.getName()+": "+r.getCurrPart()+" "+r.getMaxPart());
			if(r.getCurrPart()<r.getMaxPart())
				returnList.add(r);
		}
		if(next!=null)
			return next.filter(returnList);
		return returnList;
	}

}
