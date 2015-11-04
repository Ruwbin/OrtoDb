package handler;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Jakub on 2015-08-02.
 */
public class CalendarHandler {
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
    };


    public static Long runQuery(Context context, Patient patient, Calendar begVisit, Calendar endVisit, Visit visit, int mode) {
        try {

            ContentResolver cr = context.getContentResolver();
            Uri uri = CalendarContract.Calendars.CONTENT_URI;
            String selection = "(" + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?)";
            String[] selectionArgs;
            if (visit.getDoctor().equals("Basia"))
                selectionArgs = new String[]{"Basia"};
            else
                selectionArgs = new String[]{"Orto"};

            Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
            cur.moveToFirst();
            long calID = cur.getLong(0);
            //adding event
            ContentValues values = new ContentValues();

            if (mode != 2) {
                long startMillis = begVisit.getTimeInMillis();
                long endMillis = endVisit.getTimeInMillis();

                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.TITLE, patient.getFirstName() + " " + patient.getLastName() + "  " + visit.getPurpose());
                values.put(CalendarContract.Events.DESCRIPTION, "Cel: " + visit.getPurpose());
                values.put(CalendarContract.Events.CALENDAR_ID, calID);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Warsaw");

                /*if (visit.getDoctor().equals("Basia"))
                    values.put(CalendarContract.Events.EVENT_COLOR, Color.rgb(255, 97, 3));
                else
                    values.put(CalendarContract.Events.EVENT_COLOR, Color.rgb(148, 176, 1));
                */

            }

            /*Cursor cursor = cr.query(CalendarContract.Events.CONTENT_URI, new String[]{"_id"}, "calendar_id=" + calID, null, null);
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(cursor.getColumnIndex("_id"));
                cr.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null, null);
            }
            cursor.close();*/

            switch (mode) {
                case 0: //add
                    Uri uriWithId = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    long eventID = Long.parseLong(uriWithId.getLastPathSegment());
                    Toast toast = Toast.makeText(context, "Zapisano wizytę na " + milisToFullDate(begVisit.getTimeInMillis()), Toast.LENGTH_SHORT);
                    toast.show();
                    return eventID;
                case 1: //update

                    Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, visit.getGoogleCalendarId());
                    int updatedRows = cr.update(updateUri, values, null, null);
                    //Log.i(DEBUG_TAG, "Rows updated: " + updatedRows);
                    break;
                case 2: //delete
                    Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, visit.getGoogleCalendarId());
                    int deletedRows = cr.delete(deleteUri, null, null);

                    //Log.i(DEBUG_TAG, "Rows deleted: " + deletedRows);
                    break;
            }


        } catch (Exception e) {
            Toast toast = Toast.makeText(context, "Operacja nie powiodła się", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }


        return null;
    }

    public static String milisToFullDate(Long milis) {
        return milisToDate(milis) + " " + milisToTime(milis);
    }

    public static String milisToDate(Long milis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String milisToTime(Long milis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String milisToDateFullYear(Long milis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return simpleDateFormat.format(calendar.getTime());
    }
}
