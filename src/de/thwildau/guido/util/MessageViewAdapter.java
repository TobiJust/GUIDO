package de.thwildau.guido.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.thwildau.guido.MessageOverview;
import de.thwildau.guido.R;
import de.thwildau.guido.model.User;

/**
 * A custom adapter to provide data for the {@link MessageOverview}.
 * @author CaLLe
 * @see ArrayAdapter
 */
public class MessageViewAdapter extends ArrayAdapter<String> {

	private HashMap<Integer, User> mContactMap = new HashMap<Integer, User>();
	private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
	private Context context;

	/**
	 * Constructor assigning the passed parameters.
	 * @param context 
	 * @param textViewResourceId
	 * @param objects
	 */
	public MessageViewAdapter(Context context, int textViewResourceId, List<User> objects) {
		super(context, textViewResourceId, convertUserList(objects));
		this.context = context;
		for (int i = 0; i < objects.size(); i++) {
			System.out.println("ADDING CONTACT "+objects.get(i).toString());
			mContactMap.put(i, objects.get(i));
			mIdMap.put(contactToString(objects.get(i)), i);
		}
	}

	/**
	 * Customizes a ListView item.
	 */
	@Override 
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.list_element, parent, false);
		TextView line1 = (TextView) rowView.findViewById(R.id.firstLine);
		TextView line2 = (TextView) rowView.findViewById(R.id.secondLine);
		User contact = (User)getUserItem(position);
		System.out.println("CONTACT: "+contact.getName()+ " POSITION: "+position);
		line1.setText(contact.getName());
		line2.setText(contact.getEmail());
		return rowView;
	}

	public User getUserItem(int position){
		return mContactMap.get(position);
	}

	@Override
	public long getItemId(int position) {
		String item = getItem(position);
		return mIdMap.get(item);
	}
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	/**
	 * Converts a List of users to a List of Strings.
	 * @param uList The List of users.
	 * @return The List of Strings.
	 */
	public static List<String> convertUserList(List<User> uList){
		if(uList == null)
			return null;
		List<String> stringList = new ArrayList<String>();
		for(User u : uList){
			stringList.add(u.getEmail());
		}
		return stringList;
	}
	
	/**
	 * Converts a user to a String.
	 * @param contact The user.
	 * @return The String.
	 */
	public static String contactToString(User contact){
		User usr = (User)contact;
		String output = "";
		output+= usr.getEmail();
		return output;
	}
}