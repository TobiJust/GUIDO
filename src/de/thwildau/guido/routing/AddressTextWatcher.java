package de.thwildau.guido.routing;

import de.thwildau.guido.RouteCreatorMap;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;

public class AddressTextWatcher implements TextWatcher {

	private final int INPUT_CHAR_COUNT = 0;
	
	private RouteCreatorMap routeActivity;
	private GuidoPoint point;

	public AddressTextWatcher(RouteCreatorMap activity, LinearLayout lLayout, boolean isStart){
		this.routeActivity = activity;
	}
	
	public AddressTextWatcher(GuidoPoint newGuidoPoint){
		this.point = newGuidoPoint;
	}
	
	public AddressTextWatcher(Context currContext){
		this((RouteCreatorMap) currContext, null, false);
	}
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if(count > INPUT_CHAR_COUNT){
			new Geocoder(routeActivity).execute(s.toString().trim());
		}
	}
}
