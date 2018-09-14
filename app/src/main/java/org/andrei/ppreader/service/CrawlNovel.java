package org.andrei.ppreader.service;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by andrei on 2018/9/11.
 */

public class CrawlNovel  {


    public Observable<PPNovel> search(String name) {

        return null;
    }


    public Observable<CrawlChapterResult> fetchChapters(final PPNovel novel) {
        return Observable.create(new ObservableOnSubscribe<CrawlChapterResult>() {
            @Override
            public void subscribe(ObservableEmitter<CrawlChapterResult> e) throws Exception {
                CrawlChapterResult ret = new CrawlChapterResult();
                ret.chapterUrl = novel.chapterUrl;
                e.onNext(ret);
                e.onComplete();
            }
        });
    }


    public Observable<CrawlTextResult> fetchNovelText(String novelId, String chapterUrl) {
        return null;
    }

    protected CrawlNovel() {
        m_currEngineIndex = 0;
        loadEngines();
    }

    public void setCurrentCrawlNovelEngine(int index) {
        m_currEngineIndex = index;
    }

    private void loadEngines() {

    }



    private static ArrayList<ICrawlNovel> m_s_crawlNovelEngines = new ArrayList<ICrawlNovel>();
    private int m_currEngineIndex = -1;


}
