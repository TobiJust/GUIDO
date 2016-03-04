package de.thwildau.guido;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.thwildau.guido.model.User;

/**
 * A DialogFragment representing a dialog to filter the ListView of routes.
 * @author GUIDO
 * @version 2013-12-20
 * @see DialogFragment
 */
public class ParticipatorsDialogFragment extends DialogFragment {

	Bundle args = null;
	/**
	 * Fragment to create a new message.
	 */
	MessageCreatorFragment createMessageDialog = null;
	
	/**
	 * Called when the dialog is created.
	 * Sets the current date and creates the AlertDialog with a builder. 
	 * Initializes the Dialogs components.
	 * @param savedInstanceState If the Dialog is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 * @return The created Dialog.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LinearLayout lin = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.activity_active_route_missing_participators_list, null);
		builder.setTitle("Teilnehmer auﬂer Reichweite");
		ListView mainListView = (ListView) lin.findViewById(R.id.missing_participators_list); 
		args = getArguments();
		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.activity_active_route_missing_participators_row, args.getStringArrayList("missing_participators"));
		mainListView.setAdapter( listAdapter );
		mainListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ArrayList<User> userList = (ArrayList<User>)args.getSerializable("users");
				User sendTo = userList.get(arg2);
				createMessageDialog = new MessageCreatorFragment();
				Bundle args = new Bundle();
				args.putString("toEmail", sendTo.getEmail());
				args.putString("subject", "");
				args.putBoolean("response", false);
				createMessageDialog.setArguments(args);
				createMessageDialog.show(getFragmentManager(), "Nachricht schreiben");
			}
		});
		builder.setView(lin);
		builder.setPositiveButton("Verlassen", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		return builder.create();
	}
	
	/**
	 * Gets the Fragment to create a new message.
	 * @return The Fragment to create a new message.
	 */
	public MessageCreatorFragment getMessageCreatorFragment(){
		return createMessageDialog;
	}
}
