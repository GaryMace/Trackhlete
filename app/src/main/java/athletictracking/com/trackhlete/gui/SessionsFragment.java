package athletictracking.com.trackhlete.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.infra.DatabaseHandler;
import athletictracking.com.trackhlete.infra.Linker;
import athletictracking.com.trackhlete.infra.Session;

/**
 * Created by gary on 16/01/17.
 */

public class SessionsFragment extends Fragment {
    public static final String ARGUMENT_SESSION_ID = "session_id";
    private static final String TAG = "debug_sessions";
    private static final String REVERT_TO_TAG = "sessions_fragment";

    private ArrayList<SessionItem> mSessions;
    private FragmentManager fragmentManager;
    private List<Session> mPastSessions;
    private DatabaseHandler db;
    private SessionAdapter mAdapter;
    private ListView mListView;
    private Linker linker;

    //TODO: figure out why it's not showing the list
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the view
        View view = inflater.inflate(R.layout.fragment_all_sessions, container, false);
        fragmentManager = getActivity().getSupportFragmentManager();

        mListView = (ListView) view.findViewById(R.id.fragment_session_list);
        linker = (Linker) getActivity();
        db = new DatabaseHandler(getContext());

        initItemSelectedListener();
        initSettingsItems();

        this.mAdapter = new SessionAdapter(getContext(), mSessions);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        return view;
    }

    private void initItemSelectedListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
                FragmentTransaction ft = fragmentManager.beginTransaction();

                SessionItem sessionSelected = (SessionItem) mListView.getItemAtPosition(position);
                SingleSessionFragment singleSessionReviewFrag = new SingleSessionFragment();
                Bundle argData = new Bundle();

                argData.putInt(ARGUMENT_SESSION_ID, sessionSelected.getId());   //TODO: refactor later to pass entire object contents not just the id
                singleSessionReviewFrag.setArguments(argData);

                ft.addToBackStack(SessionsFragment.REVERT_TO_TAG); //Allow the backbutton to revert back to this fragment
                ft.replace(R.id.activity_main_fragment_container, singleSessionReviewFrag, getString(R.string.fragment_ss_id)).commit();
            }
        });
    }

    private void initSettingsItems() {
        mPastSessions = new ArrayList<>();
        mSessions = new ArrayList<>();

        mPastSessions = db.readAllSessions();
        if (!mPastSessions.isEmpty()) {
            for (Session session : mPastSessions) {
                SessionItem item = new SessionItem();

                item.setDuration(session.getOverallTime());
                item.setDistance(String.valueOf(linker.round(Double.parseDouble(session.getOverallDistance()), 1)));
                item.setDate(session.getDate());
                item.setId(session.getId());
                mSessions.add(item);
            }
        } else {
            SessionItem item1 = new SessionItem();
            item1.setDistance("10.2 Km");
            item1.setDuration("00:41:23");
            item1.setDate("23 Dec 2016");

            SessionItem item2 = new SessionItem();
            item2.setDistance("16.3 Km");
            item2.setDuration("01:12:23");
            item2.setDate("01 Jan 2017");

            SessionItem item3 = new SessionItem();
            item3.setDistance("5.0 Km");
            item3.setDuration("00:18:37");
            item3.setDate("5 Jan 2017");
            mSessions.add(item1);
            mSessions.add(item2);
            mSessions.add(item3);
        }
    }

    public class SessionAdapter extends ArrayAdapter<SessionItem> {

        private Context context;
        private ArrayList<SessionItem> allSessions;

        private LayoutInflater mInflater;
        private boolean mNotifyOnChange = true;

        public SessionAdapter(Context context, ArrayList<SessionItem> mSettings) {
            super(context, R.layout.list_item_settings);
            this.context = context;
            this.allSessions = mSettings;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return allSessions.size();
        }

        @Override
        public SessionItem getItem(int position) {
            return allSessions.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public int getPosition(SessionItem item) {
            return allSessions.indexOf(item);
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
            final SessionAdapter.ViewHolder holder;
            int type = getItemViewType(position);
            if (convertView == null) {
                holder = new SessionAdapter.ViewHolder();
                switch (type) {
                    case 1:
                        convertView = mInflater.inflate(R.layout.list_item_session,parent, false);
                        holder.distance = (TextView) convertView.findViewById(R.id.list_session_distance);
                        holder.duration = (TextView) convertView.findViewById(R.id.list_session_duration);
                        holder.date = (TextView) convertView.findViewById(R.id.list_settings_date);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (SessionAdapter.ViewHolder) convertView.getTag();
            }
            holder.distance.setText(allSessions.get(position).getDistance() + " " + linker.getDistanceUnit());
            holder.duration.setText(allSessions.get(position).getDuration());
            holder.date.setText(allSessions.get(position).getDate());
            holder.id = allSessions.get(position).getId();
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
            TextView distance;
            TextView duration;
            TextView date;
            int id; //Used to get from DB later, instead of passing all individualparams
            int pos; //to store the position of the item within the list
        }
    }

    class SessionItem {
        private int id;
        private String distance;
        private String duration;
        private String date;

        public SessionItem() {
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String description) {
            this.distance = description;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    //When back pressed go to home
    @Override
    public void onResume() {    //TODO: this is more of a hack than a fix.. need to handle the back press properly later
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //On back button pressed
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);  //Just go back to home screen, initially it was logging user out.
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                    return true;
                }
                return false;
            }
        });
    }
}
