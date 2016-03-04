package de.thwildau.guido.routing;

import java.util.Locale;

import android.location.Address;

public class GuidoAddress extends Address{

	private String houseNumber;
	public GuidoAddress(Locale locale) {
		super(locale);
	}
	
	public void setHouseNumber(String hn){
		houseNumber = hn;
	}
	
	public String getHouseNumber(){
		return houseNumber;
	}

}
