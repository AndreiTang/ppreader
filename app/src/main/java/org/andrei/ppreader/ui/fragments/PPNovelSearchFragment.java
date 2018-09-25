package org.andrei.ppreader.ui.fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.ui.adapters.PPNovelSearchAdapter;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

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

        final ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        lv.setFooterDividersEnabled(false);
        lv.setSelected(true);
        lv.setVerticalScrollBarEnabled(false);

        m_footView = getLayoutInflater().inflate(R.layout.view_ppnovel_search_foot,null);

        PPNovelSearchAdapter adapter = new PPNovelSearchAdapter(this);
        lv.setAdapter(adapter);

        SearchView sv = (SearchView) getView().findViewById(R.id.novel_search);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        lv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                insertFootView();
            }
        });
    }

        private void insertFootView(){
        ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        if(lv.getChildCount() == 0 || !m_isLoading ){
            return;
        }
        int pos = lv.getLastVisiblePosition();
        View v = lv.getChildAt(pos);
        if( v != null && v.getBottom() >= lv.getBottom() && lv.getFooterViewsCount() == 0){
            lv.addFooterView(m_footView);
        }
    }

    private void reset(){
        ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        if(lv.getFooterViewsCount() == 1){
            lv.removeFooterView(m_footView);
        }
        //m_searches.clear();
        getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.GONE);
        PPNovelSearchAdapter adapter = (PPNovelSearchAdapter) lv.getAdapter();
        if(adapter != null){
            adapter.reset();
        }
        //notifyDataSetChanged();
    }

    private void search(String name) {
        reset();
        CrawlNovel crawlNovel = CrawlNovelService.instance().builder();
        m_isLoading = true;
        showLoadingMask(true);
        ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        final PPNovelSearchAdapter adapter = (PPNovelSearchAdapter)lv.getAdapter();
        crawlNovel.search(name).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<PPNovel>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(PPNovel ppNovel) {
                //m_searches.add(ppNovel);
                //notifyDataSetChanged();
                if(adapter != null){
                    adapter.addSearch(ppNovel);
                }
                showLoadingMask(false);
            }

            @Override
            public void onError(Throwable e) {
                m_isLoading = false;
                int err = Integer.valueOf(e.getMessage());
                showErrorMask(err);
            }

            @Override
            public void onComplete() {
                m_isLoading = false;
                ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
                if(lv.getFooterViewsCount() == 1){
                    lv.removeFooterView(m_footView);
                }
            }
        });
    }

    private void showLoadingMask(boolean isShow){
        if(isShow == true){
            getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.VISIBLE);
        }
        else{
            getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        }
        getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.GONE);
    }

    private void showErrorMask(int err){
        getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.VISIBLE);
        TextView tx = (TextView) getView().findViewById(R.id.novel_search_err_msg);
        tx.setText(err);
    }

    private View m_footView = null;
    private boolean  m_isLoading = false;

}
