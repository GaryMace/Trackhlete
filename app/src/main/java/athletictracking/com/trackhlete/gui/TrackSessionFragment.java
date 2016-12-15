package athletictracking.com.trackhlete.gui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.Timer;
import java.util.TimerTask;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.infr.Linker;

/**
 * Created by gary on 11/12/16.
 */

public class TrackSessionFragment extends Fragment {
    private static final String TAG = "debug_ts";
    private static final int DISTANCE_KM = 1000;
    private static final double DISTANCE_MILE = 1609.34;
    private boolean hasBeenPressed;
    private SupportMapFragment fragment;
    private TextView mDurationTV;
    private TextView mDistanceTV;
    private Linker linker;

    private double mDistanceTraveled;
    private int mElapsedTime;
    private boolean isTimerRunning;
    private Timer mTimer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the view
        View view = inflater.inflate(R.layout.fragment_track_session, container, false);
        linker = (Linker) getActivity();

        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
            .findFragmentById(R.id.map);
        mapFragment.getMapAsync(linker);


        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDurationTV = (TextView) view.findViewById(R.id.fragment_ts_timer);
        mDistanceTV = (TextView) view.findViewById(R.id.fragment_ts_distance);


        final Button button = (Button) view.findViewById(R.id.fragment_ts_start_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasBeenPressed) {
                    hasBeenPressed = true;
                    button.setText("Finish");
                    button.setBackgroundResource(R.drawable.fragment_start_session_pressed);
                    mTimer = new Timer();

                    startTimer();
                } else {
                    hasBeenPressed = false;
                    button.setText("Start");
                    button.setBackgroundResource(R.drawable.fragment_start_session_unpressed);

                    if (isTimerRunning) {
                        mTimer.cancel();
                        mElapsedTime = 0;
                        isTimerRunning = false;
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fragment.getMapAsync(linker);
    }

    protected void startTimer() {
        isTimerRunning = true;
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mElapsedTime += 1; //increase every sec
                mHandle.obtainMessage(1).sendToTarget();
            }
        }, 0, 1000);
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {    //handles the timer thread

            if (msg.what == 1) { //Update textView
                String elapsedTime = parseElapsedTime();
                mDurationTV.setText(elapsedTime);
            }
        }
    };

    private String parseElapsedTime() {
        int mins, seconds;
        mins = mElapsedTime / 60;
        seconds = mElapsedTime - (mins * 60);

        return mins + ":" + ((seconds < 10) ? ("0" + seconds) : seconds);
    }

    public void updateDistanceTraveledCallback(double newDist) {
        if (hasBeenPressed) {
            mDistanceTraveled += newDist;

            String distanceForSession = parseDistanceTraveled();
            mDistanceTV.setText(distanceForSession);
        }
    }

    //TODO: change to allow for miles later ... how?
    private String parseDistanceTraveled() {
        int metricWhole, metricFraction;

        metricWhole = (int) (mDistanceTraveled / DISTANCE_KM);
        double fraction = (mDistanceTraveled - (metricWhole * DISTANCE_KM)) / 10;
        metricFraction = (int) fraction;

        return metricWhole + "." + ((metricFraction < 10) ? ("0" + metricFraction) : metricFraction);
    }
}
