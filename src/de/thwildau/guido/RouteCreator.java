package de.thwildau.guido;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import de.thwildau.guido.model.Route;
import de.thwildau.guido.util.Constants;
import de.thwildau.guido.util.DatabaseInteractor;
import de.thwildau.guido.util.HttpConnector;
import de.thwildau.guido.util.PreferenceData;
import de.thwildau.guido.util.TextValidator;

/**
 * An Activity displaying a form to create Routes.
 * The created routes are sent to the server. Observes the {@link HttpConnector}
 * and is notified when the data exchange finished.
 * @author GUIDO
 * @version 2013-12-20
 * @see Activity
 * @see Observer
 */
public class RouteCreator extends Activity {

	/**
	 * Instance of the calendar 
	 */
	private Calendar c;

	/**
	 * A Spinner with to choose the routes category.
	 */
	private Spinner categories, traveltypes;

	/**
	 * A dates components
	 */
	private int fYear, fMonth, fDay, fHour, fMinute;

	/**
	 * The forms TimePicker to set the starting time.
	 */
	private TimePicker timer;

	/**
	 * The forms DatePicker to set the starting date.
	 */
	private DatePicker dater;

	private Spinner timeSpinner, dateSpinner;

	private EditText nameText, descText, maxPartText, passwordText;

	private CheckBox privateCheck;

	/**
	 * The Route which will be created
	 */
	private Route r;
	/**
	 * Counts the number of times the back button was pressed.
	 */
	private static int backButtonCount = 0;

