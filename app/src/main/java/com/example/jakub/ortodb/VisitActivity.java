package com.example.jakub.ortodb;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import handler.CalendarHandler;
import handler.Patient;
import handler.PatientsHandler;
import handler.Visit;
import handler.VisitsHandler;

import static handler.CalendarHandler.milisToDate;
import static handler.CalendarHandler.milisToTime;


public class VisitActivity extends Activity {

    String patientId;
    Calendar begVisit, endVisit;
    private PatientsHandler patientsHandler;
    private VisitsHandler visitsHandler;
    TextView textViewData;
    TextView textViewBegTime;
    TextView textViewEndTime;
    Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Nowa wizyta");
        actionBar.setDisplayHomeAsUpEnabled(true);

        begVisit = Calendar.getInstance();
        endVisit = Calendar.getInstance();
        patientsHandler = new PatientsHandler(this);
        visitsHandler = new VisitsHandler(this);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        patientId = message;

        textViewData = (TextView) findViewById(R.id.wizytaDzien);
        textViewBegTime = (TextView) findViewById(R.id.wizytaGodzinaRozpoczecia);
        textViewEndTime = (TextView) findViewById(R.id.wizytaGodzinaZakonczenia);
        spinner = (Spinner) findViewById(R.id.spinner);
        textViewData.setText(CalendarHandler.milisToDate(begVisit.getTimeInMillis()));
        textViewBegTime.setText(CalendarHandler.milisToTime(begVisit.getTimeInMillis()));
        textViewEndTime.setText(CalendarHandler.milisToTime(begVisit.getTimeInMillis()));
        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.doctors_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_visit, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.okButton:
                Patient patient = patientsHandler.getPatient(patientId);
                EditText editText = (EditText) findViewById(R.id.editPurpose);
                String purpose = String.valueOf(editText.getText());
                String doctor = spinner.getSelectedItem().toString();
                Visit visit = new Visit(0l, begVisit.getTimeInMillis(), endVisit.getTimeInMillis(), purpose, Long.valueOf(patientId), doctor, 0l);

                Long googleCalendarId = CalendarHandler.runQuery(this, patient, begVisit, endVisit, visit, 0);

                visit.setGoogleCalendarId(googleCalendarId);
                visitsHandler.addVisit(visit);
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showTimeBegPickerDialog(View view) {
        DialogFragment newFragment = new TimePickerFragment(0);
        newFragment.show(getFragmentManager(), "begTimePicker");
    }

    public void showTimeEndPickerDialog(View view) {
        DialogFragment newFragment = new TimePickerFragment(1);
        newFragment.show(getFragmentManager(), "endTimePicker");
    }


    private class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            TextView textViewData = (TextView) findViewById(R.id.wizytaDzien);
            begVisit.set(Calendar.YEAR, year);
            begVisit.set(Calendar.MONTH, monthOfYear);
            begVisit.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            endVisit.set(Calendar.YEAR, year);
            endVisit.set(Calendar.MONTH, monthOfYear);
            endVisit.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            textViewData.setText(milisToDate(begVisit.getTimeInMillis()));

        }
    }

    private class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        private final int b; //0 beg, 1 end

        public TimePickerFragment(int b) {
            this.b = b;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = begVisit.get(Calendar.HOUR_OF_DAY);
            int minute = begVisit.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            TextView textViewTime;
            switch (b) {
                case 0:
                    textViewTime = (TextView) findViewById(R.id.wizytaGodzinaRozpoczecia);
                    begVisit.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    begVisit.set(Calendar.MINUTE, minute);
                    begVisit.set(Calendar.SECOND, 0);
                    begVisit.set(Calendar.MILLISECOND, 0);
                    textViewTime.setText(milisToTime(begVisit.getTimeInMillis()));
                    break;
                case 1:
                    textViewTime = (TextView) findViewById(R.id.wizytaGodzinaZakonczenia);
                    endVisit.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endVisit.set(Calendar.MINUTE, minute);
                    endVisit.set(Calendar.SECOND, 0);
                    endVisit.set(Calendar.MILLISECOND, 0);
                    textViewTime.setText(milisToTime(endVisit.getTimeInMillis()));
                    break;
            }
        }
    }


}
