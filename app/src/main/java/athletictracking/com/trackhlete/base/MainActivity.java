package athletictracking.com.trackhlete.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import java.util.Date;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.gui.ProfileFragment;
import athletictracking.com.trackhlete.gui.SettingsFragment;
import athletictracking.com.trackhlete.gui.TrackSessionFragment;
import athletictracking.com.trackhlete.infra.Linker;

public class MainActivity extends AppCompatActivity implements Linker, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int REQUEST_CHECK_LOCATION_PREFERENCES = 2;
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private static final String TAG = "debug_main";

    private TrackSessionFragment mSessionFrag;
    private GoogleMap mMap;
    private Marker currentPosOnMap;

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
    ///////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleAPIClient();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });

        if (mSessionFrag == null) {
            mSessionFrag = new TrackSessionFragment();
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
                    ft.replace(R.id.activity_main_fragment_container, new ProfileFragment(), getString(R.string.bottom_bar_id)).commit();
                } else if (tabId == R.id.tab_activity) {
                    ft.replace(R.id.activity_main_fragment_container, new SettingsFragment(), getString(R.string.bottom_bar_id)).commit();

                } else if (tabId == R.id.tab_review) {
                    ft.replace(R.id.activity_main_fragment_container, mSessionFrag, getString(R.string.bottom_bar_id)).commit();
                }
            }
        });
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
     *
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
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleAPIClient.isConnected())
            mGoogleAPIClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        mPrevLocation = mCurrLocation;
        mCurrLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.d(TAG, "Lat: " + mCurrLocation.getLatitude());
        Log.d(TAG, "Lon: " + mCurrLocation.getLongitude());

        //TODO: implement GPS tracking logic here
        if (currentPosOnMap != null) {
            currentPosOnMap.remove();
        }
        LatLng latLng = new LatLng(mCurrLocation.getLatitude(), mCurrLocation.getLongitude());
        currentPosOnMap = mMap.addMarker(new MarkerOptions().position(latLng));
        currentPosOnMap.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        // Showing the current location in Google Map.

        if (mFirstLoctionUpdate) {  // Focuses the map view to the users current location.
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            mFirstLoctionUpdate = false;
        }

        displayDistanceTraveledCallback();
    }

    private void displayDistanceTraveledCallback() {
        if (mPrevLocation != null) {
            float[] res = new float[1]; //Haversine value stored in here
            Location.distanceBetween(mCurrLocation.getLatitude(), mCurrLocation.getLongitude(), mPrevLocation.getLatitude(), mPrevLocation.getLongitude(), res);

            Log.d(TAG, "distance traveled: " + res[0]);
            mSessionFrag.updateDistanceTraveledCallback(res[0]);
        }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location;

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
}
