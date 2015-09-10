package com.example.jakub.ortodb;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;

import java.text.ParseException;

import handler.Patient;
import handler.PatientsHandler;


public class EditPersonActivity extends Activity {
    String tupleId;
    private PatientsHandler patientsHandler;

    TableLayout tableLayout;
    EditText editTextFirstName;
    EditText editTextLastName;
    EditText editTextPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Nowy pacjent");
        actionBar.setDisplayHomeAsUpEnabled(true);

        patientsHandler = new PatientsHandler(this);
        tableLayout = (TableLayout) findViewById(R.id.infoTable);
        editTextFirstName = (EditText) tableLayout.findViewById(R.id.imie);
        editTextLastName = (EditText) tableLayout.findViewById(R.id.nazwisko);
        editTextPhone = (EditText) tableLayout.findViewById(R.id.telefon);


        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE); //id
        tupleId = message;


        if (tupleId != null) {
            Patient patient = patientsHandler.getPatient(tupleId);
            editTextFirstName.setText(patient.getFirstName());
            editTextLastName.setText(patient.getLastName());
            editTextPhone.setText(patient.getPhone());
            actionBar.setTitle(patient.getFirstName() + " " + patient.getLastName());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_person, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.okButton:

                Patient patient;

                if (tupleId == null) {
                    patient = new Patient(0,
                            editTextFirstName.getText().toString(),
                            editTextLastName.getText().toString(),
                            editTextPhone.getText().toString()
                    );
                    patientsHandler.addPatient(patient);
                } else {
                    patient = new Patient(Long.valueOf(tupleId),
                            editTextFirstName.getText().toString(),
                            editTextLastName.getText().toString(),
                            editTextPhone.getText().toString()
                    );
                    patientsHandler.updatePatient(patient);
                }

                finish();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
