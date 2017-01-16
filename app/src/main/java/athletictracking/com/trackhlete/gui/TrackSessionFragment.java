package athletictracking.com.trackhlete.gui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.infra.Linker;
import athletictracking.com.trackhlete.infra.Split;

/**
 * Created by gary on 11/12/16.
 */

public class TrackSessionFragment extends Fragment {
    private static final String KEY_SESSION_ONGOING = "key_session";

    private static final String TAG = "debug_ts";
    private static final int DISTANCE_KM = 1000;
    private static final double DISTANCE_MILE = 1609.34;
    private static final int THRESHOLD = 5;
    private boolean hasBeenPressed;
    private SupportMapFragment fragment;
    private TextView mDurationTV;
    private TextView mDistanceTV;
    private Linker linker;

    private double mDistanceTraveled;
    private int mElapsedTime;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the view
        View view = inflater.inflate(R.layout.fragment_track_session, container, false);
        linker = (Linker) getActivity();

        fragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
            .findFragmentById(R.id.map);
        fragment.getMapAsync(linker);
        mDistanceTraveled = 0;

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
                    sessionOngoing(button);
                    linker.initTimer();
                    linker.startTimer();
                } else {
                    hasBeenPressed = false;
                    sessionIdle(button);

                    if (linker.isTimerRunning()) {
                        //TODO: launch session finished frag
                        linker.stopTimer();
                        mElapsedTime = 0;
                        mDistanceTraveled = 0;
                        mDistanceTV.setText("0.00");
                        mDurationTV.setText("0:00");
                    }
                }
            }
        });

        if (hasBeenPressed) {
            mDistanceTraveled = linker.getDistanceTraveled();
            mDistanceTV.setText(parseDistanceTraveled());
            mElapsedTime = linker.getElapsedTime();
            mDurationTV.setText(linker.parseElapsedTime(mElapsedTime));
            sessionOngoing(button);
        } else {
            sessionIdle(button);
        }

        return view;
    }

    private void sessionIdle(Button button) {
        button.setText("Start");
        button.setBackgroundResource(R.drawable.fragment_start_session_unpressed);
    }

    public void sessionOngoing(Button button) {
        button.setText("Finish");
        button.setBackgroundResource(R.drawable.fragment_start_session_pressed);
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

    @Override
    public void onPause() {
        super.onPause();
    }

    private String parseElapsedTime() {
        int mins, seconds;
        mins = mElapsedTime / 60;
        seconds = mElapsedTime - (mins * 60);

        return mins + ":" + ((seconds < 10) ? ("0" + seconds) : seconds);
    }

    public void updateDistanceTraveledCallback() {
        mDistanceTraveled = linker.getDistanceTraveled();
        if (hasBeenPressed) {
            if (linker.hasAccelerometer()) {
                if (linker.getGForce() > THRESHOLD || linker.getGForce() < -THRESHOLD) {
                    String distanceForSession = parseDistanceTraveled();
                    mDistanceTV.setText(distanceForSession);
                }
            } else {
                String distanceForSession = parseDistanceTraveled();
                mDistanceTV.setText(distanceForSession);
            }
        }
    }

    public void updateTimeCallback(int time) {
        mElapsedTime = time;
        String elapsedTime = parseElapsedTime();
        mDurationTV.setText(elapsedTime);
    }

    //TODO: change to allow for miles later ... how?
    private String parseDistanceTraveled() {
        int metricWhole, metricFraction;

        metricWhole = (int) (mDistanceTraveled / DISTANCE_KM);
        double fraction = (mDistanceTraveled - (metricWhole * DISTANCE_KM)) / 10;
        metricFraction = (int) fraction;

        return metricWhole + "." + ((metricFraction < 10) ? ("0" + metricFraction) : metricFraction);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_SESSION_ONGOING, hasBeenPressed);
    }
}
