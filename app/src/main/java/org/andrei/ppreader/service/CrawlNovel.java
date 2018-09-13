package org.andrei.ppreader.service;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by andrei on 2018/9/11.
 */

public class CrawlNovel implements ICrawlNovel {

    @Override
    public Observable<PPNovel> search(String name) {
        return null;
    }

    @Override
    public Observable<CrawlChapterResult> fetchChapters(PPNovel novel) {
        return null;
    }

    @Override
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
