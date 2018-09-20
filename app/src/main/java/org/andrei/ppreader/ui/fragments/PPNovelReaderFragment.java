package org.andrei.ppreader.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelError;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlNovelThrowable;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * A simple {@link Fragment} subclass.
 */
public class PPNovelReaderFragment extends Fragment {


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
        ViewPager vp = (ViewPager) getView().findViewById(R.id.novel_reader_pager);
        final PPNovelReaderAdapter adapter = new PPNovelReaderAdapter(this);
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                PPNovelReaderAdapter.PPNovelTextPage item = adapter.getItem(position);
                PPNovelChapter chapter = m_novel.getPPNovelChapter(item.chapter);
                assert (chapter != null);
                if (item.status == PPNovelReaderAdapter.PPNovelTextPage.STATUS_INIT) {
                    item.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_LOADING;
                    fetchChapterText(chapter);
                }
                else if(item.status == PPNovelReaderAdapter.PPNovelTextPage.STATUS_LOADING && chapter.text.length() > 0){
                    item.text = chapter.text;
                    item.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_OK;
                    adapter.update(position);
                }
                else {
                    adapter.update(position);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (m_novel != null) {
            for (PPNovelChapter chapter : m_novel.chapters) {
                PPNovelReaderAdapter.PPNovelTextPage page = new PPNovelReaderAdapter.PPNovelTextPage();
                page.chapter = chapter.url;
                page.title = chapter.name;
                page.text = chapter.text;
                if (page.text.length() > 0) {
                    page.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_OK;
                } else {
                    page.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_INIT;
                }
                adapter.addPage(page, false);
            }
            adapter.notifyDataSetChanged();
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
                PPNovelReaderAdapter.PPNovelTextPage page = adapter.getItemByChapter(value.chapterUrl);
                assert (page != null);
                int index = adapter.getItemPositionByChapter(value.chapterUrl);
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
                PPNovelReaderAdapter.PPNovelTextPage page = adapter.getItemByChapter(err.chapterUrl);
                assert (page != null);
                page.status = PPNovelReaderAdapter.PPNovelTextPage.STATUS_FAIL;
                int index = adapter.getItemPositionByChapter(err.chapterUrl);
                adapter.update(index);
//                int curr = vp.getCurrentItem();
//                if (curr == index) {
//
//                }

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

    private CrawlNovel m_crawlNovel = null;
    private PPNovel m_novel;
    private ArrayList<PPNovelChapter> m_fetchList = new ArrayList<PPNovelChapter>();
    private boolean m_bRunning = false;
}
