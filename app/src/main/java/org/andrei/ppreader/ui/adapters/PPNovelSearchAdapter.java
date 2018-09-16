package org.andrei.ppreader.ui.adapters;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;


import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlNovelThrowable;
import org.andrei.ppreader.service.PPNovel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PPNovelSearchAdapter extends BaseAdapter {

    public PPNovelSearchAdapter(Fragment parent) {
        m_parent = parent;
        initialize();
    }

    @Override
    public int getCount() {
        return m_searches.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = createView(i, viewGroup);
        }
        updateView(i, view);

        return view;
    }

    private View createView(int i, ViewGroup vp) {
        View view = m_parent.getLayoutInflater().inflate(R.layout.view_ppnovel_search, null);
        RxView.clicks(view).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {

            }
        });
        return view;
    }

    private void updateView(int i, View view) {
        if (i >= m_searches.size()) {
            return;
        }
        PPNovel novel = m_searches.get(i);
        TextView tx = (TextView) view.findViewById(R.id.novel_search_author);
        tx.setText(novel.author);
        tx = (TextView) view.findViewById(R.id.novel_search_decs);
        tx.setText(novel.desc);
        tx = (TextView) view.findViewById(R.id.novel_search_name);
        tx.setText(novel.name);
        tx = (TextView) view.findViewById(R.id.novel_search_type);
        if (novel.type == PPNovel.TYPE_ING) {
            tx.setText(R.string.novel_type_ing);
        } else {
            tx.setText(R.string.novel_type_over);
        }

        ImageView img = (ImageView) view.findViewById(R.id.novel_search_cover);
        Glide.with(view).clear(img);
        Glide.with(view).load(novel.imgUrl).apply(RequestOptions.fitCenterTransform()).into(img);

    }

    private void search(String name) {
        reset();
        CrawlNovel crawlNovel = CrawlNovelService.instance().builder();
        m_isLoading = true;
        showLoadingMask(true);
        crawlNovel.search(name).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<PPNovel>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(PPNovel ppNovel) {
                m_searches.add(ppNovel);
                notifyDataSetChanged();
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
                ListView lv = (ListView) m_parent.getView().findViewById(R.id.novel_search_ret_list);
                if(lv.getFooterViewsCount() == 1){
                   lv.removeFooterView(m_footView);
                }
            }
        });
    }

    private void initialize(){
        if(m_parent == null){
            return;
        }

        m_footView = m_parent.getLayoutInflater().inflate(R.layout.view_ppnovel_search_foot,null);
//        SimpleDraweeView pv = (SimpleDraweeView) m_footView.findViewById(R.id.search_progress);
//        Uri uri = Uri.parse("res://" + m_parent.getContext().getPackageName() + "/" + R.drawable.progress_small);
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setUri(uri)
//                .setAutoPlayAnimations(true)
//                .build();
//        pv.setController(controller);


        final PPNovelSearchAdapter that = this;
        SearchView sv = (SearchView) m_parent.getActivity().findViewById(R.id.novel_search);
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

        ListView lv = (ListView) m_parent.getView().findViewById(R.id.novel_search_ret_list);
        lv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                insertFootView();
            }
        });

    }

    private void insertFootView(){
        ListView lv = (ListView) m_parent.getView().findViewById(R.id.novel_search_ret_list);
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
        ListView lv = (ListView) m_parent.getView().findViewById(R.id.novel_search_ret_list);
        if(lv.getFooterViewsCount() == 1){
            lv.removeFooterView(m_footView);
        }
        m_searches.clear();
        m_parent.getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        m_parent.getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.GONE);
        notifyDataSetChanged();
    }

    private void showLoadingMask(boolean isShow){
        if(m_parent == null){
            return;
        }
        if(isShow == true){
            m_parent.getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.VISIBLE);
        }
        else{
            m_parent.getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        }

        m_parent.getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.GONE);
    }

    private void showErrorMask(int err){
        if(m_parent == null){
            return;
        }
        m_parent.getView().findViewById(R.id.novel_search_loading_mask).setVisibility(View.GONE);
        m_parent.getView().findViewById(R.id.novel_search_error_mask).setVisibility(View.VISIBLE);
        TextView tx = (TextView) m_parent.getView().findViewById(R.id.novel_search_err_msg);
        tx.setText(err);

    }

    private ArrayList<PPNovel> m_searches = new ArrayList<PPNovel>();
    private Fragment m_parent = null;
    private View m_footView = null;
    private boolean  m_isLoading = false;
}
