package athletictracking.com.trackhlete.base;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.gui.SessionsFragment;
import athletictracking.com.trackhlete.gui.SettingsFragment;
import athletictracking.com.trackhlete.gui.TrackSessionFragment;
import athletictracking.com.trackhlete.infra.Linker;
import athletictracking.com.trackhlete.infra.Session;
import athletictracking.com.trackhlete.infra.Split;

public class MainActivity extends AppCompatActivity implements Linker, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {
    private static final String KEY_DISTANCE_FOR_SESSION = "key_sess_dist";
    private static final String KEY_TIME_FOR_SESSION = "key_sess_time";
    private static final String KEY_TIMER_RUNNING = "key_sess_timer";

    private static final double INVALID_TIME_SETTING = -1;
    private static final double IMPOSSIBLE_TRAVEL_SPEED = 30;   //Assumes running, not cycling

    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int REQUEST_CHECK_LOCATION_PREFERENCES = 2;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private static final String TAG = "debug_main";
    private static final int UPDATE_LIVE_INFORMATION = 1;
    private static double GLOBAL_DISTANCE_METRIC = 1000;
    private static final double KILOMETER = 1000;
    private static final double MILE = 1609.34;

    private TrackSessionFragment mSessionFrag;
    private SessionsFragment mReviewSessionsFrag;
    private SettingsFragment mSettingsFrag;
    private GoogleMap mMap;
    private Marker currentPosOnMap;

    //Split data /////////////////////////////////////////////////////////
    private ArrayList<ArrayList<Double>> mLocations;
    private ArrayList<String> mPaces;
    private ArrayList<Double> mSpeeds;
    private ArrayList<Double> mElevs;
    private ArrayList<Split> mSplits;
    private boolean isTimerRunning;
    private Timer mTimer;

    private double mDistanceTraveled;   //Overall distance
    private double mSplitDist;      //Distance into current split
    private int mElapsedTime = 0;   //Overall time
    private int mSplitTime = 0;     //time into current split
    private double mElevChange;     //Overall elevation change
    private double mSplitElevChange = 0;
    private double mAvgSpeedForSplit = 0;
    private long mRecentUpateTime = 0;
    private long mOldUpdateTime = 0;
    private int mSpeedCalcs = 0;
    private int mNumSplits = 0;
    private double mMaxSpeed = 0;

    //Accelerometer //////////////////////////////////////////////////////
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private boolean hasAccelometer;

    //  GPS API things  //////////////////////////////////////////////////
    //The desired interval for location updates. Inexact. Updates may be more or less frequent.
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
    //The fastest rate for active location updates. Exact. Updates will never be more frequent
    //than this value.
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private GoogleApiClient mGoogleAPIClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrLocation;
    protected Location mPrevLocation;
    private boolean mFirstLoctionUpdate = true;
    //Tracks the status of the location updates request. Value changes when the user presses the
    //Start Updates and Stop Updates buttons.
    protected Boolean mRequestingLocationUpdates;
    //Time when the location was updated represented as a String.
    protected String mLastUpdateTime;

