package athletictracking.com.trackhlete.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.base.SettingsActivity;
import athletictracking.com.trackhlete.infra.Linker;

/**
 * Created by gary on 28/12/16.
 */

public class SettingsFragment extends Fragment{
    private static final int LAUNCH_SETTINGS = 0;
    private Linker linker;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the view
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        linker = (Linker) getActivity();

        Intent getLoginIntent = new Intent(getActivity(), SettingsActivity.class);   //User wants to register
        startActivityForResult(getLoginIntent, LAUNCH_SETTINGS);

        return view;
    }
}
