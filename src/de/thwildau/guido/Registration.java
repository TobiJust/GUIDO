package de.thwildau.guido;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;
import de.thwildau.guido.util.TextValidator;

/**
 * This class displays a registration form. The entered data (username,
 * email, password) is sent to the server. It observes {@link HttpConnector}
 * to be notified, when the data exchange finished.
 * @author GUIDO
 * @version 2013-12-14
 * @see Activity
 * @see Observer
 */
public class Registration extends Activity implements Observer {

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;

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
		setContentView(R.layout.activity_signup);
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Currently it is a single entry with no content.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}

	/**
	 * This method is called when the submit button is pressed in the view.
	 * It makes the progress dialog visible and gets the texts from the input 
	 * fields to assign them to {@link DatabaseInteractor#createUser(Activity, String, String, String)} 
	 * where the values are sent to the server.
	 * @param view The view which was pressed.
	 */
	public void submitRegistration(View view) {
		EditText usernameField = (EditText) findViewById(R.id.signup_nickname_edittext);
		EditText emailField = (EditText) findViewById(R.id.signup_email_edittext);
		EditText passwordField = (EditText) findViewById(R.id.signup_password_edittext);
		EditText passwordConfirmField = (EditText) findViewById(R.id.signup_password_repeat_edittext);
		String username = usernameField.getText().toString();
		String email = emailField.getText().toString();
		String password = passwordField.getText().toString();
		String passwordConfirm = passwordConfirmField.getText().toString();

		if(TextValidator.isNotEmpty(username) && TextValidator.isNotEmpty(email) && TextValidator.isNotEmpty(password) && TextValidator.isNotEmpty(passwordConfirm)) {
			if(TextValidator.isValidEmail(email)) {
				if(password.equals(passwordConfirm)) {
					progress = new ProgressDialog(this);
					progress.setCancelable(false);
					progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					progress.setIndeterminate(true);
					progress.show();
					DatabaseInteractor.createUser(this, username, email, password);
				}
			}
			else
				Toast.makeText(this, "Bitte eine gültige Email-Adresse eingeben", Toast.LENGTH_LONG).show();
		}
		else
			Toast.makeText(this, "Alle Felder sind Pflichtfelder", Toast.LENGTH_LONG).show();
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
		case Constants.RESP_REGISTRATION:
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			Toast.makeText(this, resp.getObject().toString(), Toast.LENGTH_LONG).show();
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
	
	/** 
	 * Called when the activity stops.
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
