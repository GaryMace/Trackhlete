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
import java.util.List;

/**
 * Created by gary on 15/01/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "debug_db";
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
    private static final String KEY_ELEVS = "sess_elevs";
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
                KEY_OVERALL_DIST + " TEXT, " +
                KEY_OVERALL_TIME + " TEXT, " +
                KEY_NUM_SPLITS + " REAL, " +
                KEY_AVG_SPEED + " REAL, " +
                KEY_MAX_SPEED + " REAL, " +
                KEY_ELEV_GAIN + " REAL, " +
                KEY_GPS_POSITIONS + " BLOB, " +
                KEY_PACES + " BLOB, " +
                KEY_ELEVS + " BLOB, " +
                KEY_AVG_SPEEDS + " BLOB )";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUNDATA);
        onCreate(db);
    }

    public void addSession(Session session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, session.getDate());
        values.put(KEY_OVERALL_DIST, session.getOverallDistance());
        values.put(KEY_OVERALL_TIME, session.getOverallTime());
        values.put(KEY_NUM_SPLITS, session.getNumSplits());
        values.put(KEY_AVG_SPEED, session.getAverageSpeed());
        values.put(KEY_MAX_SPEED, session.getMaxSpeed());
        values.put(KEY_ELEV_GAIN, session.getOverallElevChange());
        values.put(KEY_GPS_POSITIONS, createByteArray(session.getSessionGPS()));
        values.put(KEY_PACES, createByteArray(session.getPaces()));
        values.put(KEY_ELEVS, createByteArray(session.getElevations()));
        values.put(KEY_AVG_SPEEDS, createByteArray(session.getSplitSpeeds()));

        db.insert(TABLE_RUNDATA, null, values);
        Log.d(TAG, "put in: " + values);
        db.close();
    }

    public List<Session> readAllSessions() {
        List<Session> sessions = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_RUNDATA;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(R_ID));
                String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
                String overallDist = cursor.getString(cursor.getColumnIndex(KEY_OVERALL_DIST));
                String overallTime = cursor.getString(cursor.getColumnIndex(KEY_OVERALL_TIME));
                int numSplits = cursor.getInt(cursor.getColumnIndex(KEY_NUM_SPLITS));
                double avgSpeed = cursor.getDouble(cursor.getColumnIndex(KEY_AVG_SPEED));
                double maxSpeed = cursor.getDouble(cursor.getColumnIndex(KEY_MAX_SPEED));
                double elevGain = cursor.getDouble(cursor.getColumnIndex(KEY_ELEV_GAIN));
                ArrayList<ArrayList<Double>> gpsPositions = decodeByteArrayForGPSPositions(cursor.getBlob(cursor.getColumnIndex(KEY_GPS_POSITIONS)));
                ArrayList<String> paces = decodeByteArrayForStrings(cursor.getBlob(cursor.getColumnIndex(KEY_PACES)));
                ArrayList<Double> elevs = decodeByteArrayForDoubles(cursor.getBlob(cursor.getColumnIndex(KEY_ELEVS)));
                ArrayList<Double> avgSpeeds = decodeByteArrayForDoubles(cursor.getBlob(cursor.getColumnIndex(KEY_AVG_SPEEDS)));

                sessions.add(
                        new Session(id,
                                date,
                                overallDist,
                                overallTime,
                                numSplits,
                                avgSpeed,
                                maxSpeed,
                                elevGain,
                                gpsPositions,
                                paces,
                                elevs,
                                avgSpeeds));
            } while (cursor.moveToNext());
        }
        db.close();
        return sessions;
    }

    private void deleteSessionById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RUNDATA, R_ID + " = " + id, null);
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

    private ArrayList<Double> decodeByteArrayForDoubles(byte[] bytes) {
        ArrayList<Double> list = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            list = (ArrayList<Double>) ois.readObject();
        } catch (IOException e) {
            Log.d(null, "Problem in decodeByteArray");
        } catch (ClassNotFoundException e) {
            Log.d(null, "Problem in decodeByteArray");
        }

        return list;
    }

    private ArrayList<String> decodeByteArrayForStrings(byte[] bytes) {
        ArrayList<String> list = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            list = (ArrayList<String>) ois.readObject();
        } catch (IOException e) {
            Log.d(null, "Problem in decodeByteArray");
        } catch (ClassNotFoundException e) {
            Log.d(null, "Problem in decodeByteArray");
        }

        return list;
    }

    private ArrayList<ArrayList<Double>> decodeByteArrayForGPSPositions(byte[] bytes) {
        ArrayList<ArrayList<Double>> list = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            list = (ArrayList<ArrayList<Double>>) ois.readObject();
        } catch (IOException e) {
            Log.d(null, "Problem in decodeByteArray");
        } catch (ClassNotFoundException e) {
            Log.d(null, "Problem in decodeByteArray");
        }

        return list;
    }
}
