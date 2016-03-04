package de.thwildau.guido.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Stores information in the applications SharedPreferences.
 * @author GUIDO
 * @version 2013-12-18
 */
public class PreferenceData {

	/**
	 * Gets the SharedPreferences for the given context.
	 * @param ctx The calling Activities context.
	 * @return The applications SharedPreferences.
	 */
	public static SharedPreferences getSharedPreferences(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	/**
	 * Writes the given user id of the logged in user to the SharedPreferences.
	 * @param ctx The calling Activities context.
	 * @param userId The user id to write.
	 */
	public static void setUserLoggedIn(Context ctx, String userId) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.putString(Constants.PREF_USER_ID, userId);
		editor.commit();
	}

	/**
	 * Gets the user id from the SharedPreferences or null if there is no id stored.
	 * @param ctx The calling Activities context.
	 * @return The user id or null if there is no stored in the SharedPreferences
	 */
	public static String getUserId(Context ctx) {
		return getSharedPreferences(ctx).getString(Constants.PREF_USER_ID, null);
	}
	
	/**
	 * Puts the active Routes id to the SharedPreferences.
	 * @param ctx The calling Activities context.
	 * @param routeId The active Routes id.
	 */
	public static void setActiveRoute(Context ctx, String routeId) {
		Editor editor = getSharedPreferences(ctx).edit();
		editor.putString(Constants.PREF_ACTIVE_ROUTE, routeId);
		editor.commit();
	}
	
	/**
	 * Gets the active Routes id from the SharedPreferences.
	 * @param ctx The calling Activities context.
	 * @return The active Routes id.
	 */
	public static String getActiveRoute(Context ctx) {
		return getSharedPreferences(ctx).getString(Constants.PREF_ACTIVE_ROUTE, null);
	}
}