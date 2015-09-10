package com.example.jakub.ortodb;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import handler.CalendarHandler;
import handler.DbHandler;
import handler.Patient;
import handler.PatientsHandler;
import handler.Visit;
import handler.VisitsHandler;


public class ShowPersonActivity extends Activity {
    String patientId;
    Patient patient;
    public static final String EXTRA_MESSAGE = "com.example.jakub.ortodb";
    private PatientsHandler patientsHandler;
    private VisitsHandler visitsHandler;
    private MainCursorAdapter cursorAdapter;
    private FilterQueryProvider myQueryProvider = new FilterQueryProvider() {
        @Override
        public Cursor runQuery(CharSequence constraint) {
            String query;
            query = "SELECT * FROM " + DbHandler.VisitsEntry.TABLE_NAME
                    + " WHERE " + DbHandler.VisitsEntry.KEY_PATIENT_ID + "=" + patientId
                    + " ORDER BY " + DbHandler.VisitsEntry.KEY_DATA_BEG + " DESC ";
            return db.rawQuery(query, null);

        }
    };
    private SQLiteDatabase db;

    TextView textViewVisitsTitle;
    TextView textViewCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_person);


        patientsHandler = new PatientsHandler(this);
        visitsHandler = new VisitsHandler(this);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE); //id
        patientId = message;

        DbHandler handler = new DbHandler(this);
        db = handler.getWritableDatabase();
        ListView listView = (ListView) findViewById(R.id.visitsList);
        cursorAdapter = new MainCursorAdapter(this, myQueryProvider.runQuery(null), 0);
        listView.setAdapter(cursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyOnItemClick(view, position, id);
            }
        });

        updateViews();
    }

    private void MyOnItemClick(View view, int position, long id) {
        Visit visit = VisitsHandler.toVisit((Cursor) cursorAdapter.getItem(position), 0);
        Intent intent = new Intent(this, EditVisitActivity.class);
        intent.putExtra(EXTRA_MESSAGE, String.valueOf(visit.getId()));
        startActivity(intent);
    }

    public void updateViews() {
        patient = patientsHandler.getPatient(patientId);
        textViewVisitsTitle = (TextView) findViewById(R.id.visitsTitle);
        textViewCurrentDate = (TextView) findViewById(R.id.currentDate);
        textViewCurrentDate.setText(CalendarHandler.milisToDate(Calendar.getInstance().getTimeInMillis()));
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(patient.getFirstName() + " " + patient.getLastName());
        actionBar.setDisplayHomeAsUpEnabled(true);

        String title;
        title = cursorAdapter.getCount() == 0 ? "Brak Wizyt" : "Wizyty";
        textViewVisitsTitle.setText(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_person, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.editButton:
                intent = new Intent(this, EditPersonActivity.class);
                intent.putExtra(MainActivity.EXTRA_MESSAGE, String.valueOf(patientId));
                startActivity(intent);
                break;
            case R.id.deleteButton:
                DeletingPatientDialog dialog = new DeletingPatientDialog();
                dialog.show(getFragmentManager(), "patientDeleting");
                break;
            case R.id.addVisit:
                intent = new Intent(this, VisitActivity.class);
                intent.putExtra(MainActivity.EXTRA_MESSAGE, String.valueOf(patientId));
                startActivity(intent);
                break;
            case R.id.callButton:
                Uri uri = Uri.parse("tel:" + patient.getPhone());
                Intent callIntent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(callIntent);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cursorAdapter.changeCursor(myQueryProvider.runQuery(null));
        cursorAdapter.notifyDataSetChanged();
        updateViews();
    }

    private class MainCursorAdapter extends CursorAdapter {
        public MainCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.visit, parent, false);
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            TextView textViewVisit = (TextView) view.findViewById(R.id.visit);
            Visit visit = VisitsHandler.toVisit(cursor, 0);
            if (visit.getDoctor().equals("Basia"))
                textViewVisit.setTextColor(Color.rgb(255, 97, 3));
            else
                textViewVisit.setTextColor(Color.rgb(148, 176, 1));

            if (visit.getDataBeg() < Calendar.getInstance().getTimeInMillis())
                textViewVisit.setTextColor(Color.rgb(222, 222, 222));
            textViewVisit.setText(
                    CalendarHandler.milisToDateFullYear(visit.getDataBeg()) + " "
                            + "godz."
                            + CalendarHandler.milisToTime(visit.getDataBeg())
                            + "   " + visit.getDoctor());
        }
    }

    public class DeletingPatientDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Czy na pewno chcesz usunąć pacjenta?")
                    .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            visitsHandler.deletePatientVisits(patientId);
                            patientsHandler.deletePatient(patientId);
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
