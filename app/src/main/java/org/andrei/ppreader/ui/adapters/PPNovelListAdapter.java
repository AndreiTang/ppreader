package org.andrei.ppreader.ui.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlNovelError;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.ICrawlNovel;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.ui.fragments.PPNovelReaderFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PPNovelListAdapter extends BaseAdapter{

    public PPNovelListAdapter(@NonNull Fragment parent){
        m_parent = parent;
        Collections.sort(CrawlNovelService.instance().getPPNovels(), new Comparator<PPNovel>() {
            @Override
            public int compare(PPNovel o1, PPNovel o2) {
                if(o1.lastReadTime >= o2.lastReadTime){
                    return -1;
                }
                else{
                    return 1;
                }
            }
        });
        initialize();
    }
    @Override
    public int getCount() {
        return CrawlNovelService.instance().getPPNovels().size();
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
        if(m_parent == null){
            return null;
        }
        if(view == null){
            view = createView(i);
        }
        updateView(i, view);
        return view;
    }

    public void disposable(){
        if(m_disposable != null && !m_disposable.isDisposed()){
            m_disposable.dispose();
        }
        m_disposable = null;
    }

    private View createView(int i){
        View view = m_parent.getLayoutInflater().inflate(R.layout.view_ppnovel_list,null);
        final View v = view;
        final PPNovelListAdapter that = this;
        RxView.clicks(view).throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                int pos = (Integer) v.getTag(R.id.tag_pos);
                PPNovel item = CrawlNovelService.instance().getPPNovels().get(pos);
                if(item.needRemove){
                    removeItem(pos,item.name);
                }
                else{
                    readItem(item);
                }
            }
        });

        return view;
    }

    private void updateView(int i,View view){
        PPNovel novel = CrawlNovelService.instance().getPPNovels().get(i);
        ImageView img = (ImageView) view.findViewById(R.id.novel_list_cover);
        Glide.with(view).clear(img);
        Glide.with(view).load(novel.imgUrl).apply(RequestOptions.fitCenterTransform()).into(img);
        TextView tv = (TextView)view.findViewById(R.id.novel_list_title);
        tv.setText(novel.name);
        view.setTag(R.id.tag_pos,i);
        if(novel.needRemove){
            view.findViewById(R.id.novel_list_remove).setVisibility(View.VISIBLE);
        }
        else{
            view.findViewById(R.id.novel_list_remove).setVisibility(View.GONE);
        }

        if(novel.status == PPNovel.STATUS_CHECKED){
            view.findViewById(R.id.novel_list_update).setVisibility(View.VISIBLE);
        }
        else{
            view.findViewById(R.id.novel_list_update).setVisibility(View.GONE);
        }
    }

    private void initialize(){
        ArrayList<PPNovel> checkList = new ArrayList<PPNovel>();
        for(PPNovel novel: CrawlNovelService.instance().getPPNovels()){
            if(novel.status == PPNovel.STATUS_UNCHECKED){
                checkList.add(novel);
            }
        }
        scanNovels(checkList);
    }

    private void removeItem(final int pos,String name){
        final PPNovelListAdapter that = this;
        AlertDialog.Builder dlg = new AlertDialog.Builder(m_parent.getActivity());
        String msg = m_parent.getString(R.string.novel_list_remove_msg);
        msg = String.format(msg,name);

        dlg.setMessage(msg);
        dlg.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PPNovel novel = CrawlNovelService.instance().getPPNovels().get(pos);
                CrawlNovelService.instance().removeNovel(m_parent.getActivity().getApplicationContext().getFilesDir().getAbsolutePath(),novel);
                that.notifyDataSetChanged();
            }
        });
        dlg.setNegativeButton(R.string.btn_cancel,null);
        dlg.show();
    }

    private void readItem(PPNovel novel){
        if(novel.status == PPNovel.STATUS_CHECKED) {
            novel.status = PPNovel.STATUS_CONFIRMED;
        }
        PPNovelReaderFragment fragment = new PPNovelReaderFragment();
        Bundle arg = new Bundle();
        arg.putSerializable(PPNovelReaderFragment.NOVEL,novel);
        fragment.setArguments(arg);
        m_parent.getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment,PPNovelReaderFragment.TAG).commit();
    }



    private void scanNovels(final ArrayList<PPNovel> novels){
        if(novels.size() == 0){
            return;
        }

        final PPNovelListAdapter that = this;
        m_disposable = Observable.create(new ObservableOnSubscribe<PPNovel>() {
            @Override
            public void subscribe(ObservableEmitter<PPNovel> e) throws Exception{
                try{
                    for(PPNovel novel: novels){
                        ICrawlNovel crawlNovel = CrawlNovelService.instance().builder(novel.engineName);
                        if(crawlNovel == null){
                            continue;
                        }

                        CrawlChapterResult crawlChapterResult = new CrawlChapterResult();
                        int ret = crawlNovel.fetchChapters(novel,crawlChapterResult);
                        if(ret == CrawlNovelError.ERR_NONE){
                            if(novel.chapters.size() < crawlChapterResult.chapters.size()){
                                //there maybe some problems in the special case for tianyi engine
                                for(int i = novel.chapters.size() ; i < crawlChapterResult.chapters.size() ; i++){
                                    novel.chapters.add(crawlChapterResult.chapters.get(i));
                                }
                                novel.status = PPNovel.STATUS_CHECKED;
                                CrawlNovelService.instance().saveNovel(m_parent.getActivity().getApplicationContext().getFilesDir().getAbsolutePath(),novel);
                            }
                            else{
                                //it mean the red spot will not show , due to none of new chapters
                                novel.status = PPNovel.STATUS_CONFIRMED;
                            }
                            that.notifyDataSetChanged();
                        }

                    }
                    e.onComplete();
                    m_disposable = null;
                }
                catch(Exception ex){
                    if(e.isDisposed()){
                        e.onComplete();
                        m_disposable = null;
                    }
                }

            }
        }).observeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<PPNovel>() {
            @Override
            public void accept(PPNovel novel) throws Exception {

            }
        });
    }

    private Fragment m_parent;
    private Disposable m_disposable = null;
}
