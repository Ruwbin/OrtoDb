package handler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jakub on 2015-08-14.
 */
public class VisitsHandler {
    private DbHandler dbHandler;
    SQLiteDatabase db;
    private Context context;

    public VisitsHandler(Context context) {
        this.context = context;
        dbHandler = new DbHandler(context);
    }

    public void open(){
        db = dbHandler.getWritableDatabase();
    }

    public void close(){
        dbHandler.close();
    }

    public long addVisit(Visit visit){
        open();
        ContentValues values = new ContentValues();
        values.put(DbHandler.VisitsEntry.KEY_DATA_BEG, visit.getDataBeg());
        values.put(DbHandler.VisitsEntry.KEY_DATA_END, visit.getDataEnd());
        values.put(DbHandler.VisitsEntry.KEY_PURPOSE, visit.getPurpose());
        values.put(DbHandler.VisitsEntry.KEY_DOCTOR, visit.getDoctor());
        values.put(DbHandler.VisitsEntry.KEY_PATIENT_ID, visit.getPatient());
        values.put(DbHandler.VisitsEntry.KEY_GOOGLE_CALENDAR_ID, visit.getGoogleCalendarId());

        long idRet = -1;
        try {
            idRet = db.insert(DbHandler.VisitsEntry.TABLE_NAME, null, values);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        close();
        visit.setId(idRet);
        return idRet;
    }

    public List<Visit> readAllVisits(){
        open();
        LinkedList<Visit> visits = new LinkedList<>();

        Cursor cursor = db.query(DbHandler.VisitsEntry.TABLE_NAME,null,null,null,null,null,null,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            visits.add(toVisit(cursor, 0));
            cursor.moveToNext();
        }
        close();
        cursor.close();
        return visits;
    }

    public Visit getVisit(String id){
        open();
        Visit visit;
        Cursor cursor = db.query(DbHandler.VisitsEntry.TABLE_NAME,null, DbHandler.VisitsEntry._ID + "=" +id,null,null,null,null);
        cursor.moveToFirst();
        visit = toVisit(cursor, 0);
        close();
        cursor.close();

        return visit;
    }

    public void updateVisit(Visit visit){
        open();
        ContentValues values = new ContentValues();
        values.put(DbHandler.VisitsEntry.KEY_DATA_BEG, visit.getDataBeg());
        values.put(DbHandler.VisitsEntry.KEY_DATA_END, visit.getDataEnd());
        values.put(DbHandler.VisitsEntry.KEY_PURPOSE, visit.getPurpose());
        values.put(DbHandler.VisitsEntry.KEY_DOCTOR, visit.getDoctor());
        values.put(DbHandler.VisitsEntry.KEY_PATIENT_ID, visit.getPatient());
        values.put(DbHandler.VisitsEntry.KEY_GOOGLE_CALENDAR_ID, visit.getGoogleCalendarId());

        db.update(DbHandler.VisitsEntry.TABLE_NAME, values, DbHandler.VisitsEntry._ID + "=" + visit.getId(), null);
        close();
    }

    public static Visit toVisit(Cursor cursor, int shift) {
        return new Visit(cursor.getLong(0+shift),cursor.getLong(1+shift),cursor.getLong(2+shift),
                cursor.getString(3+shift),cursor.getLong(4+shift),cursor.getString(5+shift),cursor.getLong(6+shift));
    }

    public void deletePatientVisits(String patientId) {
        open();
        db.delete(DbHandler.VisitsEntry.TABLE_NAME, DbHandler.VisitsEntry.KEY_PATIENT_ID + "=" + patientId,null);
        close();
    }

    public void deleteVisit(String visitID) {
        open();
        db.delete(DbHandler.VisitsEntry.TABLE_NAME, DbHandler.VisitsEntry._ID + "=" + visitID, null);
        Toast toast = Toast.makeText(context, "Usunięto wizytę", Toast.LENGTH_SHORT);
        toast.show();
        close();
    }
}
