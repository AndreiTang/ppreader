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
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlNovelThrowable;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.PPNovelRxBinding;
import org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * A simple {@link Fragment} subclass.
 */
public class PPNovelReaderFragment extends Fragment implements PPNovelReaderAdapter.ClickPPNovelChapter {


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

        initViewPager(root);

        TextClock tmView = (TextClock) getView().findViewById(R.id.novel_reader_time);
        tmView.setFormat24Hour("HH:mm");
        tmView.setFormat12Hour("hh:mm a");

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(m_batteryReceiver, intentFilter);

    }

    @Override
    public void onClick(String chapter) {
        PPNovelChapter it = m_novel.getPPNovelChapter(chapter);
        fetchChapterText(it);
    }

    private void initViewPager(@NonNull final View root) {

        final ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
        final PPNovelReaderAdapter adapter = new PPNovelReaderAdapter(this, this);
        if (m_novel != null) {
            for (int i = 0; i < m_novel.chapters.size(); i++) {
                PPNovelChapter chapter = m_novel.chapters.get(i);
                PPNovelReaderAdapter.PPNovelTextPage page = new PPNovelReaderAdapter.PPNovelTextPage();
                page.chapter = chapter.url;
                page.title = chapter.name;
                page.text = chapter.text;
                if (page.text.length() > 0) {
                    page.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_LOADING;
                } else {
                    page.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_INIT;
                }
                adapter.addPage(page, false);
                if (i == m_novel.currentChapterIndex && page.text.length() > 0 && m_novel.currentChapterOffset > 0) {
                    for (int j = 1; j <= m_novel.currentChapterOffset; j++) {
                        PPNovelReaderAdapter.PPNovelTextPage pp = new PPNovelReaderAdapter.PPNovelTextPage();
                        pp.offset = j;
                        pp.chapter = page.chapter;
                        pp.isSplited = false;
                        pp.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_LOADING;
                        if(j ==  m_novel.currentChapterOffset){
                            pp.text = page.text;
                        }
                        adapter.addPage(pp, false);
                    }
                }
            }

            //if the current index is beyond the array, reset all to be 0.
            if (m_novel.currentChapterIndex >= m_novel.chapters.size()) {
                m_novel.currentChapterIndex = 0;
                m_novel.currentChapterOffset = 0;
            } else {
                PPNovelChapter chapter = m_novel.chapters.get(m_novel.currentChapterIndex);
                //it mean this text isn't downloaded. its offset must be 0.
                if (chapter.text.length() == 0) {
                    m_novel.currentChapterOffset = 0;
                }
            }

        }

        PPNovelRxBinding.pageSelected(vp).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer position) throws Exception {
                PPNovelReaderAdapter.PPNovelTextPage item = adapter.getItem(position);
                PPNovelChapter chapter = m_novel.getPPNovelChapter(item.chapter);
                assert (chapter != null);
                if (item.status == PPNovelReaderAdapter.PPNovelTextPage.STATUS_INIT) {
                    item.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_LOADING;
                    fetchChapterText(chapter);
                } else if (item.status == PPNovelReaderAdapter.PPNovelTextPage.STATUS_LOADING && chapter.text.length() > 0) {
                    item.text = chapter.text;
                    item.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_OK;
                    adapter.update(position);
                } else {
                    adapter.update(position);
                }

                showChapterInfo(chapter);
            }
        });

        //With full screen , content view(root) can't get the correct size. It cause the viewpager can't get the correct size as well.
        //We directly use the screen height by  DisplayMetrics
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                DisplayMetrics dm = getResources().getDisplayMetrics();
                int h = dm.heightPixels;
                int h1 = getView().findViewById(R.id.novel_action_bar).getMeasuredHeight();
                int h2 = getView().findViewById(R.id.novel_bottom_bar).getMeasuredHeight();
                int tvHeight = h - h1 - h2;
                adapter.setTextViewHeight(tvHeight);
                vp.setAdapter(adapter);
                PPNovelChapter chapter = m_novel.chapters.get(m_novel.currentChapterIndex);
                showChapterInfo(chapter);
                vp.setCurrentItem(m_novel.currentChapterIndex + m_novel.currentChapterOffset);
            }
        });
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

    private void fetchChapterText(PPNovelChapter chapter) {
        m_fetchList.add(chapter);
        if (!m_bRunning) {
            fetchChapterTextProc();
        }
    }

    private void fetchChapterTextProc() {
        if (m_crawlNovel == null) {
            m_crawlNovel = CrawlNovelService.instance().builder(m_novel.engineIndex);
        }
        if (m_fetchList.size() == 0) {
            return;
        }
        m_bRunning = true;
        PPNovelChapter chapter = m_fetchList.remove(0);
        m_crawlNovel.fetchNovelText(m_novel.chapterUrl, chapter.url).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<CrawlTextResult>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(CrawlTextResult value) {
                PPNovelChapter citem = m_novel.getPPNovelChapter(value.chapterUrl);
                citem.text = value.text;
                ViewPager vp = getView().findViewById(R.id.novel_reader_pager);
                PPNovelReaderAdapter adapter = (PPNovelReaderAdapter) vp.getAdapter();
                if (adapter == null) {
                    return;
                }
                PPNovelReaderAdapter.PPNovelTextPage page = adapter.getFirstItemByChapter(value.chapterUrl);
                assert (page != null);
                int index = adapter.getFirstItemPositionByChapter(value.chapterUrl);
                int curr = vp.getCurrentItem();
                if (curr <= index) {
                    page.text = value.text;
                    page.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_OK;
                    adapter.update(index);
                }

                //if this text is not in the current page, don't immedially set value. Otherwise, the page will incorrectly be shown.For example , 前面的页一刷新，会把现在的页面刷掉
            }

            @Override
            public void onError(Throwable e) {
                fetchChapterTextProc();
                if (m_fetchList.size() == 0) {
                    m_bRunning = false;
                }

                CrawlNovelThrowable err = (CrawlNovelThrowable) e;
                ViewPager vp = getView().findViewById(R.id.novel_reader_pager);
                PPNovelReaderAdapter adapter = (PPNovelReaderAdapter) vp.getAdapter();
                if (adapter == null) {
                    return;
                }
                PPNovelReaderAdapter.PPNovelTextPage page = adapter.getFirstItemByChapter(err.chapterUrl);
                assert (page != null);
                page.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_FAIL;
                int index = adapter.getFirstItemPositionByChapter(err.chapterUrl);
                adapter.update(index);
            }

            @Override
            public void onComplete() {
                fetchChapterTextProc();
                if (m_fetchList.size() == 0) {
                    m_bRunning = false;
                }
            }
        });
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
    private CrawlNovel m_crawlNovel = null;
    private PPNovel m_novel;
    private ArrayList<PPNovelChapter> m_fetchList = new ArrayList<PPNovelChapter>();
    private boolean m_bRunning = false;


}
