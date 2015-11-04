package com.example.jakub.ortodb;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import handler.CalendarHandler;
import handler.DbHandler;
import handler.Patient;
import handler.PatientsHandler;
import handler.Visit;
import handler.VisitsHandler;


public class MessageActivity extends Activity {

    private MessageCursorAdapter messageCursorAdapter;
    private PatientsHandler patientsHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Jutrzejsze wizyty");
        actionBar.setDisplayHomeAsUpEnabled(true);


        {
            DbHandler handler = new DbHandler(this);
            SQLiteDatabase db = handler.getWritableDatabase();
            Calendar begDay = Calendar.getInstance();
            Calendar endDay;
            begDay.set(Calendar.HOUR_OF_DAY, 0);
            begDay.set(Calendar.MINUTE, 0);
            begDay.set(Calendar.SECOND, 0);
            begDay.set(Calendar.MILLISECOND, 1);

            begDay.add(Calendar.DAY_OF_MONTH, 1);
            endDay = (Calendar) begDay.clone();
            endDay.add(Calendar.DAY_OF_MONTH, 1);

            String query;
            query = "SELECT * FROM " + DbHandler.VisitsEntry.TABLE_NAME +
                    " WHERE " + DbHandler.VisitsEntry.KEY_DATA_BEG +
                    " BETWEEN " + begDay.getTimeInMillis() +
                    " AND " + endDay.getTimeInMillis() +
                    " ORDER BY " + DbHandler.VisitsEntry.KEY_DATA_BEG;


           // System.out.println(CalendarHandler.milisToFullDate(begDay.getTimeInMillis()) + " " + begDay.getTimeInMillis());
           // System.out.println(CalendarHandler.milisToFullDate(endDay.getTimeInMillis()) + " " + endDay.getTimeInMillis());

            Cursor messageCursor = db.rawQuery(query, null);
            // Find ListView to populate



            ListView messageItems = (ListView) findViewById(R.id.messagelistView);
            messageCursorAdapter = new MessageCursorAdapter(this, messageCursor, 0);
            messageItems.setAdapter(messageCursorAdapter);
            messageItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MyOnItemClick(view, position, id);
                }
            });
            patientsHandler = new PatientsHandler(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void MyOnItemClick(View view, int position, long id) {
        System.out.println("pozycja " + position);
        Visit visit = VisitsHandler.toVisit((Cursor) messageCursorAdapter.getItem(position), 0);
        System.out.println("halo" + visit);
        Patient patient = patientsHandler.getPatient(visit.getPatient().toString());

        Uri uri = Uri.parse("smsto:" + patient.getPhone());
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);

        String smsBody = "Przypominamy o wizycie w Ortodenti, która odbędzie się dnia "
                + CalendarHandler.milisToDate(visit.getDataBeg()) + " o godzinie "
                + CalendarHandler.milisToTime(visit.getDataBeg()) + "."
                + " Zapraszamy.";
        smsIntent.putExtra("sms_body", smsBody);
        startActivity(smsIntent);


        System.out.println("wizyta " + patient);

    }

    public class MessageCursorAdapter extends CursorAdapter {

        public MessageCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.message_to_send, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView textViewFirstName = (TextView) view.findViewById(R.id.messageFirstName);
            TextView textViewLastName = (TextView) view.findViewById(R.id.messageLastName);
            TextView textViewMessage = (TextView) view.findViewById(R.id.message);

            Visit visit = VisitsHandler.toVisit(cursor, 0);
            Patient patient = patientsHandler.getPatient(visit.getPatient().toString());
            String firstName = patient.getFirstName();
            String lastName = patient.getLastName();
            long data = visit.getDataBeg();


            String message = "Przypominamy o wizycie w Ortodenti, która odbędzie się dnia "
                    + CalendarHandler.milisToDate(data) + " o godzinie "
                    + CalendarHandler.milisToTime(data) + "."
                    + " Zapraszamy.";


            textViewFirstName.setText(firstName);
            textViewLastName.setText(lastName);
            textViewMessage.setText(message);
        }

    }
}
