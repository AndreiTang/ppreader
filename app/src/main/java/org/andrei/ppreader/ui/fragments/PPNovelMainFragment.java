package org.andrei.ppreader.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.andrei.ppreader.R;
import org.andrei.ppreader.ui.adapters.PPNovelMainAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class PPNovelMainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    public final static String TAG = "PPNovelMainFragment";
    public final static String POS = "pos";

    public PPNovelMainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ppnovel_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getActivity().findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        int pos = 0;
        Bundle arg = getArguments();
        if(arg != null){
            pos = arg.getInt(POS);
        }

        PPNovelMainAdapter adapter = new PPNovelMainAdapter(this.getChildFragmentManager());
        ViewPager vp = (ViewPager)getView().findViewById(R.id.main_viewpager);
        vp.setAdapter(adapter);
        vp.setCurrentItem(pos);
    }




}
