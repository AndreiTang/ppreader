package org.andrei.ppreader.ui.adapters;

import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class PPNovelReaderAdapter {



    private void fetchChapterText(PPNovelChapter chapter){
        m_fetchList.add(chapter);
        if(!m_bRunning){
            fetchChapterTextProc();
        }
    }

    private void fetchChapterTextProc(){
        if(m_crawlNovel == null){
            m_crawlNovel = CrawlNovelService.instance().builder(m_novel.engineIndex);
        }
        if(m_fetchList.size() == 0){
            return;
        }
        m_bRunning = true;
        PPNovelChapter chapter = m_fetchList.remove(0);
        m_crawlNovel.fetchNovelText(m_novel.chapterUrl,chapter.url).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<CrawlTextResult>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(CrawlTextResult value) {

            }

            @Override
            public void onError(Throwable e) {
                fetchChapterTextProc();
                if(m_fetchList.size() == 0){
                    m_bRunning = false;
                }
            }

            @Override
            public void onComplete() {
                fetchChapterTextProc();
                if(m_fetchList.size() == 0){
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
