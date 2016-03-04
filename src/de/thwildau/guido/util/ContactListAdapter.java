package de.thwildau.guido.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import de.thwildau.guido.MessageCreatorFragment;
import de.thwildau.guido.R;
import de.thwildau.guido.RoutesOverview;
import de.thwildau.guido.model.User;

/**
 * A custom adapter to provide data to the {@link RoutesOverview}s ListView.
 * The getView method is overwritten to pass the Route id to the returned view.
 * @author GUIDO
 * @version 2013-12-16
 * @see SimpleAdapter
 */
public class ContactListAdapter extends SimpleAdapter {

	/**
	 * The context passed from the calling Activity.
	 */
	private Context context;
	/**
	 * The data for the ListView
	 */
	private ArrayList<HashMap<String, String>> data;
	/**
	 * A users contacs
	 */
	private ArrayList<User> contactList;
	/**
	 * A Fragment to create a new Dialog
	 */
	private MessageCreatorFragment createMessageDialog = null;
	
	/**
	 * Constructor calling the super constructor and assigning the passed values.
	 * @param context The context passed from the calling Activity.
	 * @param data The data for the ListView
	 * @param resource The resource id for the layout representing an item.
	 * @param from The HashMap keys where the items text is from.
	 * @param to The resource id of the TextViews the text will be written to.
	 */
	public ContactListAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
		this.context = context;
		this.data = (ArrayList<HashMap<String, String>>) data;
	}

	/**
	 * Gets the view.
	 * @param position The items position in the list.
	 * @param convertView The current view to process.
	 * @param parent The parent view (the ListView).
	 * @return The current view.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		// convertView is always null
        if(view == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.activity_contacts_2lined_overview, null);
        }
        ImageView line1Image = (ImageView) view.findViewById(R.id.list_image_line1);
        final int onClickPosition = position;
        line1Image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createMessageDialog = new MessageCreatorFragment();
				Bundle args = new Bundle();
				args.putString("toEmail", contactList.get(onClickPosition).getEmail());
				args.putString("subject", "");
				args.putBoolean("response", false);
				createMessageDialog.setArguments(args);
				createMessageDialog.show(((Activity)v.getContext()).getFragmentManager(), "Nachricht schreiben");
			}
		});
        view.setTag(data.get(position).get("id"));
        TextView line1 = (TextView) view.findViewById(R.id.list_item_line1);
        line1.setText(data.get(position).get("line1"));
        TextView line2 = (TextView) view.findViewById(R.id.list_item_line2);
        line2.setText(data.get(position).get("line2"));
        
        return view;
	}
	
	/**
	 * Sets the users contacts list.
	 * @param contacts The users contacts.
	 */
	public void setContactList(ArrayList<User> contacts){
		contactList = contacts;
	}
	
	/**
	 * Returns a Fragment to create a message.
	 * @return A Fragment to create a message.
	 */
	public MessageCreatorFragment getMessageDialog(){
		return createMessageDialog;
	}
}