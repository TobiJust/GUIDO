package de.thwildau.guido.util;

import java.util.ArrayList;

import android.util.Log;
import de.thwildau.guido.model.Route;

public class RouteFilterCategory extends RouteFilter{

	String category;
	
	public RouteFilterCategory(String cat){
		category = cat;
	}
	
	@Override
	protected ArrayList<Route> filter(ArrayList<Route> route) {
		Log.i("F_CATEGORY", "filter started... remaining list size: "+route.size());
		ArrayList<Route> returnList = new ArrayList<Route>();
		for(Route r:route){
			Log.i("F_CATEGORY", "checking "+r.getName()+": "+r.getCategory()+" "+category);
			if(r.getCategory().equals(category))
				returnList.add(r);
		}
		if(next!=null)
			return next.filter(returnList);
		return returnList;
	}

}
