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
        //initialize();
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

    public void reset(){
        m_searches.clear();
        notifyDataSetChanged();
    }

    public void addSearch(PPNovel novel){
        m_searches.add(novel);
        notifyDataSetChanged();
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

    private ArrayList<PPNovel> m_searches = new ArrayList<PPNovel>();
    private Fragment m_parent = null;
}
