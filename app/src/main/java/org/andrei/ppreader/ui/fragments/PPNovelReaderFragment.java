package org.andrei.ppreader.ui.fragments;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import java.util.ArrayList;
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

    public PPNovel getNovel() {
        return m_novel;
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
                preloadPPNovelTextPage(position);
            }
        });

        //set the time clock
        TextClock tmView = (TextClock) getView().findViewById(R.id.novel_reader_time);
        tmView.setFormat24Hour("HH:mm");
        tmView.setFormat12Hour("hh:mm a");


        //set control panel callback
        PPNovelReaderControlPanel panel = getView().findViewById(R.id.novel_reader_panel);
        panel.click().subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer type) throws Exception {
                if (type == PPNovelReaderControlPanel.DICT) {
                    openControlPanel();
                }
                else if(type == PPNovelReaderControlPanel.LIST || type == PPNovelReaderControlPanel.SEARCH){
                    int pos = 0;
                    if(type == PPNovelReaderControlPanel.SEARCH){
                        pos = 1;
                    }
                    switchToMainFragment(pos);
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
    public void onResume(){
        super.onResume();
        //when the page restore, full screen need be reset
        getView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        m_beginTime = System.currentTimeMillis();
    }

    @Override
    public void onPause(){
        super.onPause();
        m_novel.duration += System.currentTimeMillis() - m_beginTime;
        m_novel.lastReadTime = System.currentTimeMillis();

        if(CrawlNovelService.instance().getNovel(m_novel.chapterUrl) != null){
            CrawlNovelService.instance().saveNovel(getActivity().getApplicationContext().getFilesDir().getAbsolutePath(),m_novel);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        //monitor the battery
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(m_batteryReceiver, intentFilter);
    }

    @Override
    public void onStop(){
        super.onStop();
        getActivity().unregisterReceiver(m_batteryReceiver);

    }

    @Override
    public void onDestroyView(){
        final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
        PPNovelReaderAdapter adapter = (PPNovelReaderAdapter)vp.getAdapter();
        adapter.clear();
        super.onDestroyView();
    }

    public void switchToMainFragment(final int pos){
        if(CrawlNovelService.instance().getNovel(m_novel.chapterUrl) != null){
            switchToMainFragmentInner(pos);
        }
        else{
            AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
            String msg = getString(R.string.novel_list_add_msg);
            msg = String.format(msg,m_novel.name);
            dlg.setMessage(msg);
            dlg.setNegativeButton(R.string.btn_cancel,new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switchToMainFragmentInner(pos);
                }
            });
            dlg.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    CrawlNovelService.instance().getPPNovels().add(m_novel);
                    switchToMainFragmentInner(pos);
                }
            });
            dlg.show();
        }
    }

    private void preloadPPNovelTextPage(final int pos){
        ArrayList<PPNovelTextPage> pages = m_pageMgr.getPages();
        for(int i = pos +1 ; i < pages.size(); i++){
            PPNovelTextPage page = pages.get(i);
            if(page.offset == 0){
                if(page.status  == PPNovelTextPage.STATUS_INIT){
                    m_pageMgr.fetchChapterText(page);
                    page.status = PPNovelTextPage.STATUS_LOADING;
                }
                return;
            }
        }
    }

    private void openControlPanel(){
        ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
        int curr = vp.getCurrentItem();
        PPNovelTextPage page = m_pageMgr.getItem(curr);
        assert (page != null);
        int pos = m_novel.getPPNovelPosition(page.chapter);

        TextView tv = getView().findViewById(R.id.novel_dict_progress);
        String per =pos*100/m_novel.chapters.size() + "%";
        tv.setText(per);

        long duration = (m_novel.duration + System.currentTimeMillis() - m_beginTime)/1000;
        long h = duration/3600;
        long m = (duration%3600)/60;
        String ds = getActivity().getString(R.string.novel_dict_duration);
        ds = String.format(ds,h,m);
        tv = getView().findViewById(R.id.novel_dict_duration);
        tv.setText(ds);

        ListView lv = getView().findViewById(R.id.novel_dict_list);
        lv.setSelection(pos);

        getView().findViewById(R.id.novel_reader_dict).setVisibility(View.VISIBLE);
    }

    private void switchToMainFragmentInner(int pos){
        Fragment fragment = new PPNovelMainFragment();
        Bundle arg = new Bundle();
        arg.putInt(PPNovelMainFragment.POS,pos);
        fragment.setArguments(arg);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container,fragment,PPNovelMainFragment.TAG);
        transaction.commit();
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

    //initialize the control panel
    private void initPPNovelDict() {
        final LinearLayout dict = getView().findViewById(R.id.novel_reader_dict);
        TextView tv = dict.findViewById(R.id.novel_dict_author);
        tv.setText(m_novel.author);
        tv = dict.findViewById(R.id.novel_dict_name);
        tv.setText(m_novel.name);
        ImageView img = dict.findViewById(R.id.novel_dict_img);
        Glide.with(dict).clear(img);
        Glide.with(dict).load(m_novel.imgUrl).apply(RequestOptions.fitCenterTransform()).into(img);

        //click to return reader page
        View v = getView().findViewById(R.id.novel_dict_mask);
        RxView.clicks(v).throttleFirst(200, TimeUnit.MICROSECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object obj) throws Exception {
                dict.setVisibility(View.GONE);
            }
        });

        //switch between group and dict
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

        //prev page
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

        //next page
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

    private void selectCurrentItem(int position) {
        final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
        PPNovelReaderAdapter adapter = (PPNovelReaderAdapter) vp.getAdapter();
        PPNovelTextPage item = m_pageMgr.getItem(position);
        PPNovelChapter chapter = m_novel.getPPNovelChapter(item.chapter);
        adapter.selectCurrentItem(position);
        showChapterInfo(chapter);
        m_novel.currentChapterIndex = m_novel.getPPNovelPosition(chapter.url);
        m_novel.currentChapterOffset = item.offset;
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
    private long m_beginTime = 0;
}
