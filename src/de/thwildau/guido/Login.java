package de.thwildau.guido;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import de.thwildau.guido.gcm.GcmUtil;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;
import de.thwildau.guido.util.TextValidator;

/**
 * This Activity displays a login form and a button to start the
 * {@link Registration}. When pressing the submit button the entered
 * data is sent to the web server. It observes {@link HttpConnector}
 * to be informed when the data exchange finished.
 * @author GUIDO
 * @version 2013-12-18
 * @see Activity
 * @see Observer
 */
public class Login extends Activity implements Observer {

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;
	
	/**
	 * Counter how often the back button was pressed.
	 */
	private static int backButtonCount = 0;

	/**
	 * Called when the activity is first created. Sets the content view with layout xml
	 * and initializes the progress bar, which will be displayed later.
	 * @param savedInstanceState If the activity is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/**
	 * Called when the back button was pressed. If it was pressed twice the
	 * application is minimized and the device returns to the home screen. 
	 */
	@Override
	public void onBackPressed() {
		if(backButtonCount >= 1) {
			backButtonCount = 0;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		else {
			Toast.makeText(this, "Noch einmal drücken, um Anwendung zu schließen", Toast.LENGTH_SHORT).show();
			backButtonCount++;
		}
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Currently it is a single entry with no content.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * This method is called when the submit button is pressed in the view.
	 * It makes the progress dialog visible and gets the texts from the input 
	 * fields to assign them to {@link DatabaseInteractor#login(Activity, String, String)} 
	 * where the values are sent to the server.
	 * @param view The view which was pressed.
	 */
	public void submitLogin(View view) {
		backButtonCount = 0;
		EditText emailField = (EditText) findViewById(R.id.login_email_edittext);
		EditText passwordField = (EditText) findViewById(R.id.login_password_edittext);
		String email = emailField.getText().toString();
		String password = passwordField.getText().toString();
		if(TextValidator.isNotEmpty(email) && TextValidator.isValidEmail(email)) {
			if(TextValidator.isNotEmpty(password)) {
				
				progress = new ProgressDialog(this);
				progress.setCancelable(false);
				progress.show();				
				GcmUtil.setActivityAndContext(this, getApplicationContext());
				DatabaseInteractor.login(this, email, password);
			}
			else
				Toast.makeText(this, "Bitte ein Passwort eingeben", Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(this, "Bitte eine gültige Email-Adresse eingeben", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Called when the sign up button was pressed. This method starts 
	 * the {@link Registration} activity using an intent.
	 * @param view The view which was pressed.
	 */
	public void startRegistration(View view) {
		backButtonCount = 0;
		Intent registerIntent = new Intent(Login.this, Registration.class);
		startActivity(registerIntent);
	}

	/**
	 * This method is called whenever the observed object {@link HttpConnector}
	 * is changed via its notify() method when the data exchange is finished. 
	 * It hides the progress bar and passes the response gotten by the server
	 * to {@link ResponseHandler#handleResponse(Activity, String)}.
	 * @param oberservable The observable object.
	 * @param data An argument passed to the notifyObservers method.
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(progress != null)
			progress.hide();
		if(DatabaseInteractor.getResponse()==null){
			Toast.makeText(this, "Netzwerkprobleme...", Toast.LENGTH_LONG).show();
			return;
		}
		Response resp = ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
		switch(resp.getId()) {
		case Constants.RESP_LOGIN:	
			// user logged in, save the id in the SharedPreferences and open the lobby.
			PreferenceData.setUserLoggedIn(this, resp.getObject().toString());
			Intent intent = new Intent(this, Lobby.class);
			startActivity(intent);
			break;
		case Constants.RESP_ERROR:
			Builder alert = new AlertDialog.Builder(this);
			GuidoError err = (GuidoError)resp.getObject();
			final Activity act = this;
			if(err.getErrorCode()==2){
				alert.setTitle(err.getMessage());
				alert.setMessage("Login erzwingen?");
				alert.setPositiveButton("Ok", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText emailField = (EditText) findViewById(R.id.login_email_edittext);
						EditText passwordField = (EditText) findViewById(R.id.login_password_edittext);
						DatabaseInteractor.forceLogin(act, emailField.getText().toString(), passwordField.getText().toString());
						dialog.dismiss();
					}
				});
				alert.setNegativeButton("Abbrechen", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}
			else{
				alert.setTitle("Error #"+err.getErrorCode());
				alert.setMessage(err.getMessage());
				alert.setPositiveButton("Ok", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}
			AlertDialog alertDiag = alert.create();
			alertDiag.show();
			break;
		}
	}
	
	/**
	 * Called when the Activity stops.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		if(progress != null){
			progress.dismiss();
			progress = null;
		}
	}
}
