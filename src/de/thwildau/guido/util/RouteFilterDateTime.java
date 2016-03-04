package de.thwildau.guido.util;

import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

import de.thwildau.guido.model.Route;

public class RouteFilterDateTime extends RouteFilter{

	Date date;
	
	public RouteFilterDateTime(Date d){
		date = d;
	}
	
	@Override
	protected ArrayList<Route> filter(ArrayList<Route> route) {
		Log.i("F_DATE", "filter started... remaining list size: "+route.size());
		ArrayList<Route> returnList = new ArrayList<Route>();
		for(Route r:route){
			Log.i("F_DATE", "checking "+r.getName()+": "+r.getDate()+" "+date);
			if(r.getDate().after(date))
				returnList.add(r);
		}
		if(next!=null)
			return next.filter(returnList);
		return returnList;
	}

}
