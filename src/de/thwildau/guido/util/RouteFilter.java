package de.thwildau.guido.util;

import java.util.ArrayList;

import de.thwildau.guido.model.Route;

/**
 * Abstract implementation of a filter chain.
 * @author Guido
 */
abstract class RouteFilter {
    protected RouteFilter next;
    
    /**
     * Sets the next Filter
     * @param routeFilter
     */
    public void setNext(RouteFilter routeFilter) {
        next = routeFilter;
    }

    /**
     * Gets the next Filter
     * @return
     */
    public RouteFilter getNext(){
    	return next;
    }
    /**
     * Returns a filtered List of Route for the given List.
     * @param route The Routes to filter.
     * @return The filtered Routes.
     */
    abstract protected ArrayList<Route> filter(ArrayList<Route> route);
}
