package de.thwildau.guido;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.RouteListFilter;

/**
 * A DialogFragment representing a dialog to filter the ListView of routes.
 * @author GUIDO
 * @version 2013-12-20
 * @see DialogFragment
 */
public class FilterDialogFragment extends DialogFragment {

	/**
	 * Calendar instance to get the current date.
	 */
	private Calendar c;

	/**
	 * Components of the set date and time.
	 */
	private int fYear, fMonth, fDay, fHour, fMinute;

	/**
	 * CheckBoxes to filter the list.
	 */
	private CheckBox checkName, checkPublic, checkOwn, 
	checkMaxparts, checkNotFull, checkDate, 
	checkCategory, checkTraveltype;

	private EditText nameInput, maxpartsInput;
	private Spinner categorySpinner, traveltypeSpinner;
	private DatePicker datePicker;
	private TimePicker timePicker;
	/**
	 * Instance of the {@link RoutesOverview}
	 */
	private RoutesOverview ro;

	private ScrollView scrollView;

	/**
	 * Called when the dialog is created.
	 * Sets the current date and creates the AlertDialog with a builder. 
	 * Initializes the Dialogs components.
	 * @param savedInstanceState If the Dialog is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 * @return The created Dialog.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		scrollView = (ScrollView)getActivity().getLayoutInflater().inflate(R.layout.dialog_filter, null);
		checkPublic = (CheckBox) scrollView.findViewById(R.id.check_public);
		checkOwn = (CheckBox) scrollView.findViewById(R.id.check_own);
		checkNotFull = (CheckBox) scrollView.findViewById(R.id.check_not_full);
		checkMaxparts = (CheckBox) scrollView.findViewById(R.id.check_maxparts);
		checkName = (CheckBox) scrollView.findViewById(R.id.check_name);
		checkDate = (CheckBox) scrollView.findViewById(R.id.check_date);
		checkCategory = (CheckBox) scrollView.findViewById(R.id.check_category);
		checkTraveltype = (CheckBox) scrollView.findViewById(R.id.check_traveltype);
		checkName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if(nameInput==null)
					nameInput = (EditText)scrollView.findViewById(R.id.name_input);
				if(isChecked){
					nameInput.setVisibility(View.VISIBLE);					
				}
				else{
					nameInput.setVisibility(View.GONE);
				}
			}
		});

		checkDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if(datePicker==null)
					datePicker = (DatePicker)scrollView.findViewById(R.id.datepicker);
				if(timePicker==null)
					timePicker = (TimePicker)scrollView.findViewById(R.id.timepicker);
				if(isChecked){
					datePicker.setVisibility(View.VISIBLE);	
					timePicker.setVisibility(View.VISIBLE);					
				}
				else{
					datePicker.setVisibility(View.GONE);
					timePicker.setVisibility(View.GONE);
				}
			}
		});

		checkCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if(categorySpinner==null)
					categorySpinner = (Spinner)scrollView.findViewById(R.id.category_spinner);
				if(isChecked){
					categorySpinner.setVisibility(View.VISIBLE);					
				}
				else{
					categorySpinner.setVisibility(View.GONE);
				}
			}
		});

		checkTraveltype.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if(traveltypeSpinner==null)
					traveltypeSpinner = (Spinner)scrollView.findViewById(R.id.traveltype_spinner);
				if(isChecked){
					traveltypeSpinner.setVisibility(View.VISIBLE);					
				}
				else{
					traveltypeSpinner.setVisibility(View.GONE);
				}
			}
		});

		checkMaxparts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if(maxpartsInput==null)
					maxpartsInput = (EditText)scrollView.findViewById(R.id.maxpart_input);
				if(isChecked){
					maxpartsInput.setVisibility(View.VISIBLE);					
				}
				else{
					maxpartsInput.setVisibility(View.GONE);
				}
			}
		});
		c = Calendar.getInstance();
		fYear = c.get(Calendar.YEAR);
		fMonth = c.get(Calendar.MONTH);
		fDay = c.get(Calendar.DAY_OF_MONTH);
		fHour = c.get(Calendar.HOUR_OF_DAY);
		fMinute = c.get(Calendar.MINUTE);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Filter");



		categorySpinner = (Spinner)scrollView.findViewById(R.id.category_spinner);
		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Constants.ROUTE_CATEGORIES);
		categorySpinner.setAdapter(categoryAdapter);

		traveltypeSpinner = (Spinner)scrollView.findViewById(R.id.traveltype_spinner);
		ArrayAdapter<String> traveltypeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Constants.ROUTE_TRAVELTYPES);
		traveltypeSpinner.setAdapter(traveltypeAdapter);

		builder.setView(scrollView);
		ro = (RoutesOverview) getActivity();
		builder.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				c.set(fYear, fMonth, fDay, fHour, fMinute, 0);
				Date date = c.getTime();
				Log.i("MAXPARTS",(checkName.isChecked()&&nameInput.getText().length()>0?nameInput.getText().toString():"null"));
				Log.i("MAXPARTS",checkPublic.isChecked()+"");
				Log.i("MAXPARTS",checkNotFull.isChecked()+"");
				Log.i("MAXPARTS",checkOwn.isChecked()+"");
				Log.i("MAXPARTS",(checkMaxparts.isChecked()&&maxpartsInput.getText().length()>0?(""+Integer.parseInt(maxpartsInput.getText().toString())):("null")));
				Log.i("MAXPARTS",(checkDate.isChecked()?""+date:"null"));
				Log.i("MAXPARTS",(checkCategory.isChecked()?categorySpinner.getSelectedItem().toString():"null"));
				Log.i("MAXPARTS",(checkTraveltype.isChecked()?traveltypeSpinner.getSelectedItem().toString():"null"));
				RouteListFilter.setFilter( 
						(checkName.isChecked()&&nameInput.getText().length()>0?nameInput.getText().toString():null),
						checkPublic.isChecked(),
						checkNotFull.isChecked(),
						checkOwn.isChecked(),
						(checkMaxparts.isChecked()&&maxpartsInput.getText().length()>0?Integer.parseInt(maxpartsInput.getText().toString()):null),
						(checkDate.isChecked()?date:null),
						(checkCategory.isChecked()?categorySpinner.getSelectedItem().toString():null),
						(checkTraveltype.isChecked()?traveltypeSpinner.getSelectedItem().toString():null));
				ro.provideTwoLineData(RoutesOverview.getRoutes());
				dismiss();
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});

		Bundle b = getArguments();
		if(b!=null){
			if(b.getString("name")!=null){
				checkName.setChecked(true);
				if(nameInput==null)
					nameInput = (EditText)scrollView.findViewById(R.id.name_input);
				nameInput.setText(b.getString("name"));
				nameInput.setVisibility(View.VISIBLE);
			}
			if(b.getBoolean("public"))
				checkPublic.setChecked(true);
			if(b.getBoolean("notFull"))
				checkNotFull.setChecked(true);
			if(b.getBoolean("ownRoutes"))
				checkOwn.setChecked(true);
			if(b.getInt("maxParts")!=0){
				checkMaxparts.setChecked(true);
				if(maxpartsInput==null)
					maxpartsInput = (EditText)scrollView.findViewById(R.id.maxpart_input);
				maxpartsInput.setText(String.valueOf(b.getInt("maxParts")));
				maxpartsInput.setVisibility(View.VISIBLE);
			}
			if(b.getString("date")!=null){
				try {
					Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMAN).parse(b.getString("date"));
					c.setTime(d);
					Log.i("SETTING TIME", b.getString("date")+" "+c.getTime().toString());
					fYear = c.get(Calendar.YEAR);
					fMonth = c.get(Calendar.MONTH);
					fDay = c.get(Calendar.DAY_OF_MONTH);
					fHour = c.get(Calendar.HOUR_OF_DAY);
					fMinute = c.get(Calendar.MINUTE);
					checkDate.setChecked(true);
					datePicker = (DatePicker)scrollView.findViewById(R.id.datepicker);
					datePicker.setVisibility(View.VISIBLE);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if(b.getString("category")!=null){
				checkCategory.setChecked(true);
				if(categorySpinner==null)
					categorySpinner = (Spinner)scrollView.findViewById(R.id.category_spinner);
				int i = 0;
				for(String s:Constants.ROUTE_CATEGORIES){
					if(s.equals(b.getString("category")))
						break;
					i++;
				}
				categorySpinner.setSelection(i);
				categorySpinner.setVisibility(View.VISIBLE);
			}
			if(b.getString("traveltype")!=null){
				checkTraveltype.setChecked(true);
				if(traveltypeSpinner==null)
					traveltypeSpinner = (Spinner)scrollView.findViewById(R.id.traveltype_spinner);
				int i = 0;
				for(String s:Constants.ROUTE_TRAVELTYPES){
					if(s.equals(b.getString("traveltype")))
						break;
					i++;
				}
				traveltypeSpinner.setSelection(i);
				traveltypeSpinner.setVisibility(View.VISIBLE);
			}

		}

		setupDatePicker();
		setupTimePicker();
		return builder.create();
	}

	/**
	 * Setup the TimePicker with the current time and add a Listener.
	 * @param time The Dialogs TimePicker.
	 */
	public void setupTimePicker() {
		if(timePicker==null)
			timePicker = (TimePicker) scrollView.findViewById(R.id.timepicker);
		timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
		timePicker.setIs24HourView(true);
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				fHour = hourOfDay;
				fMinute = minute;
			}
		});
	}

	/**
	 * Setup the DatePicker with the current date and add a Listener.
	 * @param date The Dialogs DatePicker.
	 */
	public void setupDatePicker() {
		if(datePicker==null)
			datePicker = (DatePicker)scrollView.findViewById(R.id.datepicker);
		datePicker.setCalendarViewShown(false);
		datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				fYear = year;
				fMonth = monthOfYear;
				fDay = dayOfMonth;
			}
		});
	}


}
