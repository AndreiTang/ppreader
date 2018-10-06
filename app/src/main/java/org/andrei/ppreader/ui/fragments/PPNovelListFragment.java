package org.andrei.ppreader.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.ui.adapters.PPNovelListAdapter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * A simple {@link Fragment} subclass.
 */
public class PPNovelListFragment extends Fragment {


    public PPNovelListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ppnovel_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        PPNovelListAdapter adapter = new PPNovelListAdapter(this);
        GridView vp = (GridView) getView().findViewById(R.id.novel_list);
        vp.setAdapter(adapter);

        View v = getView().findViewById(R.id.novel_list_edit_btn);
        final PPNovelListAdapter that = adapter;

        RxView.clicks(v).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                getView().findViewById(R.id.novel_list_edit_btn).setVisibility(View.GONE);
                getView().findViewById(R.id.novel_list_remove_btn).setVisibility(View.VISIBLE);
                ArrayList<PPNovel> novels = CrawlNovelService.instance().getPPNovels();
                for(PPNovel novel: novels){
                    novel.needRemove = true;
                }
                that.notifyDataSetChanged();
            }
        });
        v = getView().findViewById(R.id.novel_list_remove_btn);
        RxView.clicks(v).throttleFirst(1,TimeUnit.SECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                getView().findViewById(R.id.novel_list_edit_btn).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.novel_list_remove_btn).setVisibility(View.GONE);
                ArrayList<PPNovel> novels = CrawlNovelService.instance().getPPNovels();
                for(PPNovel novel: novels){
                    novel.needRemove = false;
                }
                that.notifyDataSetChanged();
            }
        });

        v = getView().findViewById(R.id.novel_list_search_btn);
        RxView.clicks(v).throttleFirst(1,TimeUnit.SECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
               ViewPager vp = getActivity().findViewById(R.id.main_viewpager);
               vp.setCurrentItem(1);
            }
        });

    }


}
