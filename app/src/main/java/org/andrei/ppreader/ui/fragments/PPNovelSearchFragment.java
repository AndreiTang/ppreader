package org.andrei.ppreader.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelError;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.ui.adapters.PPNovelSearchAdapter;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class PPNovelSearchFragment extends Fragment {


    public PPNovelSearchFragment() {
        // Required empty public constructor
        m_crawlNovel = CrawlNovelService.instance().builder();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ppnovel_search, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        final ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        lv.setFooterDividersEnabled(false);
        lv.setSelected(true);
        lv.setVerticalScrollBarEnabled(false);

        m_footView = getLayoutInflater().inflate(R.layout.view_ppnovel_search_foot, null);

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

        View v = getView().findViewById(R.id.novel_list_btn);
        RxView.clicks(v).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                ViewPager vp = getActivity().findViewById(R.id.main_viewpager);
                vp.setCurrentItem(0);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (m_disposable != null) {
            if (!m_disposable.isDisposed()) {
                m_disposable.dispose();
            }
            m_disposable = null;
        }
    }

    private void insertFootView() {
        ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        if (lv == null || lv.getChildCount() == 0 || !m_isLoading) {
            return;
        }
        int pos = lv.getLastVisiblePosition();
        View v = lv.getChildAt(pos);
        if (v != null && v.getBottom() >= lv.getBottom() && lv.getFooterViewsCount() == 0) {
            lv.addFooterView(m_footView);
        }
    }

    private PPNovelSearchAdapter getAdapter() {
        ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        ListAdapter adapter = lv.getAdapter();
        if (adapter == null) {
            return null;
        }
        if (adapter instanceof PPNovelSearchAdapter) {
            return (PPNovelSearchAdapter) adapter;
        } else {
            HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) adapter;
            return (PPNovelSearchAdapter) headerViewListAdapter.getWrappedAdapter();
        }
    }

    private void reset() {
        ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        if (lv.getFooterViewsCount() == 1) {
            lv.removeFooterView(m_footView);
        }
        //m_searches.clear();
        getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.GONE);
        PPNovelSearchAdapter adapter = (PPNovelSearchAdapter) getAdapter();
        if (adapter != null) {
            adapter.reset();
        }
        //notifyDataSetChanged();
    }

    private void search(final String name) {
        reset();

        m_isLoading = true;

        if (m_disposable != null) {
            if (!m_disposable.isDisposed()) {
                m_disposable.dispose();
            }
            m_disposable = null;
        }

        showLoadingMask(true);
        final ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
        final PPNovelSearchAdapter adapter = (PPNovelSearchAdapter) getAdapter();

        Observable.create(new ObservableOnSubscribe<PPNovel>() {
            @Override
            public void subscribe(ObservableEmitter<PPNovel> e) throws Exception {
                try{
                    int ret = m_crawlNovel.search(name,e);
                    if(ret == CrawlNovelError.ERR_NONE_FETCHED){
                        Integer i = R.string.err_not_found;
                        Throwable err = new Throwable(i.toString());
                        e.onError(err);
                    }
                    else if(ret == CrawlNovelError.ERR_NETWORK){
                        Integer i = R.string.err_network;
                        Throwable err = new Throwable(i.toString());
                        e.onError(err);
                    }
                    else{
                        e.onComplete();
                    }
                    m_disposable = null;

                }catch(Exception ex){
                    if(!m_disposable.isDisposed()){
                        Integer i = R.string.err_network;
                        Throwable err = new Throwable(i.toString());
                        e.onError(err);
                        m_disposable = null;
                    }
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<PPNovel>() {
            @Override
            public void onError(Throwable e) {
                m_isLoading = false;
                int err = Integer.valueOf(e.getMessage());
                showErrorMask(err);
            }

            @Override
            public void onSubscribe(Disposable d) {
                m_disposable = d;
            }

            @Override
            public void onNext(PPNovel ppNovel) {
                if (adapter != null) {
                    adapter.addSearch(ppNovel);
                }
                showLoadingMask(false);
                lv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        lv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        insertFootView();
                    }
                });
            }

            @Override
            public void onComplete() {
                m_isLoading = false;
                ListView lv = (ListView) getView().findViewById(R.id.novel_search_ret_list);
                if (lv.getFooterViewsCount() == 1) {
                    lv.removeFooterView(m_footView);
                }
            }
        });
    }

    private void showLoadingMask(boolean isShow) {
        if (isShow == true) {
            getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        }
        getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.GONE);
    }

    private void showErrorMask(int err) {
        getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.VISIBLE);
        TextView tx = (TextView) getView().findViewById(R.id.novel_search_err_msg);
        tx.setText(err);
    }

    private View m_footView = null;
    private boolean m_isLoading = false;
    private CrawlNovel m_crawlNovel = null;
    private Disposable m_disposable;

}
