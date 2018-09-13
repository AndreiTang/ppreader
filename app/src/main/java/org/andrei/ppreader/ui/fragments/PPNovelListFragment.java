package org.andrei.ppreader.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.PPNovel;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

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

        CrawlNovel crawlNovel = CrawlNovelService .instance().builder();
        ArrayList<PPNovel> checkList = new ArrayList<PPNovel>();
        checkNovels(checkList, crawlNovel);
    }


    private void checkNovels(final ArrayList<PPNovel> checkList, final CrawlNovel crawlNovel){
        if(checkList.size() == 0){
            return;
        }

        PPNovel novel =   checkList.remove(0);
        crawlNovel.fetchChapters(novel).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<CrawlChapterResult>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(CrawlChapterResult value) {

            }

            @Override
            public void onError(Throwable e) {
                checkNovels(checkList,crawlNovel);
            }

            @Override
            public void onComplete() {
                checkNovels(checkList,crawlNovel);
            }
        });

    }


    private ArrayList<PPNovel> m_checkList = new ArrayList<PPNovel>();


}
