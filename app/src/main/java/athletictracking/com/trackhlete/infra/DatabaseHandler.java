package athletictracking.com.trackhlete.infra;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gary on 15/01/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DatabaseRundata";
    private static final int DATABASE_VERSION = 7;

    private static final String R_ID = "r_id";
    private static final String TABLE_RUNDATA = "rundataTable";
    private static final String KEY_DATE = "date";
    private static final String KEY_OVERALL_DIST = "overall_dist";
    private static final String KEY_OVERALL_TIME = "overall_time";
    private static final String KEY_NUM_SPLITS = "num_splits";
    private static final String KEY_AVG_SPEED = "avg_speed";
    private static final String KEY_MAX_SPEED = "max_speed";
    private static final String KEY_ELEV_GAIN = "elev_gain";
    private static final String KEY_GPS_POSITIONS = "gps_pos";
    private static final String KEY_PACES = "sess_paces";
    private static final String KEY_AVG_SPEEDS = "sess_speeds";

    public DatabaseHandler(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RUN_DATA_TABLE = createRunDataTable();

        db.execSQL(CREATE_RUN_DATA_TABLE);
    }

    //Tables need to be edited to include failed reps somehow.
    private String createRunDataTable() {
        return "CREATE TABLE " + TABLE_RUNDATA + " ( " +
                R_ID + " INTEGER PRIMARY KEY, " +
                KEY_DATE + " TEXT, " +
                KEY_OVERALL_DIST+ " REAL, " +
                KEY_OVERALL_TIME + " REAL, " +
                KEY_NUM_SPLITS + " REAL, " +
                KEY_AVG_SPEED + " TEXT, " +
                KEY_MAX_SPEED + " REAL, " +
                KEY_ELEV_GAIN + " REAL, " +
                KEY_GPS_POSITIONS + " BLOB, " +
                KEY_PACES + " BLOB, " +
                KEY_AVG_SPEEDS + " BLOB )";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUNDATA);
        onCreate(db);
    }

    public void addSession(String email, String password, int rememberMe) {
        SQLiteDatabase db = this.getWritableDatabase();/*
        ContentValues values = new ContentValues();
        values.put(KEY_REMEMBER_EMAIL, email);
        values.put(KEY_REMEMBER_PASSWORD, password);
        values.put(KEY_REMEMBER_ME, rememberMe);
        db.insert(TABLE_REMEMBER_ME, null, values);
        Log.d(TAG, "put in: " + values);*/

        db.close();
    }

    private byte[] createByteArray(Object obj) {

        byte[] bArray = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objOstream = new ObjectOutputStream(baos);
            objOstream.writeObject(obj);
            bArray = baos.toByteArray();

        } catch (IOException e) {
            Log.d(null, "Problem in createByteArray");
        }

        return bArray;
    }

    private HashMap<String, ArrayList<Double>> decodeByteArrayForPoints(byte[] bytes) {
        HashMap<String, ArrayList<Double>> list = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            list = (HashMap<String, ArrayList<Double>>) ois.readObject();
        } catch (IOException e) {
            Log.d(null, "Problem in decodeByteArray");
        } catch (ClassNotFoundException e) {
            Log.d(null, "Problem in decodeByteArray");
        }

        return list;
    }
}
