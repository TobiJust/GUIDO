package de.thwildau.guido.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.util.Log;
import de.thwildau.guido.ActiveRoute;

/**
 * This custom BroadCastReceiver registers an AlarmManager to get regular updates on the
 * other users position data. It is implemented in the Singleton design pattern.
 * @author Guido
 * @see BroadcastReceiver
 */
public class LocationBroadcastReceiver extends BroadcastReceiver {

	/**
	 * Private constructor for the Singleton design pattern.
	 */
	public LocationBroadcastReceiver() {}

	/**
	 * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
	 * It triggers {@link ActiveRoute#getPosAll()}.
	 * @param context The Context in which the receiver is running. 
	 * @param intent The Intent being received.  
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();
		try {
			ActiveRoute.getPosAll();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			wl.release();
		}
	}

	/**
	 * Initializes the AlarmManager and sends the Intent to receive in onReceive.
	 * @param context The calling Activities context.
	 */
	public void setAlarm(Context context) {
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, LocationBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 8000, pi); // Millisec * Second * Minute
	}

	/**
	 * Cancels the registered alarms.
	 * @param context The calling Activities context.
	 */
	public void cancelAlarm(Context context) {
		ComponentName receiver = new ComponentName(context, LocationBroadcastReceiver.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		        PackageManager.DONT_KILL_APP);
		Intent intent = new Intent(context, LocationBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}
}
