package org.andrei.ppreader.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.ui.helper.PPNovelReaderPageManager;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Consumer;

public class PPNovelDictGroupAdapter extends BaseAdapter {

    public PPNovelDictGroupAdapter(@NonNull final Fragment parent, final PPNovel novel) {
        m_parent = parent;
        m_novel = novel;

        int groups = m_novel.chapters.size() / 100;
        if (m_novel.chapters.size() % 100 > 0) {
            groups++;
        }

        for (int i = 0; i < groups; i++) {
            String name = m_parent.getString(R.string.novel_dict_chapter);
            int end = (i+1)*100;
            if(i == groups -1 && end < m_novel.chapters.size()){
                end =  m_novel.chapters.size();
            }
            name = String.format(name,i*100+1,end);
            m_groups.add(name);
        }
    }

    public Observable<Integer> clicks(){
        return m_observable;
    }

    @Override
    public int getCount() {
        return m_groups.size();
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
        String name = m_groups.get(position);
        TextView tv = convertView.findViewById(R.id.novel_dict_item);
        tv.setText(name);

        final int pos = position;
        RxView.clicks(convertView).throttleFirst(200, TimeUnit.MILLISECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object obj) throws Exception {
                if(m_observer != null){
                    m_observer.onNext(pos*100);
                }
            }
        });

        return convertView;
    }

    private final Fragment m_parent;
    private ArrayList<String> m_groups = new ArrayList<String>();
    private final PPNovel m_novel;
    private Observer<? super Integer> m_observer = null;
    private Observable<Integer> m_observable = new Observable<Integer>(){
        @Override
        protected void subscribeActual(Observer<? super Integer> observer) {
            m_observer = observer;
        }
    };
}