	/**
	 * Called when the Activity is created.
	 * Initializes the forms components.
	 * @param savedInstanceState If the activity is being re-initialized after previously 
	 * being shut down then this Bundle contains the data it most recently supplied in 
	 * onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_creator);
		
		nameText = (EditText) findViewById(R.id.create_route_name_edittext);
		descText = (EditText) findViewById(R.id.create_route_desc_edittext);
		maxPartText = (EditText) findViewById(R.id.create_route_maxpart_edittext);
		privateCheck = (CheckBox) findViewById(R.id.create_route_private_check);
		passwordText = (EditText) findViewById(R.id.create_route_password_edittext);

		c = Calendar.getInstance();
		fYear = c.get(Calendar.YEAR);
		fMonth = c.get(Calendar.MONTH);
		fDay = c.get(Calendar.DAY_OF_MONTH);
		fHour = c.get(Calendar.HOUR_OF_DAY);
		fMinute = c.get(Calendar.MINUTE);

		timeSpinner = (Spinner) findViewById(R.id.create_route_time_spinner);
		timeSpinner.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) 
					openTimePicker(v);
				return true;
			}
		});
		timeSpinner.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					openDatePicker(v);
					return true;
				} else 
					return false;
			}
		});
		dateSpinner = (Spinner) findViewById(R.id.create_route_date_spinner);
		dateSpinner.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) 
					openDatePicker(v);
				return true;
			}
		});
		dateSpinner.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					openDatePicker(v);
					return true;
				} else 
					return false;
			}
		});

		categories = (Spinner) findViewById(R.id.create_route_category_spinner);
		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Constants.ROUTE_CATEGORIES);
		categories.setAdapter(categoryAdapter);

		traveltypes = (Spinner) findViewById(R.id.create_route_traveltype_spinner);
		ArrayAdapter<String> traveltypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Constants.ROUTE_TRAVELTYPES);
		traveltypes.setAdapter(traveltypeAdapter);
	}

	/**
	 * Called when the activity is re-launched while at the top of the activity 
	 * stack instead of a new instance of the activity being started, onNewIntent() 
	 * will be called on the existing instance with the Intent that was used to re-launch it. 
	 */
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}
	
	/**
	 * Called when the Activity gets visible to the user. Initializes the Activities components 
	 * with the passed Routes parameters.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		r = (Route) getIntent().getSerializableExtra("route");
		if(r == null){
			r = new Route();
		}
		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[] {fHour + ":" + (fMinute < 10 ? "0" + fMinute : fMinute)});
		timeSpinner.setAdapter(timeAdapter);
		ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[] {fDay + ". " + Constants.MONTHS[fMonth] + " " + fYear});
		dateSpinner.setAdapter(dateAdapter);
		privateCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				if(checked)  {
					passwordText.setVisibility(View.VISIBLE);
					passwordText.requestFocus();
				}
				else {
					passwordText.setVisibility(View.GONE);
					passwordText.setText("");
				}
			}
		});
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * Shows a dummy settings button and a logout button which logs the user out.
	 * @param menu The options menu in which the menu items are placed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_menu_logout_next, menu);
		return true;
	}

	/**
	 * Called when a menu item was selected. If the logout item was selected,
	 * the user will be logged out.
	 * @param item The selected menu item.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			DatabaseInteractor.logout(this, PreferenceData.getUserId(this));
			break;
		case R.id.action_next:
			next();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Executed when the back button is pressed.
	 */
	@Override
	public void onBackPressed() {
		if(backButtonCount >= 1) 
			super.onBackPressed();
		else {
			Toast.makeText(this, "Noch einmal drücken, um Route zu verwerfen", Toast.LENGTH_SHORT).show();
			backButtonCount++;
		}
	}

	/**
	 * Called when the submit Button was pressed.
	 * It gets the entered data from the form and passes it to 
	 * {@link DatabaseInteractor#createRoute(Activity, String, Route)}
	 * @param view
	 */
	public void next() {
		String name = nameText.getText().toString();
		String desc = descText.getText().toString();
		String maxPart = maxPartText.getText().toString();
		String password = passwordText.getText().toString();
		c.set(fYear, fMonth, fDay, fHour, fMinute, 0);
		Date startDate = c.getTime();
		c = Calendar.getInstance();
		Date currentDate = c.getTime();

		if(!(TextValidator.isNotEmpty(name) && TextValidator.isNotEmpty(desc) && TextValidator.isNotEmpty(maxPart))) 
			Toast.makeText(this, "Name, Beschreibung und maximale Teilnehmeranzahl sind Pflichtfelder", Toast.LENGTH_LONG).show();
		else if(!TextValidator.isNumber(maxPart))
			Toast.makeText(this, "Bitte nur Zahlen als maximale Teilnehmeranzahl eingeben", Toast.LENGTH_LONG).show();
		else if(!startDate.after(currentDate))
			Toast.makeText(this, "Der Startzeitpunkt liegt in der Vergangenheit", Toast.LENGTH_LONG).show();
		else if(privateCheck.isChecked() && !TextValidator.isNotEmpty(password))
			Toast.makeText(this, "Passwort eingeben oder Checkbox abwählen", Toast.LENGTH_LONG).show();
		else if(traveltypes.getSelectedItem().toString().equals(Constants.ROUTE_TRAVELTYPES[1]))
			Toast.makeText(this, "Öffentliche Verkehrsmittel sind nicht verfügbar", Toast.LENGTH_LONG).show();
		else {
			r.setName(name);
			r.setDescription(desc);
			r.setMaxPart(Integer.parseInt(maxPart));
			r.setCategory(categories.getSelectedItem().toString());
			r.setTravelType((int)traveltypes.getSelectedItemPosition()+1);
			r.setPassword(privateCheck.isChecked());
			r.setDate(startDate);

			backButtonCount = 0;

			Intent intent =new Intent(this, RouteCreatorMap.class);
			intent.putExtra("route", r);
			if(privateCheck.isChecked() && TextValidator.isNotEmpty(password))
				intent.putExtra("password", password);
			Log.i("PASSWORD ROUTECREATOR", password);
			startActivity(intent);
		}
	}

	/**
	 * Opens the TimePicker in a new Dialog to pick a time.
	 * @param view The TimePicker view.
	 */
	public void openTimePicker(View view) {
		timer = new TimePicker(this);
		timer.setIs24HourView(true);
		timer.setCurrentHour(fHour);
		timer.setCurrentMinute(fMinute);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Startzeit");
		alertDialogBuilder.setView(timer);
		alertDialogBuilder.setNegativeButton("Abbrechen", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}
		});
		final Activity act = this;
		alertDialogBuilder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				fHour = timer.getCurrentHour();
				fMinute = timer.getCurrentMinute();
				timeSpinner.setAdapter(new ArrayAdapter<String>(act, android.R.layout.simple_spinner_dropdown_item, new String[] {fHour + ":" + (fMinute < 10 ? "0" + fMinute : fMinute)}));
				dialog.dismiss();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	/**
	 * Opens the DatePicker in a new Dialog to pick a time.
	 * @param view The DatePicker view.
	 */
	public void openDatePicker(View view) {
		dater = new DatePicker(this);
		dater.setCalendarViewShown(false);
		dater.init(fYear, fMonth, fDay, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Startdatum");
		alertDialogBuilder.setView(dater);
		alertDialogBuilder.setNegativeButton("Abbrechen", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		});
		final Activity act = this;
		alertDialogBuilder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				fDay = dater.getDayOfMonth();
				fMonth = dater.getMonth();
				fYear = dater.getYear();
				dateSpinner.setAdapter(new ArrayAdapter<String>(act, android.R.layout.simple_spinner_dropdown_item, new String[] {fDay + ". " + Constants.MONTHS[fMonth] + " " + fYear}));
				dialog.dismiss();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}