package de.thwildau.guido.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osmdroid.bonuspack.routing.RoadNode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.thwildau.guido.R;
import de.thwildau.guido.util.GuidoMapView;

public class InstructionViewAdapter extends ArrayAdapter<String>{

	private HashMap<Integer, RoadNode> mInstructionMap = new HashMap<Integer, RoadNode>();
	private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
	private Context context;
	private GuidoMapView guidoMap;

	public InstructionViewAdapter(Context context, int resource, List<RoadNode> objects) {
		super(context, resource, convertRoadNodeList(objects));
		this.context = context;
		if(objects == null)
			return;
		for (int i = 0; i < objects.size(); ++i) {
			mInstructionMap.put(i, objects.get(i));
			mIdMap.put(roadNodeToString(objects.get(i)), i);
		}
	}

	public View getView(final int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.list_element_hints, parent, false);
		TextView line1 = (TextView) rowView.findViewById(R.id.firstLine);
		ImageView hintIcon = (ImageView) rowView.findViewById(R.id.hint_icon);
		
		line1.setText(getInstructionItem(position).mInstructions);
		hintIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				guidoMap.showBubbleHint(position);
			}
		});
		
		return rowView;
	}
	
	@Override
	public long getItemId(int position) {
		String item = getItem(position);
		return mIdMap.get(item);
	}
	public RoadNode getInstructionItem(int position){
		return mInstructionMap.get(position);
	}
	@Override
	public boolean hasStableIds() {
		return true;
	}
	private static List<String> convertRoadNodeList(List<RoadNode> roadNodeList) {
		if(roadNodeList == null)
			return null;
		List<String> stringList = new ArrayList<String>();
		for(RoadNode r : roadNodeList){
			stringList.add(roadNodeToString(r));
		}
		return stringList;
	}

	private static String roadNodeToString(RoadNode rn) {
		String output = "";
		output += rn.mInstructions + ",";
		output += rn.mLength;
		return output;
	}
	
	public void setGuidoMap(GuidoMapView guidoMapView){
		guidoMap = guidoMapView;
	}

}
