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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlNovelThrowable;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.adapters.PPNovelDictAdapter;
import org.andrei.ppreader.ui.adapters.PPNovelDictGroupAdapter;
import org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter;
import org.andrei.ppreader.ui.helper.PPNovelReaderPageManager;
import org.andrei.ppreader.ui.helper.PPNovelRxBinding;
import org.andrei.ppreader.ui.helper.PPNovelTextPage;
import org.andrei.ppreader.ui.views.PPNovelReaderControlPanel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * A simple {@link Fragment} subclass.
 */
public class PPNovelReaderFragment extends Fragment {

    public final static String TAG = "PPNovelReaderFragment";

    public PPNovelReaderFragment() {
        // Required empty public constructor
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
        Bundle arg = getArguments();
        if(arg != null){
            m_novel = (PPNovel) arg.getSerializable(NOVEL);
        }
        else{
            if(savedInstanceState != null){
                m_novel = (PPNovel) savedInstanceState.getSerializable(NOVEL);
            }
        }

        assert (m_novel!=null);

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

        PPNovelReaderControlPanel panel = getView().findViewById(R.id.novel_reader_panel);
        panel.click().subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer type) throws Exception {
                if (type == PPNovelReaderControlPanel.DICT) {
                    getView().findViewById(R.id.novel_reader_dict).setVisibility(View.VISIBLE);
                }
            }
        });

        initPPNovelDict();
    }

    @Override
    public void onSaveInstanceState (Bundle outState){
        outState.putSerializable(NOVEL,m_novel);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop(){
        super.onStop();
        m_pageMgr.disposableFetchText();
        getActivity().unregisterReceiver(m_batteryReceiver);
    }


    private void initViewPagerTouch() {
        m_gestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });

        m_gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                PPNovelReaderControlPanel panel = getView().findViewById(R.id.novel_reader_panel);
                panel.display((int) e.getRawX(), (int) e.getRawY());
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        getView().findViewById(R.id.novel_reader_pager).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                m_gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    private void initViewPager(@NonNull final View root) {
        final Fragment parent = this;
        //With full screen , content view(root) can't get the correct size . It cause the viewpager can't get the correct size as well.
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
                m_pageMgr = new PPNovelReaderPageManager(m_novel, tvHeight);
                final PPNovelReaderAdapter adapter = new PPNovelReaderAdapter(parent, m_pageMgr);

                initFetchPPNovelTextCallback();

                vp.setAdapter(adapter);

                PPNovelChapter chapter = m_novel.chapters.get(m_novel.currentChapterIndex);
                showChapterInfo(chapter);
                vp.setCurrentItem(m_novel.currentChapterIndex + m_novel.currentChapterOffset);

                initPPNovelDictAdapter();
                initPPNovelDictGroupAdapter();
            }
        });
        initViewPagerTouch();
    }

    private void initPPNovelDict() {
        final LinearLayout dict = getView().findViewById(R.id.novel_reader_dict);
        TextView tv = dict.findViewById(R.id.novel_dict_author);
        tv.setText(m_novel.author);
        tv = dict.findViewById(R.id.novel_dict_name);
        tv.setText(m_novel.name);
        ImageView img = dict.findViewById(R.id.novel_dict_img);
        Glide.with(dict).clear(img);
        Glide.with(dict).load(m_novel.imgUrl).apply(RequestOptions.fitCenterTransform()).into(img);

        View v = getView().findViewById(R.id.novel_dict_mask);
        RxView.clicks(v).throttleFirst(200, TimeUnit.MICROSECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object obj) throws Exception {
                dict.setVisibility(View.GONE);
            }
        });

        v = getView().findViewById(R.id.novel_dict_group);
        RxView.clicks(v).throttleFirst(200, TimeUnit.MICROSECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object obj) throws Exception {
                View l = dict.findViewById(R.id.novel_dict_list);
                View g = dict.findViewById(R.id.novel_dict_group_list);
                if (l.getVisibility() == View.GONE) {
                    l.setVisibility(View.VISIBLE);
                    g.setVisibility(View.GONE);
                } else {
                    l.setVisibility(View.GONE);
                    g.setVisibility(View.VISIBLE);
                }

            }
        });

        v = getView().findViewById(R.id.novel_dict_prev);
        RxView.clicks(v).throttleFirst(200, TimeUnit.MICROSECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object obj) throws Exception {
                ListView l = dict.findViewById(R.id.novel_dict_list);
                ListView g = dict.findViewById(R.id.novel_dict_group_list);
                ListView lv = l;
                if (g.getVisibility() == View.VISIBLE) {
                    lv = g;
                }

                int begin = lv.getFirstVisiblePosition() - 1;
                int len = lv.getLastVisiblePosition() - lv.getFirstVisiblePosition();
                begin = begin - len;
                if (begin >= 0) {
                    lv.setSelection(begin);
                }
                else{
                    lv.setSelection(0);
                }
            }
        });

        v = getView().findViewById(R.id.novel_dict_next);
        RxView.clicks(v).throttleFirst(200, TimeUnit.MICROSECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object obj) throws Exception {
                ListView l = dict.findViewById(R.id.novel_dict_list);
                ListView g = dict.findViewById(R.id.novel_dict_group_list);
                ListView lv = l;
                if (g.getVisibility() == View.VISIBLE) {
                    lv = g;
                }
                int end = lv.getLastVisiblePosition() + 1 ;
                if(end <= lv.getAdapter().getCount()-1){
                    lv.setSelection(end);
                }

            }
        });


    }

    private void initPPNovelDictAdapter() {
        final LinearLayout dict = getView().findViewById(R.id.novel_reader_dict);
        ListView lv = (ListView) dict.findViewById(R.id.novel_dict_list);
        PPNovelDictAdapter adapter = new PPNovelDictAdapter(this, m_novel, m_pageMgr);
        lv.setAdapter(adapter);
        adapter.clicks().subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer pos) throws Exception {
                final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
                vp.setCurrentItem(pos);
                dict.setVisibility(View.GONE);
            }
        });
    }

    private void initPPNovelDictGroupAdapter() {
        final LinearLayout dict = getView().findViewById(R.id.novel_reader_dict);
        ListView lv = (ListView) dict.findViewById(R.id.novel_dict_group_list);
        PPNovelDictGroupAdapter adapter = new PPNovelDictGroupAdapter(this, m_novel);
        lv.setAdapter(adapter);
        adapter.clicks().subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer pos) throws Exception {
                ListView lv = (ListView) dict.findViewById(R.id.novel_dict_list);
                lv.setSelection(pos);
                lv.setVisibility(View.VISIBLE);
                lv = (ListView) dict.findViewById(R.id.novel_dict_group_list);
                lv.setVisibility(View.GONE);
            }
        });
    }

    private void initFetchPPNovelTextCallback() {

        m_pageMgr.getPPNovelTextPageObservable().subscribe(new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer index) {
                //if this text is not in the current page, don't immedially set value. Otherwise, the page will incorrectly be shown.For example , 前面的页一刷新，会把现在的页面刷掉
                final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
                final PPNovelReaderAdapter adapter = (PPNovelReaderAdapter) vp.getAdapter();
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
                final PPNovelReaderAdapter adapter = (PPNovelReaderAdapter) vp.getAdapter();
                CrawlNovelThrowable err = (CrawlNovelThrowable) e;
                int pos = m_pageMgr.getFirstChapterItemPosition(err.chapterUrl);
                adapter.update(pos);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void selectCurrentItem(int position) {
        final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
        PPNovelReaderAdapter adapter = (PPNovelReaderAdapter) vp.getAdapter();
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
    private GestureDetector m_gestureDetector = null;
    public final static String NOVEL = "novel";
}
