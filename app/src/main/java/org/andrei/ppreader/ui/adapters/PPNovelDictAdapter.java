package org.andrei.ppreader.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.helper.PPNovelReaderPageManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Consumer;

public class PPNovelDictAdapter extends BaseAdapter {

    public PPNovelDictAdapter(@NonNull final Fragment parent,@NonNull final PPNovel novel, final PPNovelReaderPageManager mgr){
        m_novel = novel;
        m_parent = parent;
        m_mgr = mgr;
    }

    public Observable<Integer> clicks(){
        return m_observable;
    }

    @Override
    public int getCount() {
        return m_novel.chapters.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = m_parent.getLayoutInflater().inflate(R.layout.view_ppnovel_reader_dict_item,null);
        }
        PPNovelChapter chapter = m_novel.chapters.get(position);
        TextView tv = convertView.findViewById(R.id.novel_dict_item);
        tv.setText(chapter.name);
        convertView.setTag(R.id.tag_chapter,chapter.url);
        final View v = convertView;
        RxView.clicks(convertView).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object obj) throws Exception {
                String chapter = (String) v.getTag(R.id.tag_chapter);
                int index = m_mgr.getFirstChapterItemPosition(chapter);
                if(index != -1 && m_observer != null){
                    m_observer.onNext(index);
                }
            }
        });
        return convertView;
    }

    private final PPNovelReaderPageManager m_mgr;
    private final PPNovel m_novel;
    private final Fragment m_parent;
    private Observer<? super Integer> m_observer = null;
    private Observable<Integer> m_observable = new Observable<Integer>(){
        @Override
        protected void subscribeActual(Observer<? super Integer> observer) {
            m_observer = observer;
        }
    };
}
