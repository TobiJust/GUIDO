package de.thwildau.guido;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import de.thwildau.guido.model.Message;
import de.thwildau.guido.model.User;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;

/**
 * An Activity displaying the selected Messages details.
 * @author GUIDO
 * @version 2013-12-16
 * @see Activity
 */
public class MessageDetail extends Activity implements Observer {

	/**
	 * The Message which details are displayed
	 */
	 Message displayedMessage;

	 /**
	  * Fragment to create a new message.
	  */
	 MessageCreatorFragment createMessageDialog;
	/**
	 * Called when the activity is first created. Sets the content view with layout xml
	 * and sets the Route which was put as extra to the intent.
	 * @param savedInstanceState If the activity is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_detail);
		displayedMessage = (Message) getIntent().getSerializableExtra("message");
		setupData();
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Shows a dummy settings button and a logout button which logs the user out.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_menu_messagedetail, menu);
		return true;
	}

	/**
	 * Called when a menu item was selected. If the logout item was selected,
	 * the user will be logged out.
	 * @param item The selected menu item.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			DatabaseInteractor.logout(this, PreferenceData.getUserId(this));
			break;
		case R.id.action_sendMessagerw:
			Log.i("CREATEMESSAGE", "create button clicked");
			TextView messageFrom = (TextView) findViewById(R.id.message_from);
			TextView messageSubject = (TextView) findViewById(R.id.message_subject);
			createMessageDialog = new MessageCreatorFragment();
			// Supply num input as an argument.
		    Bundle args = new Bundle();
		    args.putString("toEmail", messageFrom.getText().toString());
		    args.putString("subject", messageSubject.getText().toString());
			args.putBoolean("response", true);
		    createMessageDialog.setArguments(args);
			createMessageDialog.show(getFragmentManager(), "Create Message");
			
			
			
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * Sets the Routes properties to the TextViews described in the layout xml.
	 */
	public void setupData() {
		TextView messageSubject = (TextView) findViewById(R.id.message_subject);
		TextView messageFrom = (TextView) findViewById(R.id.message_from);
		TextView messageContent = (TextView) findViewById(R.id.message_content);
		
		messageSubject.append(displayedMessage.getSubject());
		messageFrom.append(displayedMessage.getFromEmail());
		messageContent.append(displayedMessage.getMessage());
	}
	
	/**
	 * Called when an observable notifies this class. Evaluates the received response.
	 * @param observable The observed class.
	 * @param data The Object passed by notifyObservers(Object)
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(DatabaseInteractor.getResponse()==null){
			Toast.makeText(this, "Netzwerkprobleme...", Toast.LENGTH_LONG).show();
			return;
		}
		Response resp = ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
		switch(resp.getId()) {
		case Constants.RESP_GET_CONTACTS:
			createMessageDialog.updateDropDown((List<User>)resp.getObject());
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
		ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
	}
}
