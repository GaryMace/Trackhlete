package athletictracking.com.trackhlete.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.infra.Linker;

/**
 * Created by gary on 28/12/16.
 */

public class SettingsFragment extends Fragment {
    private static final String TAG = "debug_settings";
    private ArrayList<SettingsItem> settings;
    private SettingsAdapter mAdapter;
    private ListView mListView;
    private Linker linker;

    //TODO: figure out why it's not showing the list
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the view
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mListView = (ListView) view.findViewById(R.id.fragment_profile_list);
        linker = (Linker) getActivity();
        settings = new ArrayList<>();

        initSettingsItems();

        this.mAdapter = new SettingsAdapter(getContext(), settings);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        return view;
    }

    private void initSettingsItems() {
        SettingsItem profileSettings = new SettingsItem();
        profileSettings.setDescription("Profile");
        //set image
        SettingsItem metricSettings = new SettingsItem();
        metricSettings.setDescription("Metrics");

        settings.add(profileSettings);
        settings.add(metricSettings);
    }

    public class SettingsAdapter extends ArrayAdapter<SettingsItem> {

        private Context context;
        private ArrayList<SettingsItem> allSettings;

        private LayoutInflater mInflater;
        private boolean mNotifyOnChange = true;

        public SettingsAdapter(Context context, ArrayList<SettingsItem> mSettings) {
            super(context, R.layout.list_item_settings);
            this.context = context;
            this.allSettings = mSettings;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return allSettings.size();
        }

        @Override
        public SettingsItem getItem(int position) {
            return allSettings.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public int getPosition(SettingsItem item) {
            return allSettings.indexOf(item);
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
                        convertView = mInflater.inflate(R.layout.list_item_settings,parent, false);
                        holder.img = (ImageView) convertView.findViewById(R.id.list_settings_picture);
                        holder.description = (TextView) convertView.findViewById(R.id.list_settings_description);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.img  = allSettings.get(position).getImg();
            holder.description.setText(allSettings.get(position).getDescription());
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
            ImageView img;
            TextView description;
            int pos; //to store the position of the item within the list
        }
    }

    class SettingsItem {
        private ImageView img;
        private String description;

        public SettingsItem() {
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ImageView getImg() {
            return img;
        }

        public void setImg(ImageView img) {
            this.img = img;
        }

    }
}
