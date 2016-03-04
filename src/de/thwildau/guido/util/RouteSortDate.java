package de.thwildau.guido.util;

import java.util.Comparator;

import de.thwildau.guido.model.Route;

/**
 * This utility class sorts two given Routes by date.
 * @author GUIDO
 * @version 2013-12-20
 * @see Comparator
 */
public class RouteSortDate implements Comparator<Route> {

	@Override
	public int compare(Route lhs, Route rhs) {
		if(lhs.getDate().before(rhs.getDate())){
			return -1;
		}
		else{
			return 1;
		}
	}

}
