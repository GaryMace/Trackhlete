package athletictracking.com.trackhlete.infra;

import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by gary on 11/12/16.
 */

public interface Linker extends OnMapReadyCallback {
    void initTimer();

    void startTimer();

    void stopTimer();

    boolean isTimerRunning();

    boolean hasAccelerometer();

    double getGForce();

    double getDistanceTraveled();

    int getElapsedTime();

    String parseElapsedTime(int time);

    String getDistanceUnit();

    int getUnitSetting();

    double round(double value, int percision);

    String getSpeedUnit();

    void setToolbarText(String title);
}
