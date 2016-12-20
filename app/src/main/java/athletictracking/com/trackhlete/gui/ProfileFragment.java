package athletictracking.com.trackhlete.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import athletictracking.com.trackhlete.R;
import athletictracking.com.trackhlete.infr.Linker;

/**
 * Created by gary on 17/12/16.
 */

public class ProfileFragment extends Fragment {
    private Linker linker;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the view
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        linker = (Linker) getActivity();

        return view;
    }
}
