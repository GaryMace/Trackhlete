package athletictracking.com.trackhlete.infra;

import java.util.ArrayList;

/**
 * Created by gary on 16/01/17.
 */

public class Session {
    private int id; //For deleting later
    private ArrayList<ArrayList<Double>> sessionGPS;
    private ArrayList<Double> splitSpeeds;
    private ArrayList<Double> elevations;
    private ArrayList<String> paces;
    private double overallElevChange;
    private String overallDistance;
    private String overallTime;
    private double averageSpeed;
    private double maxSpeed;
    private int numSplits;
    private String date;

    //For loading from DB
    public Session(int id,
                   String date,
                   String overallDistance,
                   String overallTime,
                   int numSplits,
                   double averageSpeed,
                   double maxSpeed,
                   double overallElevChange,
                   ArrayList<ArrayList<Double>> sessionGPS,
                   ArrayList<String> paces,
                   ArrayList<Double> elevations,
                   ArrayList<Double> splitSpeeds) {
        this.id = id;
        this.date = date;
        this.overallDistance = overallDistance;
        this.overallTime = overallTime;
        this.numSplits = numSplits;
        this.averageSpeed = averageSpeed;
        this.maxSpeed = maxSpeed;
        this.overallElevChange = overallElevChange;
        this.sessionGPS = sessionGPS;
        this.paces = paces;
        this.elevations = elevations;
        this.splitSpeeds = splitSpeeds;
    }

    //for storing in DB
    public Session(String date,
                   String overallDistance,
                   String overallTime,
                   int numSplits,
                   double averageSpeed,
                   double maxSpeed,
                   double overallElevChange,
                   ArrayList<ArrayList<Double>> sessionGPS,
                   ArrayList<String> paces,
                   ArrayList<Double> elevations,
                   ArrayList<Double> splitSpeeds) {
        this.date = date;
        this.overallDistance = overallDistance;
        this.overallTime = overallTime;
        this.numSplits = numSplits;
        this.averageSpeed = averageSpeed;
        this.maxSpeed = maxSpeed;
        this.overallElevChange = overallElevChange;
        this.sessionGPS = sessionGPS;
        this.paces = paces;
        this.elevations = elevations;
        this.splitSpeeds = splitSpeeds;
    }

    public int getId() {
        return id;
    }

    public ArrayList<ArrayList<Double>> getSessionGPS() {
        return sessionGPS;
    }

    public ArrayList<Double> getSplitSpeeds() {
        return splitSpeeds;
    }

    public ArrayList<Double> getElevations() {
        return elevations;
    }

    public ArrayList<String> getPaces() {
        return paces;
    }

    public double getOverallElevChange() {
        return overallElevChange;
    }

    public String getOverallDistance() {
        return overallDistance;
    }

    public String getOverallTime() {
        return overallTime;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public int getNumSplits() {
        return numSplits;
    }

    public String getDate() {
        return date;
    }
}
