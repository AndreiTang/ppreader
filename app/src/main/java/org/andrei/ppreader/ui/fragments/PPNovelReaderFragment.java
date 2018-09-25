package org.andrei.ppreader.ui.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovelThrowable;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.helper.PPNovelRxBinding;
import org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter;
import org.andrei.ppreader.ui.helper.PPNovelReaderPageManager;
import org.andrei.ppreader.ui.helper.PPNovelTextPage;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * A simple {@link Fragment} subclass.
 */
public class PPNovelReaderFragment extends Fragment  {


    public PPNovelReaderFragment() {
        // Required empty public constructor
    }

    public void setPPNovel(PPNovel novel) {
        m_novel = novel;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ppnovel_reader, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        final View root = this.getActivity().findViewById(android.R.id.content);
        root.findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        //initialize viewpager
        initViewPager(root);

        //set select callback
        final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
        PPNovelRxBinding.pageSelected(vp).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer position) throws Exception {
                selectCurrentItem(position);
            }
        });

        //set the time clock
        TextClock tmView = (TextClock) getView().findViewById(R.id.novel_reader_time);
        tmView.setFormat24Hour("HH:mm");
        tmView.setFormat12Hour("hh:mm a");

        //monitor the battery
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(m_batteryReceiver, intentFilter);

    }


    private void initViewPager(@NonNull final View root) {
        final Fragment parent = this;
        //With full screen , content view(root) can't get the correct size. It cause the viewpager can't get the correct size as well.
        //We directly use the screen height by  DisplayMetrics
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                DisplayMetrics dm = getResources().getDisplayMetrics();
                int h = dm.heightPixels;
                int h1 = getView().findViewById(R.id.novel_action_bar).getMeasuredHeight();
                int h2 = getView().findViewById(R.id.novel_bottom_bar).getMeasuredHeight();
                int tvHeight = h - h1 - h2;
                m_pageMgr = new PPNovelReaderPageManager(m_novel,tvHeight);
                final PPNovelReaderAdapter adapter = new PPNovelReaderAdapter(parent,m_pageMgr);

                initFetchPPNovelTextCallback();

                vp.setAdapter(adapter);

                PPNovelChapter chapter = m_novel.chapters.get(m_novel.currentChapterIndex);
                showChapterInfo(chapter);
                vp.setCurrentItem(m_novel.currentChapterIndex + m_novel.currentChapterOffset);
            }
        });
    }

    private void initFetchPPNovelTextCallback(){

        m_pageMgr.getPPNovelTextPageObservable().subscribe(new Observer<Integer>(){

            @Override
            public void onSubscribe(Disposable d) {
                
            }

            @Override
            public void onNext(Integer index) {
                //if this text is not in the current page, don't immedially set value. Otherwise, the page will incorrectly be shown.For example , 前面的页一刷新，会把现在的页面刷掉
                final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
                final PPNovelReaderAdapter adapter = (PPNovelReaderAdapter)vp.getAdapter();
                int curr = vp.getCurrentItem();
                if (curr == index) {
                    PPNovelTextPage page = m_pageMgr.getItem(index);
                    page.status = PPNovelTextPage.STATUS_OK;
                    adapter.update(index);
                }
            }

            @Override
            public void onError(Throwable e) {
                final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
                final PPNovelReaderAdapter adapter = (PPNovelReaderAdapter)vp.getAdapter();
                CrawlNovelThrowable err = (CrawlNovelThrowable) e;
                int pos = m_pageMgr.getFirstChapterItemPosition(err.chapterUrl);
                adapter.update(pos);
            }

            @Override
            public void onComplete() {

            }
        } );
    }

    private void selectCurrentItem(int position){
        final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
        PPNovelReaderAdapter adapter = (PPNovelReaderAdapter)vp.getAdapter();
        PPNovelTextPage item = m_pageMgr.getItem(position);
        PPNovelChapter chapter = m_novel.getPPNovelChapter(item.chapter);
        assert (item != null);
        if (item.status == PPNovelTextPage.STATUS_INIT) {
            item.status = PPNovelTextPage.STATUS_LOADING;
            m_pageMgr.fetchChapterText(item);
        } else if (item.status == PPNovelTextPage.STATUS_LOADED) {
            item.status = PPNovelTextPage.STATUS_OK;
            adapter.update(position);
        } else {
            adapter.update(position);
        }
        showChapterInfo(chapter);
    }

    private void showChapterInfo(PPNovelChapter chapter) {
        TextView tv = getView().findViewById(R.id.novel_reader_title);
        tv.setText(chapter.name);

        int index = m_novel.getPPNovelPosition(chapter.url);
        if (index != -1) {
            index++;
            String sIndex = String.format("%1$d/%2$d", index, m_novel.chapters.size());
            tv = getView().findViewById(R.id.novel_bottom_bar);
            tv.setText(sIndex);
        }
    }

    private int getBatteryId(int per) {
        if (per > 90)
            return R.drawable.battery_100_90;
        else if (per > 80)
            return R.drawable.battery_90_80;
        else if (per > 70)
            return R.drawable.battery_80_70;
        else if (per > 60)
            return R.drawable.battery_70_60;
        else if (per > 50)
            return R.drawable.battery_60_50;
        else if (per > 40)
            return R.drawable.battery_50_40;
        else if (per > 30)
            return R.drawable.battery_40_30;
        else if (per > 20)
            return R.drawable.battery_30_20;
        else if (per > 10)
            return R.drawable.battery_20_10;
        else
            return R.drawable.battery_10_0;
    }

    private BroadcastReceiver m_batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int power = level * 100 / scale;
                int id = getBatteryId(power);
                ImageView battery = (ImageView) getView().findViewById(R.id.novel_reader_battery);
                battery.setImageResource(id);
            }
        }
    };
    private PPNovel m_novel;
    private PPNovelReaderPageManager m_pageMgr = null;
}
