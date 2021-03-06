package handler;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.CalendarContract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Jakub on 2015-07-28.
 */
public class DbHandler extends SQLiteOpenHelper implements BaseColumns {


    public static final String DATABASE_NAME = "myBase.db";
    private static final int DATABASE_VERSION = 36;


    public static abstract class PatientEntry implements BaseColumns {
        public static final String TABLE_NAME = "patients";
        public static final String KEY_FIRST_NAME = "firstName";
        public static final String KEY_LAST_NAME = "lastName";
        public static final String KEY_PHONE = "phone";
        public static final String KEY_CONTACTS_ID = "contact_id";
        public static final String KEY_ADDITIONAL = "additional";


        private static final String TABLE_CREATE = "create table " + TABLE_NAME
                + "(" + _ID + " integer primary key, "
                + KEY_FIRST_NAME + " text, "
                + KEY_LAST_NAME + " text, "
                + KEY_PHONE + " text, "
                + KEY_CONTACTS_ID + " text, "
                + KEY_ADDITIONAL +  " text );";



        public static final String[] PROJECTION = new String[]{
                TABLE_NAME+"."+_ID,
                TABLE_NAME+"."+KEY_FIRST_NAME,
                TABLE_NAME+"."+KEY_LAST_NAME,
                TABLE_NAME+"."+KEY_PHONE,
        };
    }

    public static abstract class VisitsEntry implements BaseColumns {
        public static final String TABLE_NAME = "visits";
        public static final String KEY_DATA_BEG = "data_beg";
        public static final String KEY_DATA_END = "data_end";
        public static final String KEY_PURPOSE = "purpose";
        public static final String KEY_DOCTOR = "doctor";
        public static final String KEY_PATIENT_ID = "patient_id";
        public static final String KEY_GOOGLE_CALENDAR_ID = "google_calendar_id";



        private static final String TABLE_CREATE = "create table " + TABLE_NAME
                + "(" + _ID + " integer primary key, "
                + KEY_DATA_BEG + " integer, "
                + KEY_DATA_END + " integer, "
                + KEY_PURPOSE + " text, "
                + KEY_PATIENT_ID + " integer, "
                + KEY_DOCTOR + " text, "
                + KEY_GOOGLE_CALENDAR_ID + " integer, "
                + "FOREIGN KEY(" + KEY_PATIENT_ID + ") REFERENCES " + PatientEntry.TABLE_NAME + " (" + PatientEntry._ID + ") );";

        public static final String[] PROJECTION = new String[]{
                TABLE_NAME+"."+_ID,
                TABLE_NAME+"."+KEY_DATA_BEG,
                TABLE_NAME+"."+KEY_DATA_END,
                TABLE_NAME+"."+KEY_PURPOSE,
                TABLE_NAME+"."+KEY_DOCTOR,
                TABLE_NAME+"."+KEY_PATIENT_ID,
                TABLE_NAME+"."+KEY_GOOGLE_CALENDAR_ID
        };
    }


    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PatientEntry.TABLE_CREATE);
        db.execSQL(VisitsEntry.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PatientEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VisitsEntry.TABLE_NAME);
        onCreate(db);

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //onUpgrade(db, oldVersion, newVersion);
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void exportDatabse(String packageName) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//"+packageName+"//databases//"+DATABASE_NAME+"";
                String backupDBPath = "backupname2.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                try (BufferedReader br = new BufferedReader(new FileReader(backupDB))) {
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                System.out.println(sd.getPath());
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                System.out.println("Success");

            }
        } catch (Exception e) {

        }
    }
}
