package org.andrei.ppreader.ui.fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.ui.adapters.PPNovelSearchAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class PPNovelSearchFragment extends Fragment {


    public PPNovelSearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ppnovel_search, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        lv.setFooterDividersEnabled(false);
        lv.setSelected(true);
        lv.setVerticalScrollBarEnabled(false);

        PPNovelSearchAdapter adapter = new PPNovelSearchAdapter(this);
        lv.setAdapter(adapter);
    }

}