    private double mGForce = 0.0;
    ///////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        if (savedInstanceState != null) {
            mDistanceTraveled = savedInstanceState.getDouble(KEY_DISTANCE_FOR_SESSION);
            mElapsedTime = savedInstanceState.getInt(KEY_TIME_FOR_SESSION);
            isTimerRunning = savedInstanceState.getBoolean(KEY_TIMER_RUNNING);
        } else {
            //get locations? mLocations
            mDistanceTraveled = 0;
            isTimerRunning = false; //TODO: careful of this
            mLocations = new ArrayList<>();
            mPaces = new ArrayList<>();
            mSpeeds = new ArrayList<>();
            mSplits = new ArrayList<>();
            mElevs = new ArrayList<>();
        }
        checkForAccelerometer();

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleAPIClient();
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        if (mSessionFrag == null) {
            mSessionFrag = new TrackSessionFragment();
            mSettingsFrag = new SettingsFragment();
            mReviewSessionsFrag = new SessionsFragment();
        }

        //BottomBar is the navigation tool used in the app.
        BottomBar bottomBar = (BottomBar) findViewById(R.id.activity_main_bottombar);
        bottomBar.setDefaultTabPosition(1);     // Set the tab that is selected by default when the user logs in for the first time.
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                FragmentManager fragmentManager;
                fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                if (tabId == R.id.tab_profile) {
                    ft.replace(R.id.activity_main_fragment_container, mSettingsFrag, getString(R.string.bottom_bar_id)).commit();
                } else if (tabId == R.id.tab_activity) {
                    ft.replace(R.id.activity_main_fragment_container, mSessionFrag, getString(R.string.bottom_bar_id)).commit();

                } else if (tabId == R.id.tab_review) {
                    ft.replace(R.id.activity_main_fragment_container, mReviewSessionsFrag, getString(R.string.bottom_bar_id)).commit();
                }
            }
        });
    }

    private void checkForAccelerometer() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mAccel == null) {
            // Use the accelerometer.
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                hasAccelometer = true;
            } else {
                hasAccelometer = false;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates fields based on data stored in the bundle. Most of the GPS code in here came
     * straight from Google code labs
     * <p>
     * But.. it works :)
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {

            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                //setButtonsEnabledState();
            }

            // Update the value of mCurrLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrLocation
                // is not null.
                mCurrLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
        }
    }

    protected synchronized void buildGoogleAPIClient() {
        mGoogleAPIClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        getUserLocationFromRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Google's stuff, copied from their tutorial
    protected void getUserLocationFromRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleAPIClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates k = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        checkLocationPermissions();
                        Log.d(TAG, "Starting location updates");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            Log.d(TAG, "Resolution required, dont have access to GPS");
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    //Yeah this is my code though.. not google's.
    private void checkLocationPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) { //Dont care if they have access, ask them anyways

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Permission Needed",        //Make pop-up dialog asking for permissions
                        "We need to access your GPS to use for matching",
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        REQUEST_CHECK_LOCATION_PREFERENCES
                );
            } else {
                Log.d(TAG, "Not asking, just getting Loc");
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CHECK_LOCATION_PREFERENCES);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    //Show explanation for any type of permission, re-usable code.
    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CHECK_LOCATION_PREFERENCES: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Starting GPS Update");
                    startLocationUpdates();
                } else {
                    //Nothing
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location update recieved");
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleAPIClient, mLocationRequest, this);

        } else {
            Log.d(TAG, "Location update failed");
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPIClient, this);
    }

    protected void onStart() {
        super.onStart();
        mGoogleAPIClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleAPIClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);

    }

    protected void onStop() {
        super.onStop();
        if (mGoogleAPIClient.isConnected())
            mGoogleAPIClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (mCurrLocation != null)
            stopLocationUpdates();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected to GoogleApiClient");

        //Don;t do this if no permission or last location is null.
        if (mCurrLocation == null &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mCurrLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleAPIClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        mGoogleAPIClient.connect();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        ArrayList<Double> newLoc = new ArrayList<>();
        mPrevLocation = mCurrLocation;
        mCurrLocation = location;

        newLoc.add(mCurrLocation.getLatitude());
        newLoc.add(mCurrLocation.getLongitude());
        mLocations.add(newLoc);  //Record all locations visited, will be stored in DB later

        mOldUpdateTime = mRecentUpateTime;
        mRecentUpateTime = new Date().getTime() / 1000; //Used for calculting avg. speed later

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
       /* Log.d(TAG, "Old time: " + mOldUpdateTime);
        Log.d(TAG, "New time: " + mRecentUpateTime);*/
        Log.d(TAG, "Lat: " + mCurrLocation.getLatitude());
        Log.d(TAG, "Lon: " + mCurrLocation.getLongitude());

        if (currentPosOnMap != null) {
            currentPosOnMap.remove();   //Prevents multiple markers being placed on map with each loc update
        }
        LatLng latLng = new LatLng(mCurrLocation.getLatitude(), mCurrLocation.getLongitude());

        //Show the updated marker on the map
        if (mMap != null) {
            currentPosOnMap = mMap.addMarker(new MarkerOptions().position(latLng));
            currentPosOnMap.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            // Showing the current location in Google Map.
            if (mFirstLoctionUpdate) {  // Focuses the map view to the users current location, only for the first location update
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                mFirstLoctionUpdate = false;    //Only zoom to location for first update
            }
        }
        Log.d(TAG, "Distance travelled: "+  mDistanceTraveled);
        displaySessionChangesCallback();
    }

    private void analyseRunningData() {
        if (mSplitDist >= GLOBAL_DISTANCE_METRIC) {
            Split split = new Split();
            split.setTime(mSplitTime);
            mSplits.add(split);

            mNumSplits++;
            mPaces.add(parseElapsedTime(mSplitTime));
            mSpeeds.add(mAvgSpeedForSplit / mSpeedCalcs);   //Add the average speed for this split
            mElevs.add(mSplitElevChange);

            Log.d(TAG, "Elevs: " + mElevs);
            Log.d(TAG, "Paces: " + mPaces);
            Log.d(TAG, "Speeds: " + mSpeeds);
            mSplitElevChange = 0;
            mSplitTime = 0; //TODO: refactor to be more accurate
            mSplitDist = 0;
            mSpeedCalcs = 0;
            mAvgSpeedForSplit = 0;
        }
    }

    private void displaySessionChangesCallback() {
        if (mPrevLocation != null) {
            float[] res = new float[1]; //Haversine value stored in here
            Location.distanceBetween(mCurrLocation.getLatitude(), mCurrLocation.getLongitude(), mPrevLocation.getLatitude(), mPrevLocation.getLongitude(), res);

            if (!impossibleLocationChange(res[0])) {    //If speed for travel is > 30kph don't update, might need refactoring
                updateSplitInfo(res[0]);
                calcAvgSpeedSoFar(res[0]);
                analyseRunningData();
                Log.d(TAG, "distance traveled: " + res[0]);

                if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null &&
                        mSessionFrag.isVisible())
                    mSessionFrag.updateDistanceTraveledCallback();
            }
        }
    }

    private boolean impossibleLocationChange(float distance) {
        double speed = distance / getTimeSinceLastUpdate();
        if (speed > IMPOSSIBLE_TRAVEL_SPEED)
            return true;
        return false;
    }

    private void updateSplitInfo(float distance) {
        mDistanceTraveled += distance;
        mSplitDist += distance;
        if (mPrevLocation != null) {
            double elevChangeFromUpdate = mCurrLocation.getAltitude() - mPrevLocation.getAltitude();
            mElevChange += elevChangeFromUpdate;
            mSplitElevChange += elevChangeFromUpdate;
        }
    }

    private void calcAvgSpeedSoFar(float distance) {
        double time = getTimeSinceLastUpdate();

        if (!(time == INVALID_TIME_SETTING)) {
            mAvgSpeedForSplit += distance / time;   //TODO: divide this by number average speeds later
            mSpeedCalcs++;                          //used later to get average speed for split
        }
    }

    private double getTimeSinceLastUpdate() {
        if (mOldUpdateTime == 0)
            return INVALID_TIME_SETTING;
        else
            return mRecentUpateTime - mOldUpdateTime;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleAPIClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {    //handles the timer thread

            if (msg.what == UPDATE_LIVE_INFORMATION &&
                    mSessionFrag.isVisible()) { //Update textView
                mSessionFrag.updateTimeCallback(mElapsedTime);
            }
        }
    };

    @Override
    public void initTimer() {
        mTimer = new Timer();
    }

    @Override
    public void startTimer() {
        isTimerRunning = true;
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mElapsedTime += 1; //increase every sec
                mSplitTime += 1;
                mHandle.obtainMessage(UPDATE_LIVE_INFORMATION).sendToTarget();
            }
        }, 0, 1000);
    }

    @Override
    public void stopTimer() {
        //Store data in db!
        mTimer.cancel();
        String date = getCurrentDate();
        String distance = getDistanceTraveledAsMetricString();
        Session session =
                new Session(date,
                        getDistanceTraveledAsMetricString(),
                        parseElapsedTime(mElapsedTime),
                        mNumSplits,
                        getAverageSpeedForSession(),
                        mMaxSpeed,
                        getOverallElevationChange(),
                        mLocations,
                        mPaces,
                        mElevs,
                        mSpeeds);

        mDistanceTraveled = 0;
        mElapsedTime = 0;
        isTimerRunning = false;
    }

    @Override
    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    @Override
    public boolean hasAccelerometer() {
        return hasAccelometer;
    }

    @Override
    public double getGForce() {
        return mGForce;
    }

    @Override
    public double getDistanceTraveled() {
        return mDistanceTraveled;
    }

    @Override
    public int getElapsedTime() {
        return mElapsedTime;
    }

    @Override
    public String parseElapsedTime(int time) {
        int hours, mins, seconds;
        hours = time / 3600;
        mins = time / 60;
        seconds = time - (mins * 60);

        String minuteString = (mins + ":" + ((seconds < 10) ? ("0" + seconds) : seconds));

        return (hours >= 1 && hours < 10) ? ("0" + hours + ":" + minuteString) : minuteString;
    }

    private double getOverallElevationChange() {
        double overallElevChange = 0.0;
        for (double elevChange : mElevs) {
            overallElevChange += elevChange;
        }
        return overallElevChange;
    }

    private double getAverageSpeedForSession() {
        double sum = 0.0;
        for (double avgSpeedForSplit : mSpeeds) {
            sum += avgSpeedForSplit;
            if (avgSpeedForSplit > mMaxSpeed)
                mMaxSpeed = avgSpeedForSplit;
        }
        return sum / mSpeeds.size();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location;

        Log.d(TAG, "map ready");
        // For showing a move to my location button
        //googleMap.setMyLocationEnabled(true);

        if (mCurrLocation != null) {
            location = new LatLng(mCurrLocation.getLatitude(), mCurrLocation.getLongitude());
        } else {
            location = new LatLng(0, 0);
        }

        currentPosOnMap = googleMap.addMarker(new MarkerOptions().position(location).title("Marker Title").snippet("Marker Description"));
        currentPosOnMap.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    // Accelerometer tracking   /////////////////////////////
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float ax, ay, az;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax = sensorEvent.values[0];
            ay = sensorEvent.values[1];
            az = sensorEvent.values[2];

            mGForce = Math.sqrt((ax * ax) + (ay * ay) + (az * az)) - 9.8;
            //Log.d(TAG, "GFORCE: " + mGForce);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstaceState) {
        savedInstaceState.putDouble(KEY_DISTANCE_FOR_SESSION, mDistanceTraveled);
        savedInstaceState.putInt(KEY_TIME_FOR_SESSION, mElapsedTime);
        savedInstaceState.putBoolean(KEY_TIMER_RUNNING, isTimerRunning);

    }

    private String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("DD MMM yy");
        return format.format(new Date());
    }

    private String getDistanceTraveledAsMetricString() {
        if (GLOBAL_DISTANCE_METRIC == KILOMETER) {
            return String.valueOf(mDistanceTraveled / KILOMETER);
        }
        return String.valueOf(mDistanceTraveled / MILE);
    }
}
