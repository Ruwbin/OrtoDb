package handler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jakub on 2015-07-29.
 */
public class PatientsHandler {
    private DbHandler dbHandler;
    SQLiteDatabase db;
    private Context context;

    public PatientsHandler(Context context) {
        this.context = context;
        dbHandler = new DbHandler(context);
    }

    public void open() {
        db = dbHandler.getWritableDatabase();
    }

    public void close() {
        dbHandler.close();
    }

    public long addPatient(Patient patient) {
        open();
        ContentValues values = new ContentValues();
        values.put(DbHandler.PatientEntry.KEY_FIRST_NAME, patient.getFirstName());
        values.put(DbHandler.PatientEntry.KEY_LAST_NAME, patient.getLastName());
        values.put(DbHandler.PatientEntry.KEY_PHONE, patient.getPhone());
        long idRet = -1;
        try {
            idRet = db.insert(DbHandler.PatientEntry.TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
        patient.setId(idRet);
        return idRet;
    }

    public List<Patient> readAllPatients() {
        open();
        LinkedList<Patient> patients = new LinkedList<>();

        Cursor cursor = db.query(DbHandler.PatientEntry.TABLE_NAME, null, null, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            patients.add(toPatient(cursor, 0));
            cursor.moveToNext();
        }
        close();
        cursor.close();
        return patients;
    }

    public Patient getPatient(String id) {
        open();
        Patient patient;
        Cursor cursor = db.query(DbHandler.PatientEntry.TABLE_NAME, null, DbHandler.PatientEntry._ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();
        patient = toPatient(cursor, 0);
        close();
        cursor.close();

        return patient;
    }

    public void updatePatient(Patient patient) {
        open();
        ContentValues values = new ContentValues();
        //values.put(dbHandler._ID,patient.getId());
        values.put(DbHandler.PatientEntry.KEY_FIRST_NAME, patient.getFirstName());
        values.put(DbHandler.PatientEntry.KEY_LAST_NAME, patient.getLastName());
        values.put(DbHandler.PatientEntry.KEY_PHONE, patient.getPhone());

        db.update(DbHandler.PatientEntry.TABLE_NAME, values, dbHandler._ID + "=" + patient.getId(), null);
        close();
    }

    public static Patient toPatient(Cursor cursor, int shift) {
        return new Patient(cursor.getLong(0 + shift), cursor.getString(1 + shift), cursor.getString(2 + shift), cursor.getString(3 + shift));
    }


    public void deletePatient(String patientID) {
        open();
        db.delete(DbHandler.PatientEntry.TABLE_NAME, dbHandler._ID + "=" + patientID, null);
        Toast toast = Toast.makeText(context, "Usunięto pacjenta", Toast.LENGTH_SHORT);
        toast.show();
        close();
    }
}
