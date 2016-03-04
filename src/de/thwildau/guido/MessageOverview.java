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
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import de.thwildau.guido.model.Message;
import de.thwildau.guido.model.User;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.GuidoError;
import de.thwildau.guido.util.MessageListAdapter;
import de.thwildau.guido.util.MessageSortDate;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.Response;
import de.thwildau.guido.util.ResponseHandler;
import de.thwildau.guido.util.SwipeDismissListViewTouchListener;

/**
 * This Activity displays an overview about all received Messages
 * in a {@link ListView}. The list items are LinearLayouts 
 * described in the layout xml. Selecting one opens the 
 * {@link MessageDetail}.
 * @author GUIDO
 * @version 2013-12-20
 * @see ListActivity
 */
public class MessageOverview extends ListActivity implements Observer {

	/**
	 * A List of messages.
	 */
	private static ArrayList<Message> messageList;

	/**
	 * Instance of a progress dialog, which is displayed when the data exchange
	 * takes place.
	 */
	private ProgressDialog progress;
	
	/**
	 * Fragment to create a new message.
	 */
	private MessageCreatorFragment createMessageDialog;

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

		// setContentView(R.layout.activity_route_2lined_overview);
		// Show the Up button in the action bar.
		setupActionBar();
	}

	/**
	 * Called when the Activity gets visible to the user. Gets the messages from the database and registers
	 * Swipe- Touch- and ScrollListeners. 
	 */
	@Override
	protected void onResume() {
		DatabaseInteractor.getMessages(this, PreferenceData.getUserId(this));
		SwipeDismissListViewTouchListener touchListener =
				new SwipeDismissListViewTouchListener(
						this.getListView(),
						new SwipeDismissListViewTouchListener.DismissCallbacks() {
							public void onDismiss(ListView listView, int[] reverseSortedPositions) {
								DatabaseInteractor.deleteMessage((Activity)listView.getContext(), PreferenceData.getUserId(listView.getContext()), messageList.get(reverseSortedPositions[0]).getId());
								for (int position : reverseSortedPositions) {
									messageList.remove(position);
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
		progress = new ProgressDialog(MessageOverview.this);
		progress.setCancelable(false);
		progress.show();
		super.onResume();
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
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
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

		getMenuInflater().inflate(R.menu.app_menu_logout_sendmessage, menu);
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
		case R.id.action_sendMessage:
			Log.i("CREATEMESSAGE", "create button clicked");
			createMessageDialog = new MessageCreatorFragment();
			createMessageDialog.show(getFragmentManager(), "Create Message");
		case R.id.action_refreshMessages:
			progress.show();
			DatabaseInteractor.getMessages(this, PreferenceData.getUserId(this));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Provides the data for a ListView which two lined items. It also handles
	 * the selection of an item by opening the {@link MessageDetail}.
	 * @param displayedMessages The messages which shall be listed in the ListView.
	 */
	public void provideTwoLineData(boolean sort) {
		if(sort)
			Collections.sort(messageList, new MessageSortDate());
		ArrayList<HashMap<String, String>> listContent = new ArrayList<HashMap<String, String>>();
		for(Message m : messageList) {
			HashMap<String, String> temp = new HashMap<String, String>();
			temp.put("line1", m.getSubject());
			temp.put("line2", (m.getMessage().length()>20?m.getMessage().substring(0, 20)+"...":m.getMessage()));
			temp.put("id", m.getId());
			listContent.add(temp);
		}
		final ListView messageListView = getListView();

		// using custom adapter
		messageListView.setAdapter(new MessageListAdapter(
				this, 
				listContent,
				R.layout.activity_message_2lined_overview,
				new String[] {"line1", "line2"},
				new int[] {R.id.message_line1, 
						R.id.message_line2}));

		messageListView.setBackgroundResource(R.drawable.general_back);

		messageListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				// Get selected items tag, which contains the routes id, to identify the selected route
				Message selectedMessage = messageList.get(position);
				Intent messageDetail = new Intent(MessageOverview.this, MessageDetail.class);
				messageDetail.putExtra("message", selectedMessage);
				startActivity(messageDetail);
			}
		});
	}
	
	/**
	 * Called when an observable notifies this class. Evaluates the received response.
	 * @param observable The observed class.
	 * @param data The Object passed by notifyObservers(Object)
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(progress != null)
			progress.hide();
		Response resp = ResponseHandler.handleResponse(this, DatabaseInteractor.getResponse());
		switch(resp.getId()) {
		case Constants.RESP_GET_MESSAGE:
			messageList = (ArrayList<Message>) resp.getObject();
			provideTwoLineData(true);
			break;
		case Constants.RESP_SEND_MESSAGE:
			Toast.makeText(this, resp.getObject().toString(), Toast.LENGTH_LONG).show();
			break;
		case Constants.RESP_DELETE_MESSAGE:
			Toast.makeText(this, resp.getObject().toString(), Toast.LENGTH_LONG).show();
			break;
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
	}

	/**
	 * Returns the users messages.
	 * @return The users messages.
	 */
	public static ArrayList<Message> getMessages() {
		return messageList;
	}

	/**
	 * Sets the users messages.
	 * @param messages The users messages.
	 */
	public static void setMessages(ArrayList<Message> messages) {
		messageList = messages;
	}
}
