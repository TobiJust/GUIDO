package de.thwildau.guido.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import de.thwildau.guido.gcm.GcmUtil;
import de.thwildau.guido.model.Message;
import de.thwildau.guido.model.POI;
import de.thwildau.guido.model.Route;

/**
 * This class prepares data for interacting with the database.
 * It sets the key value pairs dependent of different use cases.
 * @author GUIDO
 * @version 2013-12-20
 */
public class DatabaseInteractor {

	/**
	 * The response of a data exchange.
	 */
	private static String response;

	/**
	 * This method checks if a data communication is available 
	 * on the device and if so starts the transmission by calling
	 * {@link HttpConnector#executeTask()}. It also adds the 
	 * assigned Activity as an observer to the HttpConnector.
	 * @param act The assigned Activity.
	 * @param keyValuePairs The data which is supposed to be exchanged.
	 */
	private static void sendRequest(Activity act, String url, List<NameValuePair> keyValuePairs) {
		Log.i("DATABASEINTERACTOR", "Sending request to... "+url);
		ConnectivityManager connMgr = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			HttpConnector httpConn = new HttpConnector(url, keyValuePairs);
			httpConn.addObserver((Observer) act);
			httpConn.executeTask();
		}
		else{
			Toast.makeText(act, "Netzwerkfehler...", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Called when a new user is created.
	 * Adds the given parameters to a list of key value pairs to prepare 
	 * them for transmission. The assigned password will be hashed in 
	 * SHA-256. It calls sendRequest to start data exchange.
	 * @param act The calling Activity
	 * @param name User name
	 * @param email Users email address
	 * @param password Users password
	 */
	public static void createUser(Activity act, String name, String email, String password) {
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user", name));
		nameValuePairs.add(new BasicNameValuePair("email", email));
		nameValuePairs.add(new BasicNameValuePair("pass", encryptPassword(password)));

		sendRequest(act, Constants.URL_SECURE + Constants.URL_BASE + Constants.URL_CREATE_USER, nameValuePairs);
	}

	/**
	 * Called when a user logins.
	 * Adds the given parameters to a list of key value pairs to prepare 
	 * them for transmission. The assigned password will be hashed in 
	 * SHA-256. It calls sendRequest to start data exchange.
	 * @param act The calling Activity
	 * @param email The users email address
	 * @param password The users password
	 */
	public static void login(Activity act, String email, String password) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("email", email));
		nameValuePairs.add(new BasicNameValuePair("pass", encryptPassword(password)));
		nameValuePairs.add(new BasicNameValuePair("reg_id", GcmUtil.getRegistrationId(act.getApplicationContext())));

		sendRequest(act, Constants.URL_SECURE + Constants.URL_BASE + Constants.URL_LOGIN, nameValuePairs);
	}

