package de.thwildau.guido.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import de.thwildau.guido.ActiveRoute;
import de.thwildau.guido.Login;
import de.thwildau.guido.model.Message;
import de.thwildau.guido.model.POI;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.model.User;

/**
 * This class handles the response gotten from the web server. 
 * @author GUIDO
 * @version 2013-12-18
 */
public class ResponseHandler {

	/**
	 * Convert the assigned response into a JSON object and process it
	 * appropriate by switching through the various response constants.
	 * @param act The calling Activity.
	 * @param result The result from the web server.
	 */
	public static Response handleResponse(Context context, String response) {
		Log.i("RESPONSE", response);
		Response responseObj = new Response();
		JSONObject json = null;
		Intent intent = null;
		User creator = null;
		try {
			// Convert the given response to a JSON object
			json = new JSONObject(response);
			responseObj.setId(json.getInt("id"));
			// switch through the different response constants
			switch(responseObj.getId()) {
			case Constants.RESP_ERROR:
				// error returned from server, display the assigned message
				// in an alert dialog
				GuidoError guidoError = new GuidoError(json.getString("message"), json.getInt("error_code"));
				responseObj.setObject(guidoError);
				break;
			case Constants.RESP_REGISTRATION:
				responseObj.setObject(json.getString("message"));
				break;
			case Constants.RESP_LOGIN:
				responseObj.setObject(json.getString("user_id"));
				break;
			case Constants.RESP_GET_MESSAGE:
				ArrayList<Message> messages = new ArrayList<Message>();
				JSONArray messageArray = json.getJSONArray("messages");
				for(int i=0; i<messageArray.length(); i++) {
					Message m = new Message();
					JSONObject row = messageArray.getJSONObject(i);
					m.setId(row.getString("id"));
					m.setSubject(row.getString("subject"));
					m.setMessage(row.getString("content"));
					m.setFromEmail(row.getString("from"));
					messages.add(m);
				}
				responseObj.setObject(messages);
				break;
			case Constants.RESP_SEND_MESSAGE:
				responseObj.setObject(json.getString("message"));
				break;
			case Constants.RESP_CREATE_ROUTE:
				responseObj.setObject(json.getString("message"));
				break;
			case Constants.RESP_START_ROUTE:
				break;
			case Constants.RESP_JOIN_PUBLIC:
				responseObj.setObject(json.getString("route_id"));
				break;
			case Constants.RESP_JOIN_PRIVATE:
				responseObj.setObject(json.getString("route_id"));
				break;
			case Constants.RESP_GET_POS_ALL:
				JSONArray participatorPos = json.getJSONArray("participators");
				ArrayList<User> partPos = new ArrayList<User>();
				for(int i=0; i<participatorPos.length(); i++) {
					JSONObject row = participatorPos.getJSONObject(i);
					User u = new User();
					u.setId(row.getString("id"));
					u.setName(row.getString("name"));
					u.setEmail(row.getString("e-mail"));
					u.setLat(row.getDouble("lat"));
					u.setLng(row.getDouble("lng"));
					partPos.add(u);
				}
				responseObj.setObject(partPos);
				break;
			case Constants.RESP_GET_POS_GUIDO:
				JSONArray guidoPos = json.getJSONArray("guide");
				ArrayList<User> guidPos = new ArrayList<User>();
				for(int i=0; i<guidoPos.length(); i++) {
					JSONObject row = guidoPos.getJSONObject(i);
					User u = new User();
					u.setId(row.getString("id"));
					u.setLat(row.getDouble("lat"));
					u.setLng(row.getDouble("lng"));
					guidPos.add(u);
				}
				responseObj.setObject(guidPos);
				break;
			case Constants.RESP_SEND_POS:
				break;
			case Constants.RESP_END_ROUTE:
				break;
			case Constants.RESP_LEAVE_ROUTE:
				responseObj.setObject(json.getString("route_id"));
				break;
			case Constants.RESP_GET_ROUTE_DETAILS:
				Route detailRoute = new Route();
				JSONObject details = json.getJSONObject("details");
				detailRoute.setId(details.getString("id"));
				detailRoute.setName(details.getString("name"));
				detailRoute.setDescription(details.getString("desc"));
				detailRoute.setMaxPart(Integer.parseInt(details.getString("maxparticipators")));
				detailRoute.setCurrPart(Integer.parseInt(details.getString("currentparticipators")));
				if(details.getInt("password") == 0)
					detailRoute.setPassword(false);
				else
					detailRoute.setPassword(true);
				creator = new User();
				creator.setId(details.getString("creator_id"));
				creator.setName(details.getString("creator_name"));
				detailRoute.setCreator(creator);
				try {
					detailRoute.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMAN).parse(details.getString("date")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				detailRoute.setTravelType(Integer.parseInt(details.getString("traveltype")));
				detailRoute.setCategory(details.getString("category"));
				JSONArray pois = details.getJSONArray("pois");
				ArrayList<POI> poiList = new ArrayList<POI>();
				for(int i=0; i<pois.length(); i++) {
					JSONObject row = pois.getJSONObject(i);
					POI poi = new POI();
					poi.setId(row.getString("id"));
					poi.setName(row.getString("name"));
					poi.setDescription(row.getString("desc"));
					poi.setLat(Double.parseDouble(row.getString("lat")));
					poi.setLng(Double.parseDouble(row.getString("lng")));
					poiList.add(poi);
				}
				detailRoute.setPois(poiList);
				
				JSONArray participators = details.getJSONArray("participators");
				ArrayList<User> parts = new ArrayList<User>();
				for(int i=0; i<participators.length(); i++) {
					JSONObject row = participators.getJSONObject(i);
					User u = new User();
					u.setId(row.getString("id"));
					u.setName(row.getString("name"));
					parts.add(u);
				}
				detailRoute.setParticipators(parts);
				responseObj.setObject(detailRoute);
				break;
			case Constants.RESP_LIST_ROUTES:
				// read the List of Routes from the response JSON file
				ArrayList<Route> routes = new ArrayList<Route>();
				JSONArray array = json.getJSONArray("routes");
				for(int i=0; i<array.length(); i++) {
					Route r = new Route();
					JSONObject row = array.getJSONObject(i);
					r.setId(row.getString("id"));
					r.setName(row.getString("name"));
					r.setDescription(row.getString("desc"));
					r.setMaxPart(Integer.parseInt(row.getString("maxparticipators")));
					r.setCurrPart(Integer.parseInt(row.getString("currentparticipators")));
					r.setCategory(row.getString("category"));
					r.setTravelType(Integer.parseInt(row.getString("traveltype")));
					if(row.getInt("password") == 0)
						r.setPassword(false);
					else
						r.setPassword(true);
					creator = new User();
					creator.setId(row.getString("creator_id"));
					creator.setName(row.getString("creator_name"));
					r.setCreator(creator);
					try {
						r.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMAN).parse(row.getString("date")));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					routes.add(r);
				}
				responseObj.setObject(routes);
				break;
			case Constants.RESP_LOGOUT:
				// user is logged out, return to Login Activity
				if(PreferenceData.getActiveRoute(context) != null)
					ActiveRoute.cancelAlarm();
				PreferenceData.setUserLoggedIn(context, null);
				PreferenceData.setActiveRoute(context, null);
				intent = new Intent(context, Login.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				break;
			case Constants.RESP_LOGGED_IN:
				responseObj.setObject(Integer.valueOf(json.getInt("status")));
				break;
			case Constants.RESP_DELETE_MESSAGE:
				responseObj.setObject(json.getString("message"));
				break;
			case Constants.RESP_GET_USER_INFO:
				User u = new User();
				u.setName(json.getString("name"));
				u.setCreated(json.getInt("created"));
				u.setParticipated(json.getInt("participated"));
				u.setEmail(json.getString("email"));
				responseObj.setObject(u);
				break;
			case Constants.RESP_GET_CONTACT_INFO:
				User contactInfo = new User();
				contactInfo.setName(json.getString("name"));
				contactInfo.setCreated(json.getInt("created"));
				contactInfo.setParticipated(json.getInt("participated"));
				contactInfo.setEmail(json.getString("email"));
				responseObj.setObject(contactInfo);
				break;
			case Constants.RESP_GET_CONTACTS:
				ArrayList<User> contactList = new ArrayList<User>();
				JSONArray contactArray = json.getJSONArray("contacts");
				for(int i=0; i<contactArray.length(); i++) {
					User contact = new User();
					JSONObject row = contactArray.getJSONObject(i);
					contact.setId(row.getString("id"));
					contact.setName(row.getString("name"));
					contact.setEmail(row.getString("email"));
					contactList.add(contact);
				}
				responseObj.setObject(contactList);
				break;
			case Constants.RESP_ADD_CONTACT:
				responseObj.setObject(json.getString("message"));
				break;
			case Constants.RESP_DELETE_CONTACT:
				responseObj.setObject(json.getString("message"));
				break;
			case Constants.RESP_CHANGE_PROFILE_NAME:
				responseObj.setObject(json.getString("message"));
				break;
			}
			return responseObj;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
