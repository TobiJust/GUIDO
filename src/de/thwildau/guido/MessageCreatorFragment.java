package de.thwildau.guido;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import de.thwildau.guido.model.Message;
import de.thwildau.guido.model.User;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.MessageViewAdapter;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.ResponseHandler;

/**
 * An Activity displaying a form to create Routes.
 * The created routes are sent to the server.
 * @author GUIDO
 */
public class MessageCreatorFragment extends DialogFragment implements Observer {

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;
	private EditText subjectText, messageText;
	private AutoCompleteTextView toText;
	private Activity activity;
	private List<User> contactList = null;
	private MessageViewAdapter contactAdapter;
	private TextWatcher contactTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			if(contactList == null)
				DatabaseInteractor.getContacts(activity, PreferenceData.getUserId(activity));
			else{
				updateDropDown(contactList);
			}
		}
	};
	
	/**
	 * Called when the Dialog is created. Initializes its components.
	 * @param savedInstanceState The last saved instance state of the Fragment, 
	 * or null if this is a freshly created Fragment
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LinearLayout lin = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.activity_message_creator, null);
		toText = (AutoCompleteTextView) lin.findViewById(R.id.message_to_email);
		subjectText = (EditText) lin.findViewById(R.id.message_subject);
		messageText = (EditText) lin.findViewById(R.id.message_content);
		activity = getActivity();
		Bundle b = getArguments();
		if(b!=null){
			toText.append(b.getString("toEmail"));
			if(b.getBoolean("response"))
				subjectText.append("AW: "+ b.getString("subject"));
			else
				subjectText.append(b.getString("subject"));
		}
		else{
			toText.addTextChangedListener(contactTextWatcher);
		}

		builder.setView(lin);
		builder.setPositiveButton("Senden", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message m = new Message();
				m.setToEmail(toText.getText().toString());
				m.setSubject(subjectText.getText().toString());
				m.setMessage(messageText.getText().toString());
				DatabaseInteractor.sendMessage(activity, PreferenceData.getUserId(activity), m);
				dismiss();
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		return builder.create();
	}

	/**
	 * Updates the dropdown of users.
	 * @param dropDownList The dropdown List
	 */
	public void updateDropDown(List<User> dropDownList){
		if(dropDownList!= null && dropDownList.size()>0){
			contactList = dropDownList;
			List<User> filteredList = new ArrayList<User>();
			for(User u:contactList){
				if(u.getEmail().contains(toText.getText().toString())){
					filteredList.add(u);
				}
			}
			if(filteredList.size()==0){
				return;
			}
			contactAdapter= new MessageViewAdapter(getActivity(),android.R.layout.simple_dropdown_item_1line, filteredList);
			//configure AutoComplete
			toText.setAdapter(contactAdapter);
			toText.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
					toText.removeTextChangedListener(contactTextWatcher);
					toText.setText(contactList.get(arg2).getEmail());
					toText.addTextChangedListener(contactTextWatcher);
				}
			});
			toText.showDropDown();
		}
	}

	/**
	 * Called when an observable notifies this class.
	 * @param observable The observed class.
	 * @param data The Object passed by notifyObservers(Object)
	 */
	@Override
	public void update(Observable observable, Object data) {
		ResponseHandler.handleResponse(activity, DatabaseInteractor.getResponse());
	}
}
