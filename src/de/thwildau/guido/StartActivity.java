package de.thwildau.guido;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import de.thwildau.guido.gcm.GcmUtil;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;

/**
 * An full-screen activity that displays a logo and checks in the background
 * if the user is logged in. If the field user_id in the SharedPreferences is 
 * set the user id is sent to the server to check if the user is logged in.
 * The Activity observes the {@link HttpConnector} to be notified when the data
 * exchange finished and process its result in the {@link ResponseHandler}.
 * @author GUIDO
 * @version 2013-12-18
 * @see Activity
 * @see Observer
 */
public class StartActivity extends Activity implements Observer {
	Context context;
	String regid;
	
	/**
	 * Called when the activity is first created.
	 * Sets the content view and checks if the field user id in the SharedPreferences is set.
	 * If that is the case it communicates with the server if the user is logged in. If he is, 
	 * the {@link Lobby} is opened. If not the {@link Login} Activity is shown.
	 * @param savedInstanceState If the activity is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_start);

		if(PreferenceData.getUserId(this) != null) 
			DatabaseInteractor.requestLogin(this, PreferenceData.getUserId(this));

		context = getApplicationContext();
		Log.i("CHECKPLAYSERVICE", "checking play services");
		GcmUtil.setActivityAndContext(this, context);
		if(GcmUtil.checkPlayServices()){
			regid = GcmUtil.getRegistrationId(context);
			Log.i("REGID", regid);
			if (regid.isEmpty()) {
				GcmUtil.registerInBackground();
			}
			Log.i("REGIDAFTER", regid);
			if(PreferenceData.getUserId(this) != null) {
				DatabaseInteractor.requestLogin(this, PreferenceData.getUserId(this));
			}
			else if(PreferenceData.getUserId(this) == null){
				Intent login = new Intent(this, Login.class);
				startActivity(login);
			}
		}
		else {
			Log.i("PLAYSERVICES", "No valid Google Play Services APK found.");
		}
	}

	/**
	 * Called when the Activity gets visible to the user. Checks the Play Services.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		GcmUtil.checkPlayServices();
	}

	/**
	 * Called when the observable ({@link HttpConnector}) notifies this class.
	 * It passes the servers response to {@link ResponseHandler#handleResponse(android.content.Context, String)}
	 * @param oberservable The observable object.
	 * @param data An argument passed to the notifyObservers method.
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(DatabaseInteractor.getResponse()==null){
			Toast.makeText(this, "Netzwerkprobleme...", Toast.LENGTH_LONG).show();
			return;
		}
		Response resp = ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
		Intent intent = null;
		switch(resp.getId()) {
		case Constants.RESP_LOGGED_IN:
			// if user is logged in open the Lobby Activity
			if((Integer)resp.getObject() == 1) 
				intent = new Intent(this, Lobby.class);
			// if the user is not logged in open the Login Activity
			else if((Integer)resp.getObject() == 0)
				intent = new Intent(this, Login.class);
			startActivity(intent);
			break;
		case Constants.RESP_ERROR:
			Builder alert = new AlertDialog.Builder(this);
			GuidoError err = (GuidoError)resp.getObject();
			alert.setTitle("Error #"+err.getErrorCode());
			alert.setMessage(err.getMessage());
			alert.setPositiveButton("Ok", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog alertDiag = alert.create();
			alertDiag.show();
			break;
		}
	}
}