	/**
	 * Called when a user forces the login.
	 * Adds the given parameters to a list of key value pairs to prepare 
	 * them for transmission. The assigned password will be hashed in 
	 * SHA-256. It calls sendRequest to start data exchange.
	 * @param act The calling Activity
	 * @param email The users email address
	 * @param password The users password
	 */
	public static void forceLogin(Activity act, String email, String password) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("email", email));
		nameValuePairs.add(new BasicNameValuePair("pass", encryptPassword(password)));
		nameValuePairs.add(new BasicNameValuePair("reg_id", GcmUtil.getRegistrationId(act.getApplicationContext())));

		sendRequest(act, Constants.URL_SECURE + Constants.URL_BASE + Constants.URL_FORCE_LOGIN, nameValuePairs);
	}

	/**
	 * Called when a Route is created.
	 * Adds the given parameters to a list of key value pairs to prepare 
	 * them for transmission. It calls sendRequest to start data exchange.
	 * TODO: Don't create dummy POI but add them when creating the passed Route.
	 * @param act The calling Activity
	 * @param userId The creators id.
	 * @param route The created Route.
	 */
	public static void createRoute(Activity act, String userId, Route route, String password) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		JSONObject poiJson = new JSONObject();
		int i = 0;
		for(POI poi : route.getPois()) {
			JSONObject poiObject = new JSONObject();
			try {
				poiObject.put("name", poi.getName());
				poiObject.put("desc", poi.getDescription());
				poiObject.put("lat", poi.getLat());
				poiObject.put("lng", poi.getLng());
				poiJson.put(i+"", poiObject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			i++;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.GERMAN);
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_name", route.getName()));
		nameValuePairs.add(new BasicNameValuePair("route_desc", route.getDescription()));
		nameValuePairs.add(new BasicNameValuePair("max_part", route.getMaxPart()+""));
		nameValuePairs.add(new BasicNameValuePair("pois", poiJson.toString()));
		nameValuePairs.add(new BasicNameValuePair("traveltype", route.getTravelType()+""));
		nameValuePairs.add(new BasicNameValuePair("category", route.getCategory()));
		nameValuePairs.add(new BasicNameValuePair("date", sdf.format(route.getDate())));
		if(password != null)
			nameValuePairs.add(new BasicNameValuePair("pass", encryptPassword(password)));

		sendRequest(act, Constants.URL_SECURE + Constants.URL_BASE + Constants.URL_CREATE_ROUTE, nameValuePairs);
	}

	/**
	 * A request do get all created Routes from the server.
	 * @param act The calling Activity.
	 * @param userId The user who requests the List of routes.
	 */
	public static void getRoutes(Activity act, String userId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_GET_ROUTES, nameValuePairs);
	}

	/**
	 * Requests at the server is a user is logged in.
	 * @param act The calling Activity.
	 * @param userId The user id of the user to check.
	 */
	public static void requestLogin(Activity act, String userId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_LOGIN_REQUEST, nameValuePairs);
	}

	/**
	 * Sends a logout request to the server.
	 * @param act The calling Activity.
	 * @param userId The users user id.
	 */
	public static void logout(Activity act, String userId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_LOGOUT, nameValuePairs);
	}

	/**
	 * Called when a users messages are requested.
	 * Users id is added to a list containing key value pairs to 
	 * identify the user. It calls sendRequest to start data exchange.
	 * @param act The calling Activity
	 * @param id The users id
	 */
	public static void getMessages(Activity act, String id) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", id));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_GET_MESSAGES, nameValuePairs);
	}

	/**
	 * Called when a user sends a message.
	 * The {@link Message} parameters are added to a list containing 
	 * key value pairs. It calls sendRequest to start data exchange.
	 * @param act The calling Activity
	 * @param message The users message
	 */
	public static void sendMessage(Activity act, String id, Message message) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", id));
		nameValuePairs.add(new BasicNameValuePair("to_email", message.getToEmail()));
		nameValuePairs.add(new BasicNameValuePair("subject", message.getSubject()));
		nameValuePairs.add(new BasicNameValuePair("content", message.getMessage()));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_SEND_MESSAGE, nameValuePairs);
	}

	/**
	 * Called when a guide starts a Route.
	 * Users and Routes id are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 */
	public static void startRoute(Activity act, String userId, String routeId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_id", routeId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_START_ROUTE, nameValuePairs);
	}

	/**
	 * Called when a user joins a public Route.
	 * Users and Routes id are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 */
	public static void joinPublic(Activity act, String userId, String routeId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_id", routeId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_JOIN_PUBLIC, nameValuePairs);
	}

	/**
	 * Called when a user joins a private Route.
	 * Users and Routes id and the password are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 * @param password The password to join the Route.
	 */
	public static void joinPrivate(Activity act, String userId, String routeId, String password) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_id", routeId));
		nameValuePairs.add(new BasicNameValuePair("pass", encryptPassword(password)));

		sendRequest(act, Constants.URL_SECURE + Constants.URL_BASE + Constants.URL_JOIN_PRIVATE, nameValuePairs);
	}

	/**
	 * Called to get the position of all participators.
	 * Users and Routes id and the password are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 */
	public static void getPosAll(Activity act, String userId, String routeId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_id", routeId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_POSITION_ALL, nameValuePairs);
	}

	/**
	 * Called to get the position of the guide.
	 * Params are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 */
	public static void getPosGuido(Activity act, String userId, String routeId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_id", routeId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_POSITION_GUIDO, nameValuePairs);
	}

	/**
	 * Called to send the users position.
	 * Users and Routes id and the password are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 */
	public static void sendPosition(Activity act, String userId, double lat, double lng) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		nameValuePairs.add(new BasicNameValuePair("lng", String.valueOf(lng)));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_SEND_POSITION, nameValuePairs);
	}

	/**
	 * Called to end a Route.
	 * Users and Routes id and the password are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 */
	public static void endRoute(Activity act, String userId, String routeId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_id", routeId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_END_ROUTE, nameValuePairs);
	}

	/**
	 * Called when the user leaves a Route.
	 * Users and Routes id and the password are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 */
	public static void leaveRoute(Activity act, String userId, String routeId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_id", routeId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_LEAVE_ROUTE, nameValuePairs);
	}

	/**
	 * Called to request the details for a public Route.
	 * Users and Routes id and the password are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param routeId The Routes id.
	 */
	public static void getRouteDetails(Activity act, String userId, String routeId) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("route_id", routeId));

		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_ROUTE_DETAILS, nameValuePairs);
	}

	/**
	 * Called to delete a message.
	 * Users and Messages id are added to a list containing key value pairs. 
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param messageId The Messages id.
	 */
	public static void deleteMessage(Activity act, String userId, String messageId){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("message_id", messageId));
		
		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_DELETE_MESSAGE, nameValuePairs);
	}
	
	/**
	 * Called to get a Users information.
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 */
	public static void getUserInfo(Activity act, String userId){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_USER_INFO, nameValuePairs);
	}
	
	/**
	 * Called to get a Users contacts.
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 */
	public static void getContacts(Activity act, String userId){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_CONTACTS, nameValuePairs);
	}
	
	/**
	 * Called to get a Users contact information.
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param email The contacts email address.
	 */
	public static void getContactInfo(Activity act, String userId, String email){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("contact_email", email));
		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_CONTACT_INFO, nameValuePairs);
	}
	
	/**
	 * Called to add a new contact.
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param email The contacts email address.
	 */
	public static void addContact(Activity act, String userId, String email){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("contact_email", email));
		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_ADD_CONTACT, nameValuePairs);
	}
	
	/**
	 * Called to delete a contact.
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param email The contacts email address.
	 */
	public static void deleteContact(Activity act, String userId, String email){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("contact_email", email));
		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_DELETE_CONTACT, nameValuePairs);
	}
	
	/**
	 * Called to change the users name.
	 * It calls sendRequest to start data exchange.
	 * @param act The calling Activity.
	 * @param userId The users id.
	 * @param name The users new name.
	 */
	public static void changeName(Activity act, String userId, String name){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_id", userId));
		nameValuePairs.add(new BasicNameValuePair("name", name));
		sendRequest(act, Constants.URL_STANDARD + Constants.URL_BASE + Constants.URL_CHANGE_PROFILE_NAME, nameValuePairs);
	}
	
	/**
	 * Get a data exchanges response.
	 * @return The data exchanges response.
	 */
	public static String getResponse() {
		return response;
	}

	/**
	 * Set a data exchanges response.
	 * @param result The data exchanges response.
	 */
	public static void setResponse(String response) {
		DatabaseInteractor.response = response;
	}
	
	/**
	 * Encrypts the assigned password in SHA-256.
	 * @param password The password to encrypt.
	 * @return A hashed string of the assigned password.
	 */
	public static String encryptPassword(String password){
		MessageDigest cript = null;
		try {
			cript = MessageDigest.getInstance("SHA-256");
			cript.reset();
			cript.update(password.getBytes("utf8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] b = cript.digest();
		StringBuffer sb = new StringBuffer(b.length * 2);
	     for (int i = 0; i < b.length; i++){
	       int v = b[i] & 0xff;
	       if (v < 16) {
	         sb.append('0');
	       }
	       sb.append(Integer.toHexString(v));
	     }
	     return sb.toString().toUpperCase();
	  }
}
