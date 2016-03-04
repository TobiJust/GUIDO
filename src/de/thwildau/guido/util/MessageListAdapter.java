package de.thwildau.guido.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import de.thwildau.guido.MessageOverview;
import de.thwildau.guido.R;

/**
 * A custom adapter to provide data to the {@link MessageOverview}s ListView.
 * The getView method is overwritten to pass the Message id to the returned view.
 * @author GUIDO
 * @version 2013-12-16
 * @see SimpleAdapter
 */
public class MessageListAdapter extends SimpleAdapter {

	/**
	 * The context passed from the calling Activity.
	 */
	private Context context;
	/**
	 * The data for the ListView
	 */
	private ArrayList<HashMap<String, String>> data;
	
	/**
	 * Constructor calling the super constructor and assigning the passed values.
	 * @param context The context passed from the calling Activity.
	 * @param data The data for the ListView
	 * @param resource The resource id for the layout representing an item.
	 * @param from The HashMap keys where the items text is from.
	 * @param to The resource id of the TextViews the text will be written to.
	 */
	public MessageListAdapter(Context context,
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
            view = vi.inflate(R.layout.activity_message_2lined_overview, null);
        }
        view.setTag(data.get(position).get("id"));
        TextView line1 = (TextView) view.findViewById(R.id.message_line1);
        line1.setText(data.get(position).get("line1"));
        TextView line2 = (TextView) view.findViewById(R.id.message_line2);
        line2.setText(data.get(position).get("line2"));
        
        return view;
	}
}