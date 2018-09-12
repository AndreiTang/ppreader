package org.andrei.ppreader.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrei on 2018/9/11.
 */

public class CrawlNovel implements ICrawlNovel {


    @Override
    public int search(String name, List<PPNovel> novels) {
        return 0;
    }

    @Override
    public int fetchChapters(String novelUrl, List<Character> chapters) {
        return 0;
    }

    @Override
    public int fetchNovelText(String chapterUrl, String text) {
        return 0;
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
