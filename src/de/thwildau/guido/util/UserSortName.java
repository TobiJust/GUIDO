package de.thwildau.guido.util;

import java.util.Comparator;

import de.thwildau.guido.model.User;

/**
 * This utility class sorts two given Users by name.
 * @author GUIDO
 * @version 2013-12-20
 * @see Comparator
 */
public class UserSortName implements Comparator<User> {

	@Override
	public int compare(User lhs, User rhs) {
		return lhs.getName().compareTo(rhs.getName());
	}

}
