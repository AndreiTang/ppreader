package org.andrei.ppreader.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.andrei.ppreader.R;

/**

 */
public class PPNovelCoverFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match


    public PPNovelCoverFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ppnovel_cover, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/wawa.ttf");
        TextView tv = (TextView) getActivity().findViewById(R.id.cover_title);
        tv.setTypeface(typeface);

        //fix the bug that Glide will on the top of the page. It cause the fragment can't exit.
        Glide.with(this).load(R.drawable.rm);
    }

}
