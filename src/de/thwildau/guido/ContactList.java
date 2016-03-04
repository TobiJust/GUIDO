package de.thwildau.guido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import de.thwildau.guido.model.User;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.ContactListAdapter;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;
import de.thwildau.guido.util.SwipeDismissListViewTouchListener;
import de.thwildau.guido.util.UserSortName;

/**
 * This Activity displays an overview about all contacts.
 * in a {@link ListView}. The lists items are LinearLayouts 
 * described in the layout xml. Selecting one opens the 
 * {@link RouteDetail}. It observes {@link HttpConnector} to be
 * notified when the data exchange finishes.
 * @author GUIDO
 * @version 2013-12-20
 * @see ListActivity
 * @see Observer
 */
public class ContactList extends ListActivity implements Observer {

	/**
	 * A List containing the contacts to display.
	 */
	private static ArrayList<User> contactList;

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;
	
	/**
	 * The ListView custom adapter.
	 */
	ContactListAdapter adapter = null;

	/**
	 * Called when the activity is first created.
	 * Currently shows the up button in the action bar, eventually not needed.
	 * @param savedInstanceState If the activity is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_route_2lined_overview);
		// Show the Up button in the action bar.
		setupActionBar();
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Shows a dummy settings button and a logout button which logs the user out.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_menu_logout_contact, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Called when a menu item was selected. If the logout item was selected,
	 * the user will be logged out. If the filter icon was selected a 
	 * {@link FilterDialog} will be opened.
	 * @param item The selected menu item.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			DatabaseInteractor.logout(this, PreferenceData.getUserId(this));
			break;
		case R.id.action_add_contact:
			AlertDialog.Builder addContactDialog = new AlertDialog.Builder(this);
			addContactDialog.setTitle("Kontakt hinzufügen");
			final EditText contactInput = new EditText(this);
			contactInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
			addContactDialog.setView(contactInput);
			final Activity act = this;
			addContactDialog.setPositiveButton("Ok", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DatabaseInteractor.addContact(act, PreferenceData.getUserId(act), contactInput.getText().toString());
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
			break;
		case R.id.action_refreshRoutes:
			progress.show();
			DatabaseInteractor.getRoutes(this, PreferenceData.getUserId(this));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Provides the data for a ListView which two lined items. It also handles
	 * the selection of an item by opening the {@link RouteDetail}. The displayed
	 * List of items is sorted by starting date.
	 * @param displayedRoutes The Routes which shall be listed in the ListView.
	 */
	public void provideTwoLineData(boolean sort) {
		if(sort)
			Collections.sort(contactList, new UserSortName());
		ArrayList<HashMap<String, String>> listContent = new ArrayList<HashMap<String, String>>();
		for(User u : contactList) {
			HashMap<String, String> temp = new HashMap<String, String>();
			temp.put("line1", u.getName());
			temp.put("line2", u.getEmail());
			listContent.add(temp);
		}
		final ListView contactsListView = getListView();

		adapter = new ContactListAdapter(this, listContent, R.layout.activity_contacts_2lined_overview, new String[] {"line1", "line2"}, new int[] {R.id.list_item_line1, R.id.list_item_line2});
		adapter.setContactList(contactList);
		contactsListView.setBackgroundResource(R.drawable.general_back);
		contactsListView.setAdapter(adapter);

		final Activity act = this;
		contactsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				progress.show();
				DatabaseInteractor.getContactInfo(act, PreferenceData.getUserId(act), contactList.get(position).getEmail());
			}
		});
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
		case Constants.RESP_GET_CONTACTS:
			if(adapter!=null && adapter.getMessageDialog()!=null)
				adapter.getMessageDialog().updateDropDown((List<User>)resp.getObject());
			else{
				contactList = (ArrayList<User>)resp.getObject();
				if(contactList != null && (contactList.size() > 0))
					provideTwoLineData(true);
			}
			break;
		case Constants.RESP_GET_CONTACT_INFO:
			User contact = (User)resp.getObject();
			Intent intent =new Intent(this, ProfileInformation.class);
			intent.putExtra("contact", contact);
			startActivity(intent);
			break;
		case Constants.RESP_ADD_CONTACT:
			Toast.makeText(this, resp.getObject().toString(), Toast.LENGTH_LONG).show();
			DatabaseInteractor.getContacts(this, PreferenceData.getUserId(this));
			break;
		case Constants.RESP_DELETE_CONTACT:
			Toast.makeText(this, resp.getObject().toString(), Toast.LENGTH_LONG).show();
			DatabaseInteractor.getContacts(this, PreferenceData.getUserId(this));
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
	 * Get the List of contacts.
	 * @return The List of contacts.
	 */
	public static ArrayList<User> getRoutes() {
		return contactList;
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

	/**
	 * Called when the Activity is visible to the user. Registers the Swipe- Touch- and
	 * ScrollListener to the ListView.
	 */
	@Override
	protected void onResume(){
		super.onResume();
		SwipeDismissListViewTouchListener touchListener =
				new SwipeDismissListViewTouchListener(
						this.getListView(),
						new SwipeDismissListViewTouchListener.DismissCallbacks() {
							public void onDismiss(ListView listView, int[] reverseSortedPositions) {
								DatabaseInteractor.deleteContact((Activity)listView.getContext(), PreferenceData.getUserId(listView.getContext()), contactList.get(reverseSortedPositions[0]).getEmail());
								for (int position : reverseSortedPositions) {
									contactList.remove(position);
								}
								provideTwoLineData(false);
							}

							@Override
							public boolean canDismiss(int position) {
								return true;
							}
						});
		this.getListView().setOnTouchListener(touchListener);
		this.getListView().setOnScrollListener(touchListener.makeScrollListener());
		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		progress.show();
		DatabaseInteractor.getContacts(this, PreferenceData.getUserId(this));
	}
}
