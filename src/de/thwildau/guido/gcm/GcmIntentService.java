package de.thwildau.guido.gcm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.thwildau.guido.ActiveRoute;
import de.thwildau.guido.MessageOverview;
import de.thwildau.guido.R;
import de.thwildau.guido.model.POI;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.model.User;
import de.thwildau.guido.util.Constants;

public class GcmIntentService extends IntentService{
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM
			 * will be extended in the future with new message types, just ignore
			 * any message types you're not interested in, or that you don't
			 * recognize.
			 */
			try{
				if (GoogleCloudMessaging.
						MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
					sendNotification("Send error: " + extras.toString());
				} else if (GoogleCloudMessaging.
						MESSAGE_TYPE_DELETED.equals(messageType)) {
					sendNotification("Deleted messages on server: " +
							extras.toString());
					// If it's a regular GCM message, do some work.
				} else if (GoogleCloudMessaging.
						MESSAGE_TYPE_MESSAGE.equals(messageType)) {
					Log.i("SERVICECOMPLETE", "Completed work @ " + SystemClock.elapsedRealtime());
					// Post notification of received message.
					sendNotification(extras.getString("message"));
					Log.i("SERVICERECEIVED", "Received: " + extras.getString("message"));
				}
			}catch(JSONException jse){
				Log.e("INTENTSERVICE", "JSON EXCEPTION");
				jse.printStackTrace();
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) throws JSONException {
		JSONObject message = null;
		message = new JSONObject(msg);
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.InboxStyle inboxStyle =
				new NotificationCompat.InboxStyle();
		switch(message.getInt("id")){
		case Constants.RESP_SEND_MESSAGE:
			PendingIntent messageOverviewIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, MessageOverview.class), PendingIntent.FLAG_CANCEL_CURRENT);
			NotificationCompat.Builder messageReceivedBuilder =
					new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setDefaults(Notification.DEFAULT_ALL);
			inboxStyle.setBigContentTitle(message.getString("message_head"));
			inboxStyle.addLine(Html.fromHtml("<b>"+message.getString("subject")+"</b>"));
			inboxStyle.addLine(message.getString("message").length()>60?
					message.getString("message").substring(0, 59)+"..."
					:message.getString("message"));
			messageReceivedBuilder.setStyle(inboxStyle);
			messageReceivedBuilder.setContentIntent(messageOverviewIntent);
			messageReceivedBuilder.setAutoCancel(true);
			mNotificationManager.notify(Constants.NOT_MESSAGE_RECEIVED, messageReceivedBuilder.build());
			break;
		case Constants.RESP_START_ROUTE:
			Route r = getRouteDetails(message.getString("route"));
			Intent intent = new Intent(this, ActiveRoute.class);
			intent.putExtra("route", r);
			PendingIntent activeRouteIntent = PendingIntent.getActivity(this, 0, intent
					, PendingIntent.FLAG_ONE_SHOT);
			NotificationCompat.Builder routeStartedBuilder =
					new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(message.getString("message_head"))
			.setStyle(new NotificationCompat.BigTextStyle()
			.bigText(message.getString("message")))
			.setContentText(message.getString("message"))
			.setDefaults(Notification.DEFAULT_ALL);
			routeStartedBuilder.setContentIntent(activeRouteIntent);
			routeStartedBuilder.setAutoCancel(true);
			mNotificationManager.notify(Constants.NOT_ROUTE_STARTED, routeStartedBuilder.build());
			break;
		case Constants.RESP_END_ROUTE:
			Log.i("END ROUTE", "ENDING ROUTE");
			ActiveRoute.leaveRoute();
			break;
		}

	}

	public Route getRouteDetails(String message) {
		Route detailRoute = new Route();
		Log.i("ROUTEDETAILS", message);
		JSONObject details;
		try {
			JSONObject json = new JSONObject(message);
			details = json.getJSONObject("details");
			detailRoute.setId(details.getString("id"));
			detailRoute.setName(details.getString("name"));
			detailRoute.setDescription(details.getString("desc"));
			detailRoute.setMaxPart(Integer.parseInt(details.getString("maxparticipators")));
			detailRoute.setCurrPart(Integer.parseInt(details.getString("currentparticipators")));
			if(details.getInt("password") == 0)
				detailRoute.setPassword(false);
			else
				detailRoute.setPassword(true);
			User creator = new User();
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
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return detailRoute;
	}
}