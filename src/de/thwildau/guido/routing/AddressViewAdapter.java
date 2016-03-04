package de.thwildau.guido.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.thwildau.guido.R;

public class AddressViewAdapter extends ArrayAdapter<String> {

	private HashMap<Integer, Address> mAddressMap = new HashMap<Integer, Address>();
	private HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
	private Context context;

	public AddressViewAdapter(Context context, int textViewResourceId, List<Address> objects) {
		super(context, textViewResourceId, convertAddressList(objects));
		this.context = context;
		if(objects == null)
			return;
		for (int i = 0; i < objects.size(); ++i) {
			mAddressMap.put(i, objects.get(i));
			mIdMap.put(addressToString(objects.get(i)), i);
		}
	}

	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.list_element, parent, false);
		TextView line1 = (TextView) rowView.findViewById(R.id.firstLine);
		TextView line2 = (TextView) rowView.findViewById(R.id.secondLine);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		GuidoAddress address = (GuidoAddress)getAddressItem(position);
		if(address.getThoroughfare()!=null){
			line1.setText(address.getThoroughfare()+(address.getHouseNumber()==null?"":" "+address.getHouseNumber()));
			line2.setText((address.getPostalCode()==null?"":address.getPostalCode()+", ")
					+(address.getLocality()==null?address.getAdminArea():address.getLocality()));
		}
		else{
			if(address.getPostalCode()!=null && address.getLocality() == null && address.getAdminArea()!=null){
				line1.setText(address.getPostalCode()+", "+address.getAdminArea());
			}
			else if(address.getPostalCode()!=null && address.getLocality()!=null){
				line1.setText(address.getPostalCode()+", "+address.getLocality());
			}
			else if(address.getPostalCode()!=null && address.getLocality()==null && address.getAdminArea()==null){
				line1.setText(address.getPostalCode());
			}
			else{
				line1.setText("Keine Informationen");
			}
			line2.setText((address.getCountryName()==null?"":address.getCountryName()+", ")+(address.getLocale()==null?"":address.getLocale()));
		}
		return rowView;
	}

	@Override
	public long getItemId(int position) {
		String item = getItem(position);
		return mIdMap.get(item);
	}
	public Address getAddressItem(int position){
		return mAddressMap.get(position);
	}
	@Override
	public boolean hasStableIds() {
		return true;
	}
	public static List<String> convertAddressList(List<Address> addrList){
		if(addrList == null)
			return null;
		List<String> stringList = new ArrayList<String>();
		for(Address a : addrList){
			stringList.add(addressToString(a));
		}
		return stringList;
	}
	public static String addressToString(Address addr){
		GuidoAddress address = (GuidoAddress)addr;
		String output = "";
		
		if(address.getThoroughfare()!=null){
			output+=(address.getThoroughfare()+(address.getHouseNumber()==null?"":" "+address.getHouseNumber()));
			output+=", "+((address.getPostalCode()==null?"":address.getPostalCode()+", ")
					+(address.getLocality()==null?address.getAdminArea():address.getLocality()));
		}
		else{
			if(address.getPostalCode()!=null && address.getLocality() == null && address.getAdminArea()!=null){
				output+=(address.getPostalCode()+", "+address.getAdminArea());
			}
			else if(address.getPostalCode()!=null && address.getLocality()!=null){
				output+=(address.getPostalCode()+", "+address.getLocality());
			}
			else if(address.getPostalCode()!=null && address.getLocality()==null && address.getAdminArea()==null){
				output+=(address.getPostalCode());
			}
			if(output.length()>0)
				output+=", ";
			output+=((address.getCountryName()==null?"":address.getCountryName()+", ")+(address.getLocale()==null?"":address.getLocale()));
		}
		return output;
	}

}