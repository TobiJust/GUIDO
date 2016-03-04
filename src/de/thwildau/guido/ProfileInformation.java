package de.thwildau.guido;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.thwildau.guido.model.User;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;

/**
 * This Activity shows the user his profile information. It observes the {@link HttpConnector} to be informed
 * when data exchange finished.
 * @author Guido
 *
 */
public class ProfileInformation extends Activity implements Observer{
	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	ProgressDialog progress = null;
	TextView name, email, created, participated, rank, changeName;
	/**
	 * The user whoms profile is shown.
	 */
	User displayedUser;
	
	/**
	 * Called when the Activity is created. Inflates the layout and initializes
	 * variables. 
	 * @param savedInstanceState   If the activity is being re-initialized 
	 * after previously being shut down then this Bundle contains the data 
	 * it most recently supplied, otherwise its null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		setContentView(R.layout.activity_profile_information);
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * @param menu The options menu which contains the items. 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_menu_logout, menu);
		return true;
	}
	
	/**
	 * Called when the Activity gets visible to the user.
	 */
	@Override
	protected void onResume(){
		super.onResume();
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		displayedUser = (User) getIntent().getSerializableExtra("contact");
		if(displayedUser==null){
			progress.show();
			DatabaseInteractor.getUserInfo(this, PreferenceData.getUserId(this));
		}
		else{
			setUpData(false);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(progress != null){
			progress.dismiss();
			progress = null;
		}
	}
	
	/**
	 * Sets the Activities components data.
	 * @param isUserProfile True if it is a users profile.
	 */
	private void setUpData(boolean isUserProfile){
		Log.i("ISUSERPROFILE", ""+isUserProfile);
		name = (TextView)findViewById(R.id.profile_name_target);
		name.setText(displayedUser.getName());
		email = (TextView)findViewById(R.id.profile_email_target);
		email.setText(displayedUser.getEmail());
		created = (TextView)findViewById(R.id.profile_created_target);
		created.setText(String.valueOf(displayedUser.getCreated()));
		participated = (TextView)findViewById(R.id.profile_participated_target);
		participated.setText(String.valueOf(displayedUser.getParticipated()));
		rank = (TextView)findViewById(R.id.profile_rank_target);
		if((displayedUser.getParticipated()+displayedUser.getCreated())<10)
			rank.setText("Guido-Neuling");
		else if((displayedUser.getParticipated()+displayedUser.getCreated())<50)
			rank.setText("Guido-Kenner");
		else
			rank.setText("Guido-Veteran");
	}
	
	/**
	 * Called when the sign up button was pressed. This method starts 
	 * the {@link Registration} activity using an intent.
	 * @param view The view which was pressed.
	 */
	public void editName(View view) {
		AlertDialog.Builder addContactDialog = new AlertDialog.Builder(this);
		addContactDialog.setTitle("Namen ändern");
		final EditText nameInput = new EditText(this);
		nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
		addContactDialog.setView(nameInput);
		final Activity act = this;
		addContactDialog.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DatabaseInteractor.changeName(act, PreferenceData.getUserId(act), nameInput.getText().toString());
			}
		});
		addContactDialog.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				progress.hide();
				dialog.dismiss();
			}
		});
		AlertDialog diag = addContactDialog.create();
		diag.show();
	}
	
	/**
	 * This method is called whenever the observed object {@link HttpConnector}
	 * is changed via its notify() method when the data exchange is finished. 
	 * It passes the response gotten by the server to {@link ResponseHandler#handleResponse(Activity, String)}.
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
		case Constants.RESP_GET_USER_INFO:
			displayedUser = (User)resp.getObject();
			setUpData(true);
			break;
		case Constants.RESP_GET_CONTACT_INFO:
			displayedUser = (User)resp.getObject();
			setUpData(false);
			break;
		case Constants.RESP_CHANGE_PROFILE_NAME:
			Toast.makeText(this, resp.getObject().toString(), Toast.LENGTH_LONG).show();
			DatabaseInteractor.getUserInfo(this, PreferenceData.getUserId(this));
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
