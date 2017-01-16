package athletictracking.com.trackhlete.gui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
 * Created by gary on 17/12/16.
 */

public class ProfileFragment extends Fragment {
    private ListView mListView;
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
