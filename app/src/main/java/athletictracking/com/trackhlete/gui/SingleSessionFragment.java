package athletictracking.com.trackhlete.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.base.MainActivity;
import athletictracking.com.trackhlete.infra.DatabaseHandler;
import athletictracking.com.trackhlete.infra.Linker;
import athletictracking.com.trackhlete.infra.NonScrollListView;
import athletictracking.com.trackhlete.infra.Session;

/**
 * Created by gary on 16/01/17.
 */

public class SingleSessionFragment extends Fragment {
    private static final String TAG = "debug_ss";

    private ArrayList<ReviewItem> mGeneralStatsItems;
    private ArrayList<SplitItem> mSplitsItems;

    private DatabaseHandler db;
    private Linker linker;
    private Session mSessionSelected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_single_session, container, false);

        db = new DatabaseHandler(getContext());
        linker = (Linker) getActivity();
        mGeneralStatsItems = new ArrayList<>();
        mSplitsItems = new ArrayList<>();

        initReviewItems();
        setupListViewsWithAdapters(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Menu item id is: " + item.getItemId());
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "PRESS");
                getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupListViewsWithAdapters(View view) {
        NonScrollListView mGeneralStatsList = (NonScrollListView) view.findViewById(R.id.fragment_ss_general_stats_list);
        NonScrollListView mSplitsList = (NonScrollListView) view.findViewById(R.id.fragment_ss_splits_stats_list);

        ReviewItemAdapter mGeneralStatsAdapter = new ReviewItemAdapter(getContext(), mGeneralStatsItems);
        mGeneralStatsList.setAdapter(mGeneralStatsAdapter);
        mGeneralStatsAdapter.notifyDataSetChanged();

        SplitItemAdapter mSplitsAdapter = new SplitItemAdapter(getContext(), mSplitsItems);
        mSplitsList.setAdapter(mSplitsAdapter);
        mSplitsAdapter.notifyDataSetChanged();
    }

    private void initReviewItems() {
        if (!getArguments().isEmpty()) {
            mSessionSelected = db.readSessionById(getArguments().getInt(SessionsFragment.ARGUMENT_SESSION_ID));
            if (mSessionSelected != null) {
                Log.d(TAG, "session pulled" + mSessionSelected.getDate());
                linker.setToolbarText(mSessionSelected.getDate());

                ReviewItem distance = new ReviewItem();
                distance.setSessionDataDescription("Distance");
                distance.setSessionData(String.valueOf(linker.round(Double.parseDouble(mSessionSelected.getOverallDistance()), 1)) + " " + linker.getDistanceUnit());
                mGeneralStatsItems.add(distance);

                ReviewItem duration = new ReviewItem();
                duration.setSessionDataDescription("Duration");
                duration.setSessionData(mSessionSelected.getOverallTime());
                mGeneralStatsItems.add(duration);

                ReviewItem avgPace = new ReviewItem();
                avgPace.setSessionDataDescription("Average Pace");
                avgPace.setSessionData(mSessionSelected.getAveragePace());
                mGeneralStatsItems.add(duration);

                ReviewItem avgSpeed = new ReviewItem();
                avgSpeed.setSessionDataDescription("Average Speed");
                avgSpeed.setSessionData(String.valueOf(linker.round(mSessionSelected.getAverageSpeed(), 1)) + " " + linker.getSpeedUnit());
                mGeneralStatsItems.add(avgSpeed);
                //Log.d(TAG, mSessionSelected.getSplitSpeeds().toString());
                Log.d(TAG, mSessionSelected.getPaces().toString());

                ReviewItem maxSpeed = new ReviewItem();
                maxSpeed.setSessionDataDescription("Max. Speed");
                maxSpeed.setSessionData(String.valueOf(linker.round(mSessionSelected.getMaxSpeed(), 1)) + " " + linker.getSpeedUnit());
                mGeneralStatsItems.add(maxSpeed);

                ReviewItem elevGain = new ReviewItem();
                elevGain.setSessionDataDescription("Elevation Gain");
                elevGain.setSessionData(String.valueOf(linker.round(mSessionSelected.getOverallElevChange(), 1)) + " m");
                mGeneralStatsItems.add(elevGain);

                for (int i = 0; i < mSessionSelected.getNumSplits(); i++) {
                    SplitItem split = new SplitItem();
                    split.setSplitNum(i + 1);
                    split.setPace(mSessionSelected.getPaces().get(i));
                    split.setAvgSpeed(mSessionSelected.getSplitSpeeds().get(i));
                    split.setElevation(mSessionSelected.getElevations().get(i));
                    mSplitsItems.add(split);
                }
            }
            else
                Log.d(TAG, "session pulled is null");
        }
    }

    private String getDistanceUnit() {
        if (linker.getUnitSetting() == MainActivity.METRIC)
            return " min/km";
        else
            return " min/m";
    }

    public class ReviewItemAdapter extends ArrayAdapter<ReviewItem> {
        private Context context;
        private ArrayList<ReviewItem> allReviewItems;

        private LayoutInflater mInflater;
        private boolean mNotifyOnChange = true;

        public ReviewItemAdapter(Context context, ArrayList<ReviewItem> mSettings) {
            super(context, R.layout.list_item_ss_general_stat);
            this.context = context;
            this.allReviewItems = mSettings;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return allReviewItems.size();
        }

        @Override
        public ReviewItem getItem(int position) {
            return allReviewItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public int getPosition(ReviewItem item) {
            return allReviewItems.indexOf(item);
        }

        @Override
        public int getViewTypeCount() {
            return 1; //Number of types + 1 !!!!!!!!
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case 1:
                        convertView = mInflater.inflate(R.layout.list_item_ss_general_stat,parent, false);
                        holder.sessionData = (TextView) convertView.findViewById(R.id.list_item_ss_data);
                        holder.sessionDataDescription = (TextView) convertView.findViewById(R.id.list_item_ss_desc);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.sessionData.setText(allReviewItems.get(position).getSessionData());
            holder.sessionDataDescription.setText(allReviewItems.get(position).getSessionDataDescription());
            holder.pos = position;
            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            mNotifyOnChange = true;
        }

        public void setNotifyOnChange(boolean notifyOnChange) {
            mNotifyOnChange = notifyOnChange;
        }


        //---------------static views for each row-----------//
        class ViewHolder {
            TextView sessionDataDescription;
            TextView sessionData;
            int pos; //to store the position of the item within the list
        }
    }

    class ReviewItem {
        private String sessionDataDescription;
        private String sessionData;

        public ReviewItem() {
        }

        public String getSessionData() {
            return sessionData;
        }

        public void setSessionData(String sessionData) {
            this.sessionData = sessionData;
        }

        public String getSessionDataDescription() {
            return sessionDataDescription;
        }

        public void setSessionDataDescription(String sessionDataDescription) {
            this.sessionDataDescription = sessionDataDescription;
        }

    }

    public class SplitItemAdapter extends ArrayAdapter<SplitItem> {
        private Context context;
        private ArrayList<SplitItem> allSplitItems;

        private LayoutInflater mInflater;
        private boolean mNotifyOnChange = true;

        public SplitItemAdapter(Context context, ArrayList<SplitItem> mSettings) {
            super(context, R.layout.list_item_ss_split_stat);
            this.context = context;
            this.allSplitItems = mSettings;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return allSplitItems.size();
        }

        @Override
        public SplitItem getItem(int position) {
            return allSplitItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public int getPosition(SplitItem item) {
            return allSplitItems.indexOf(item);
        }

        @Override
        public int getViewTypeCount() {
            return 1; //Number of types + 1 !!!!!!!!
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new ViewHolder();
                switch (type) {
                    case 1:
                        convertView = mInflater.inflate(R.layout.list_item_ss_split_stat,parent, false);
                        holder.splitNum = (TextView) convertView.findViewById(R.id.list_item_ss_split_num);
                        holder.pace = (TextView) convertView.findViewById(R.id.list_item_ss_pace);
                        holder.elevation = (TextView) convertView.findViewById(R.id.list_item_ss_elevation);
                        holder.avgSpeed = (TextView) convertView.findViewById(R.id.list_item_ss_avg_speed);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.splitNum.setText(allSplitItems.get(position).getSplitNum() + ".0");
            holder.pace.setText(allSplitItems.get(position).getPace());
            holder.elevation.setText(allSplitItems.get(position).getElevation() + " m");
            holder.avgSpeed.setText(allSplitItems.get(position).getAvgSpeed() + linker.getSpeedUnit());
            holder.pos = position;
            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            mNotifyOnChange = true;
        }

        public void setNotifyOnChange(boolean notifyOnChange) {
            mNotifyOnChange = notifyOnChange;
        }


        //---------------static views for each row-----------//
        class ViewHolder {
            TextView splitNum;
            TextView pace;
            TextView elevation;
            TextView avgSpeed;
            int pos; //to store the position of the item within the list
        }
    }

    class SplitItem {
        private int splitNum;
        private String pace;
        private double elevation;
        private double avgSpeed;

        public SplitItem() {

        }

        public int getSplitNum() {
            return splitNum;
        }

        public String getPace() {
            return pace;
        }

        public double getElevation() {
            return elevation;
        }

        public double getAvgSpeed() {
            return avgSpeed;
        }
        public void setSplitNum(int splitNum) {
            this.splitNum = splitNum;
        }

        public void setPace(String pace) {
            this.pace = pace;
        }

        public void setElevation(double elevation) {
            this.elevation = elevation;
        }

        public void setAvgSpeed(double avgSpeed) {
            this.avgSpeed = avgSpeed;
        }
    }
}
