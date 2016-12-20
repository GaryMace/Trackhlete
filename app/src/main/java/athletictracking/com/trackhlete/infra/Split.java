package athletictracking.com.trackhlete.infra;

/**
 * Created by gary on 17/12/16.
 */

public class Split {
    public static final int SPLIT_TYPE_MILE = 0;
    public static final int SPLIT_TYPE_KM = 1;
    public static final int SPLIT_TYPE_INTERVAL = 2;

    private int splitType;
    private int time;
    private double speed;
    private String pace;

    public Split () {

    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getSplitType() {
        return splitType;
    }

    public void setSplitType(int splitType) {
        this.splitType = splitType;
    }

    public void setTime(int time) {
        this.time = time;
        setPace();
    }

    public String getPace() {
        return pace;
    }

    private void setPace() {
        int mins, seconds;
        mins = time / 60;
        seconds = time - (mins * 60);

        pace =  mins + ":" + ((seconds < 10) ? ("0" + seconds) : seconds);
    }
}
