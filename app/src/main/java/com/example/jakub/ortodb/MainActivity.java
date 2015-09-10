package com.example.jakub.ortodb;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import handler.CalendarHandler;
import handler.DbHandler;
import handler.Patient;
import handler.PatientsHandler;
import handler.Visit;
import handler.VisitsHandler;

import android.app.ActionBar;
import android.widget.Toast;


public class MainActivity extends Activity {

    public static final String EXTRA_MESSAGE = "com.example.jakub.ortodb";

    PatientsHandler patientsHandler;
    private MainCursorAdapter cursorAdapter;
    String orderType;
    private FilterQueryProvider myQueryProvider = new FilterQueryProvider() {
        @Override
        public Cursor runQuery(CharSequence constraint) {
            String query;
            Calendar tempCalendar = Calendar.getInstance();
            String patientProjection = Arrays.toString(DbHandler.PatientEntry.PROJECTION)
                    .replace("[", "")
                    .replace("]", "");
            String visitProjection = Arrays.toString(DbHandler.VisitsEntry.PROJECTION)
                    .replace("[", "")
                    .replace("]", "");

            String query1 = " SELECT " + visitProjection + " , MIN(" + DbHandler.VisitsEntry.KEY_DATA_BEG + " )" +
                    " FROM " + DbHandler.VisitsEntry.TABLE_NAME +
                    " WHERE " + DbHandler.VisitsEntry.KEY_DATA_BEG + " >" + tempCalendar.getTimeInMillis() +
                    " GROUP BY " + DbHandler.VisitsEntry.KEY_PATIENT_ID;

            if (constraint == null)
                query = "SELECT * " +
                        " FROM " + DbHandler.PatientEntry.TABLE_NAME +
                        " LEFT JOIN " + "(" + query1 + ")" +
                        " ON " + DbHandler.PatientEntry.TABLE_NAME + "." + DbHandler.PatientEntry._ID + " = " + DbHandler.VisitsEntry.KEY_PATIENT_ID +
                        " ORDER BY " + orderType;
            else
                query = "SELECT * " +
                        " FROM " + DbHandler.PatientEntry.TABLE_NAME +
                        " LEFT JOIN " + "(" + query1 + ")" +
                        " ON " + DbHandler.PatientEntry.TABLE_NAME + "." + DbHandler.PatientEntry._ID + " = " + DbHandler.VisitsEntry.KEY_PATIENT_ID +
                        " WHERE " + DbHandler.PatientEntry.KEY_LAST_NAME + " LIKE \'%" + constraint + "%\'" +
                        " ORDER BY " + orderType;


            //todo queryBuilder
            System.out.println(query + db.rawQuery(query, null).getCount());
            return db.rawQuery(query, null);

        }
    };
    private SQLiteDatabase db;
    ProgressBar progressBar;
    private String activityConstraint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        activityConstraint = null;
        patientsHandler = new PatientsHandler(this);
        DbHandler handler = new DbHandler(this);
        db = handler.getWritableDatabase();
        ListView listView = (ListView) findViewById(R.id.mainList);
        orderType = DbHandler.PatientEntry.KEY_LAST_NAME;
        cursorAdapter = new MainCursorAdapter(this, myQueryProvider.runQuery(null), 0);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLoading);

        listView.setAdapter(cursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyOnItemClick(view, position, id);
            }
        });

    }


    public static class AlarmService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            DbHandler handler = new DbHandler(this);
            SQLiteDatabase db = handler.getWritableDatabase();
            Calendar begDay = Calendar.getInstance();
            Calendar endDay;
            begDay.set(Calendar.HOUR, 0);
            begDay.set(Calendar.MINUTE, 0);
            begDay.set(Calendar.SECOND, 0);
            begDay.set(Calendar.MILLISECOND, 0);

            begDay.add(Calendar.DAY_OF_MONTH, 1);
            endDay = (Calendar) begDay.clone();
            endDay.add(Calendar.DAY_OF_MONTH, 1);

            String query2;
            query2 = "SELECT * FROM " + DbHandler.VisitsEntry.TABLE_NAME +
                    " WHERE " + DbHandler.VisitsEntry.KEY_DATA_BEG +
                    " BETWEEN " + begDay.getTimeInMillis() +
                    " AND " + endDay.getTimeInMillis() +
                    " ORDER BY " + DbHandler.VisitsEntry.KEY_DATA_BEG;

            Cursor cursor = db.rawQuery(query2, null);
            NotificationCompat.Builder mBuilder;
            Intent resultIntent;
            PendingIntent resultPendingIntent;
            Uri alarmSound;

            if (cursor.getCount() == 0) {
                mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_orto)
                        .setContentTitle("Wreszcie wolne ;)");
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            } else {
                mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_orto)
                        .setContentTitle("Pamiętaj o wysłaniu wiadomości!")
                        .setContentText("Kliknij, żeby przejść do kreatora wiadomości.");
                resultIntent = new Intent(getApplicationContext(), MessageActivity.class);
                resultPendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
                mBuilder.setContentIntent(resultPendingIntent);
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }

            mBuilder.setAutoCancel(true);
            mBuilder.setSound(alarmSound);
            mBuilder.setVibrate(new long[]{0, 1000, 1000, 1000, 1000});
            mBuilder.setLights(Color.YELLOW, 3000, 3000);
            mBuilder.setPriority(2);
            int mNotificationId = 001;

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(mNotificationId, mBuilder.build());

            return super.onStartCommand(intent, flags, startId);

        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
                setAlarm(context);
        }

        public void setAlarm(Context context) {

            AlarmManager alarmManager = (AlarmManager)
                    context.getSystemService(context.ALARM_SERVICE);

            Intent intent = new Intent(context, AlarmService.class);

            PendingIntent alarmIntent = PendingIntent.getService(context, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            calendar.set(Calendar.MINUTE, 00);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                activityConstraint = query;
                updateCursor(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                activityConstraint = newText;
                updateCursor(newText);
                return false;
            }
        });

        return true;
    }

    public TreeSet<ContactData> getContactList() {

        final String[] GROUP_PROJECTION = new String[]{
                ContactsContract.Groups._ID, ContactsContract.Groups.TITLE, ContactsContract.Groups.SUMMARY_COUNT};
        Cursor cursor = getContentResolver().query(
                ContactsContract.Groups.CONTENT_SUMMARY_URI, GROUP_PROJECTION, ContactsContract.Groups.DELETED + "!='1' AND " +
                        ContactsContract.Groups.GROUP_VISIBLE + "!='0' " + "AND " + ContactsContract.Groups.TITLE + " LIKE " + "'Orto%'",
                null, ContactsContract.Groups.TITLE);

        List<MyGroup> groups = new LinkedList<MyGroup>();

        while (cursor.moveToNext()) {
            MyGroup group = new MyGroup();

            group.id = Integer.valueOf(cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Groups._ID)));

            group.title = (cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Groups.TITLE)));

            group.amount = Integer.valueOf((cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT))));

            groups.add(group);
            // Log.d("GrpId  Title", gObj.getGroupIdList() +
            // gObj.getGroupTitle());
        }

        TreeSet<ContactData> contactDatas = new TreeSet<ContactData>();

        for (MyGroup group : groups) {
            Uri groupURI = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};

            Cursor c = getContentResolver().query(
                    groupURI,
                    projection,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                            + "=" + group.id, null, null);

            while (c.moveToNext()) {
                String id = c
                        .getString(c
                                .getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                Cursor pCur = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);

                while (pCur.moveToNext()) {
                    ContactData data = new ContactData();
                    data.name = pCur
                            .getString(pCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                    data.phone = pCur
                            .getString(pCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    contactDatas.add(data);
                }

                pCur.close();

            }
            System.out.println("group " + group.title + group.id + " have " + group.amount);

        }

        PatientsHandler patientsHandler = new PatientsHandler(this);
        List<Patient> patientList = patientsHandler.readAllPatients();
        for (Patient patient : patientList) {
            ContactData contactData = new ContactData();
            contactData.phone = patient.getPhone();
            if (patient.getFirstName() == null)
                contactData.name = patient.getLastName();
            else
                contactData.name = patient.getLastName() + " " + patient.getFirstName();
            contactDatas.remove(contactData);
        }


        System.out.println(contactDatas.size() + " " + contactDatas);
        return contactDatas;
    }

    protected void MyOnItemClick(View v, int position, long id) {
        Patient patient = PatientsHandler.toPatient((Cursor) cursorAdapter.getItem(position), 0);
        Intent intent = new Intent(this, ShowPersonActivity.class);
        intent.putExtra(EXTRA_MESSAGE, String.valueOf(patient.getId()));
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(this, EditPersonActivity.class);
                startActivity(intent);
                break;
            case R.id.sendSms:
                Intent intent1 = new Intent(this, MessageActivity.class);
                startActivity(intent1);
                break;
            case R.id.importContacts:

                new MyTask().execute();
                break;
            case R.id.sortChange:
                orderType = orderType.equals(DbHandler.PatientEntry.KEY_LAST_NAME) ?
                        "CASE WHEN " + DbHandler.VisitsEntry.KEY_DATA_BEG + " IS NULL THEN 1 ELSE 0 END, " + DbHandler.VisitsEntry.KEY_DATA_BEG :
                        DbHandler.PatientEntry.KEY_LAST_NAME;
                updateCursor(null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void updateCursor(String constraint){
        cursorAdapter.changeCursor(myQueryProvider.runQuery(constraint));
        cursorAdapter.notifyDataSetChanged();
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Pacjenci (" + cursorAdapter.getCount() + ")");
    }

    @Override
    protected void onResume() {
        updateCursor(null);
        super.onResume();
    }


    public class MyTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            TreeSet<ContactData> treeSet = getContactList();
            for (ContactData contactData : treeSet) {
                String firstName;
                String lastName = contactData.name.split(" ")[0];
                firstName = contactData.name.split(" ").length > 1 ? contactData.name.split(" ", 2)[1] : "";
                Patient patient = new Patient(0, firstName, lastName, contactData.phone);
                patientsHandler.addPatient(patient);
            }

            return treeSet.size();
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.GONE);
            onResume();
            int value = (int) o;
            String text = "";
            switch (value) {
                case 1:
                    text = "Zaimportowano jeden kontakt.";
                    break;
                case 2:
                case 3:
                case 4:
                    text = "Zaimportowano " + value + " kontakty.";
                    break;
                default:
                    text = "Zaimportowano " + value + " kontaktów.";

            }
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    private class MainCursorAdapter extends CursorAdapter {
        public MainCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.patient, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            TextView textViewFirstName = (TextView) view.findViewById(R.id.patientFirstName);
            TextView textViewLastName = (TextView) view.findViewById(R.id.patientLastName);
            TextView textViewVisit = (TextView) view.findViewById(R.id.patientVisit);

            Patient patient = PatientsHandler.toPatient(cursor, 0);
            Visit visit = VisitsHandler.toVisit(cursor, 6);

            String firstName = patient.getFirstName();
            String lastName = patient.getLastName();
            Long visitDate = visit.getDataBeg();

            textViewFirstName.setText(firstName);
            textViewLastName.setText(lastName);
            textViewVisit.setText(visitDate == 0 ? "Brak wizyt" : "Najbliższa wizyta " + CalendarHandler.milisToFullDate(visitDate));


        }
    }

    private class MyGroup {
        public int id;
        public String title;
        public int amount;

        @Override
        public String toString() {
            return "grupa " + title + " " + amount;
        }

    }

    class ContactData implements Comparable {
        public String phone, name;


        @Override
        public String toString() {
            return "kontakt*" + phone + "*" + name + "*";
        }

        @Override
        public int compareTo(Object another) {
            ContactData another2 = (ContactData) another;
            return name.replace(" ", "").compareTo(another2.name.replace(" ", ""));
        }
    }


}
