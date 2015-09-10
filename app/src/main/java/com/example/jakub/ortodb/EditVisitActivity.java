package com.example.jakub.ortodb;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
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


public class EditVisitActivity extends Activity {

    String visitId;
    Calendar begVisit, endVisit;
    private PatientsHandler patientsHandler;
    private VisitsHandler visitsHandler;


    TextView textViewData;
    TextView textViewBegTime;
    TextView textViewEndTime;
    Spinner spinner;
    TextView textViewPurpose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_visit);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Edycja wizyty");
        actionBar.setDisplayHomeAsUpEnabled(true);

        visitsHandler = new VisitsHandler(this);
        patientsHandler = new PatientsHandler(this);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        visitId = message;

        Visit visit = visitsHandler.getVisit(visitId);

        begVisit = Calendar.getInstance();
        endVisit = Calendar.getInstance();
        begVisit.setTimeInMillis(visit.getDataBeg());
        endVisit.setTimeInMillis(visit.getDataEnd());

        textViewPurpose = (TextView) findViewById(R.id.editPurpose);
        spinner = (Spinner) findViewById(R.id.spinner);
        textViewEndTime = (TextView) findViewById(R.id.wizytaGodzinaZakonczenia);
        textViewBegTime = (TextView) findViewById(R.id.wizytaGodzinaRozpoczecia);
        textViewData = (TextView) findViewById(R.id.wizytaDzien);
        textViewData.setText(CalendarHandler.milisToDate(begVisit.getTimeInMillis()));
        textViewBegTime.setText(CalendarHandler.milisToTime(begVisit.getTimeInMillis()));
        textViewEndTime.setText(CalendarHandler.milisToTime(endVisit.getTimeInMillis()));

        {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.doctors_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            String [] arr = getResources().getStringArray(R.array.doctors_array);
            int i = 0;
            while(!(arr[i].equals(visit.getDoctor())))
                i++;
            spinner.setSelection(i);
        }
        textViewPurpose.setText(visit.getPurpose());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_visit, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items


        Visit visit = visitsHandler.getVisit(visitId);
       // System.out.println(visit);

        Patient patient = patientsHandler.getPatient(visit.getPatient().toString());
      //  System.out.println(patient);

        switch (item.getItemId()) {
            case R.id.okButton:
                EditText editText = (EditText) findViewById(R.id.editPurpose);
                visit.setPurpose(String.valueOf(editText.getText()));
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                visit.setDoctor(spinner.getSelectedItem().toString());
                visit.setDataBeg(begVisit.getTimeInMillis());
                visit.setDataEnd(endVisit.getTimeInMillis());

                CalendarHandler.runQuery(this, patient, begVisit, endVisit , visit,1);
                visitsHandler.updateVisit(visit);
                finish();
                break;
            case R.id.deleteButton:
                DeletingVisitDialog dialog = new DeletingVisitDialog(visit, patient, this);
                dialog.show(getFragmentManager(),"visitDeleting");
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

    public class DeletingVisitDialog extends DialogFragment {
        private final Visit visit;
        private final Patient patient;
        private final Context context;

        public DeletingVisitDialog(Visit visit, Patient patient, Context context) {
            this.visit= visit;
            this.patient = patient;
            this.context = context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Czy na pewno chcesz usunąć wizytę?")
                    .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CalendarHandler.runQuery(context, patient, begVisit, endVisit, visit, 2);
                            visitsHandler.deleteVisit(visit.getId().toString());
                            finish();
                        }
                    })
                    .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            return builder.create();
        }
    }

}

