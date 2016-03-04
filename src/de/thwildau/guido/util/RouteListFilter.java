package de.thwildau.guido.util;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.util.Log;
import de.thwildau.guido.RoutesOverview;
import de.thwildau.guido.model.Route;

/**
 * A filter for the {@link RoutesOverview}s ListView.
 * @author GUIDO
 * @version 2013-12-20
 */
public class RouteListFilter {

	public static String filter_name = null;
	public static boolean filter_publicOnly = false;
	public static boolean filter_notFull = false;
	public static boolean filter_ownOnly = false;
	public static Integer filter_maxParts = null;
	public static Date filter_startDate = null;
	public static String filter_category = null;
	public static String filter_traveltype = null;
	/**
	 * Filters the Routes for the given parameters and returns a List with filtered Routes.
	 * @param context The Activities context.
	 * @param routes A Map of all Routes which shall be filtered.	
	 * @param publicOnly Show only public Routes.
	 * @param ownOnly Show only Routes where the logged in user is the creator.
	 * @param startDate Show only Routes starting at the given date.
	 * @return A filtered Map of Routes.
	 */
	private static ArrayList<Route> filterRoutes(Context context, ArrayList<Route> routes, 
			String name,
			boolean publicOnly, 
			boolean notFull,
			boolean ownOnly,
			Integer maxParts,
			Date startDate,
			String category,
			String traveltype) {
		RouteFilter filterChainStart = null;
		RouteFilter currentFilter = null;
		Log.i("FILTER", filter_name+", "+filter_publicOnly+", "+filter_notFull+", "+filter_ownOnly+", "+filter_maxParts+", "+filter_startDate+", "+filter_category+", "+filter_traveltype);

		if(filter_publicOnly){
			filterChainStart = new RouteFilterPublic();
			currentFilter = filterChainStart;
		}
		if(filter_notFull){
			if(filterChainStart==null){
				filterChainStart = new RouteFilterNotFull();
				currentFilter = filterChainStart;
			}
			else{
				currentFilter.setNext(new RouteFilterNotFull());
				currentFilter = currentFilter.getNext();
			}
		}
		if(filter_ownOnly){
			if(filterChainStart==null){
				filterChainStart = new RouteFilterOwnRoutes(PreferenceData.getUserId(context));
				currentFilter = filterChainStart;
			}
			else{
				currentFilter.setNext(new RouteFilterOwnRoutes(PreferenceData.getUserId(context)));
				currentFilter = currentFilter.getNext();
			}
		}
		if(filter_name!=null){
			if(filterChainStart==null){
				filterChainStart = new RouteFilterName(name);
				currentFilter = filterChainStart;
			}
			else{
				currentFilter.setNext(new RouteFilterName(name));
				currentFilter = currentFilter.getNext();
			}
		}
		if(filter_maxParts!=null){
			if(filterChainStart==null){
				filterChainStart = new RouteFilterMaxParts(maxParts);
				currentFilter = filterChainStart;
			}
			else{
				currentFilter.setNext(new RouteFilterMaxParts(maxParts));
				currentFilter = currentFilter.getNext();
			}
		}
		if(filter_startDate!=null){
			if(filterChainStart==null){
				filterChainStart = new RouteFilterDateTime(startDate);
				currentFilter = filterChainStart;
			}
			else{
				currentFilter.setNext(new RouteFilterDateTime(startDate));
				currentFilter = currentFilter.getNext();
			}
		}
		if(filter_category!=null){
			if(filterChainStart==null){
				filterChainStart = new RouteFilterCategory(category);
				currentFilter = filterChainStart;
			}
			else{
				currentFilter.setNext(new RouteFilterCategory(category));
				currentFilter = currentFilter.getNext();
			}
		}
		if(filter_traveltype!=null){
			if(filterChainStart==null){
				filterChainStart = new RouteFilterTraveltype(traveltype);
				currentFilter = filterChainStart;
			}
			else{
				currentFilter.setNext(new RouteFilterTraveltype(traveltype));
				currentFilter = currentFilter.getNext();
			}
		}
		if(filterChainStart!=null)
			return filterChainStart.filter(routes);
		return routes;
	}
	
	public static ArrayList<Route> filterRoutes(Context context, ArrayList<Route> routes){
		return filterRoutes(context, routes, filter_name, filter_publicOnly, filter_notFull, filter_ownOnly, filter_maxParts, filter_startDate, filter_category, filter_traveltype);
	}
	
	public static void setFilter(String name, boolean publicOnly, boolean notFull, boolean ownOnly, Integer maxParts, Date startDate, String category, String traveltype){
		filter_name = name;
		filter_publicOnly = publicOnly;
		filter_notFull = notFull;
		filter_ownOnly = ownOnly;
		filter_maxParts = maxParts;
		filter_startDate = startDate;
		filter_category = category;
		filter_traveltype = traveltype;
	}
}
