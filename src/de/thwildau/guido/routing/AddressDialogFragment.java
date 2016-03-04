package de.thwildau.guido.routing;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import de.thwildau.guido.R;
import de.thwildau.guido.RouteCreatorMap;
import de.thwildau.guido.model.POI;
import de.thwildau.guido.routing.AddressViewAdapter;

public class AddressDialogFragment extends DialogFragment {

	private static final int DIALOG_ICON = R.drawable.mapneedle_mini;
	AutoCompleteTextView address;
	EditText editName;
	EditText editDesc;
	Address currentAddress;
	private String addressViewText;
	private boolean showSuggestions = true;
	private POI poi;
	private View descView;
	private boolean showAddress = true;
	private boolean isEditable = true;
	private AddressTextWatcher textWatch;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		descView = inflater.inflate(R.layout.dialog_address, null); 
		//set gui elements
		address = (AutoCompleteTextView) descView.findViewById(R.id.autoAddressDropdown);
		System.out.println("ADDRESS " + address);
		address.setText(getAddressViewText());
		if(!showAddress){
			address.setVisibility(View.GONE);
//			descView.findViewById(R.id.labelDropdown).setVisibility(View.GONE);
		}
		else{
			textWatch = new AddressTextWatcher(getActivity());
			address.addTextChangedListener(textWatch);
		}
		editName = (EditText) descView.findViewById(R.id.textPOIName);
		editDesc = (EditText) descView.findViewById(R.id.textPOIDescription);
		if(!isEditable){
			address.setFocusable(false);
			address.setClickable(false);
			editName.setFocusable(false);
			editName.setClickable(false);
			editDesc.setFocusable(false);
			editDesc.setClickable(false);
		}

		AlertDialog.Builder addressDialog = new AlertDialog.Builder(getActivity());
		addressDialog.setIcon(DIALOG_ICON).setView(descView);
		addressDialog.setTitle("Routenabschnitt");
		
		Log.i("DIALOG", poi.getName() + "  " + poi.getDescription());
		if(poi.getName() != null)
			editName.setText(poi.getName());
		if(poi.getDescription() != null)
			editDesc.setText(poi.getDescription());

		//buttons
		addressDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				poi.setName(editName.getText().toString().trim());
				poi.setDescription(editDesc.getText().toString().trim());

				if(currentAddress != null){
					poi.setLat(currentAddress.getLatitude());
					poi.setLng(currentAddress.getLongitude());
					((RouteCreatorMap) getActivity()).getMapView().updateUI(poi);
				}
			}
		});
		addressDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return addressDialog.show();
	}
	public void updateDropDown(List<Address> dropDownList){
		if(dropDownList!= null && dropDownList.size()>0){
			Log.i("DROPDOWNLIST", dropDownList.get(0)+"");
			final AddressViewAdapter addressAdapter= new AddressViewAdapter(getActivity(),android.R.layout.simple_dropdown_item_1line, dropDownList);
			//configure AutoComplete
			address.setAdapter(addressAdapter);
			address.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
					Toast.makeText(getActivity(),(CharSequence)arg0.getItemAtPosition(arg2), Toast.LENGTH_LONG).show();
					currentAddress = addressAdapter.getAddressItem(arg2);
					showSuggestions = false;
				}
			});
			if(showSuggestions){
				address.showDropDown();
			}
			else{
				showSuggestions=true;
			}
		}
	}
	public void updateAddressView(List<Address> touchPoint){
		if(touchPoint == null)
			return;
		setAddressViewText(touchPoint.get(0).getAddressLine(0) + ", " + touchPoint.get(0).getAddressLine(1) + " " + touchPoint.get(0).getAddressLine(2));
		showSuggestions = false;
		currentAddress = touchPoint.get(0);
	}
	private void setAddressViewText(String text) {
		this.addressViewText = text;		
	}
	private String getAddressViewText(){
		return this.addressViewText;
	}
	public void setPOI(POI poi){
		this.poi = poi;
	}
	public void setShowAddressLine(boolean showLine){
		showAddress = showLine;
	}
	public void setEditable(boolean editable){
		isEditable = editable;
	}
}
