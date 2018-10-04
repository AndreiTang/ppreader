package org.andrei.ppreader.ui.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.PPNovel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PPNovelListAdapter extends BaseAdapter{

    public PPNovelListAdapter(Fragment parent){
        m_parent = parent;
        if(m_parent != null){
            initialize();
        }
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
        CrawlNovel crawlNovel = CrawlNovelService .instance().builder();
        ArrayList<PPNovel> checkList = new ArrayList<PPNovel>();
        for(PPNovel novel: CrawlNovelService.instance().getPPNovels()){
            if(novel.status == PPNovel.STATUS_UNCHECKED){
                checkList.add(novel);
            }
        }
        //checkNovels(checkList, crawlNovel);
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
                CrawlNovelService.instance().getPPNovels().remove(pos);
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
        this.notifyDataSetChanged();
    }

    private void checkNovels(final ArrayList<PPNovel> checkList, final CrawlNovel crawlNovel){
        if(checkList.size() == 0){
            return;
        }
        final PPNovelListAdapter that = this;
        final PPNovel novel =   checkList.remove(0);
        crawlNovel.fetchChapters(novel).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<CrawlChapterResult>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(CrawlChapterResult value) {
                ArrayList<PPNovel> novels = CrawlNovelService.instance().getPPNovels();
                for(int i = 0; i < novels.size(); i++){
                    PPNovel item = novels.get(i);
                    if(item.chapterUrl.compareTo(value.chapterUrl) == 0){
                        item.status = PPNovel.STATUS_CHECKED;
                        that.notifyDataSetChanged();
                        break;
                    }
                }
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

    private Fragment m_parent;
}
