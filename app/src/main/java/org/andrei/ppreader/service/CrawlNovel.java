package org.andrei.ppreader.service;

import android.support.annotation.NonNull;

import org.andrei.ppreader.service.engines.Crawl88dusNovel;
import org.andrei.ppreader.service.engines.CrawlTianYiNovel;
import java.util.ArrayList;
import io.reactivex.ObservableEmitter;


/**
 * Created by andrei on 2018/9/11.
 */

public class CrawlNovel implements ICrawlNovel {

    @Override
    public int search(final String name,ObservableEmitter<PPNovel> e, CrawlNovelResult crawlNovelResult) {

        String search[] = name.split("#");
        ArrayList<ICrawlNovel> engines = new ArrayList<ICrawlNovel>();
        String novelName = name;
        if(search[0].compareTo("0") == 0){
            engines.add(m_s_crawlNovelEngines.get(0));
            novelName = search[1];
        }
        else if(search[0].compareTo("1") == 0){
            engines.add(m_s_crawlNovelEngines.get(1));
            novelName = search[1];
        }
        else{
            engines.addAll(m_s_crawlNovelEngines);
        }

        int ret = CrawlNovelError.ERR_NONE;
        for(int i = 0 ; i < engines.size(); i++){
            ICrawlNovel crawlNovel = engines.get(i);
            ret = crawlNovel.search(novelName,e,crawlNovelResult);
            if(ret == CrawlNovelError.ERR_NONE){
                break;
            }
            else if(ret == CrawlNovelError.ERR_NETWORK){
                break;
            }
        }

        return ret;
    }

    @Override
    public int fetchNovels(String url, ObservableEmitter<PPNovel> e) {
        ICrawlNovel crawlNovels = m_s_crawlNovelEngines.get(m_currEngineIndex);
        return crawlNovels.fetchNovels(url,e);
    }

    @Override
    public int fetchChapters(final PPNovel novel,CrawlChapterResult ret) {
        ICrawlNovel crawlNovels = m_s_crawlNovelEngines.get(m_currEngineIndex);
        return crawlNovels.fetchChapters(novel,ret);
    }

    @Override
    public int fetchNovelText(final String novelId, final String chapterUrl,CrawlTextResult ret) {
        ICrawlNovel crawlNovels = m_s_crawlNovelEngines.get(m_currEngineIndex);
        return crawlNovels.fetchNovelText(novelId, chapterUrl,ret);
    }

    @Override
    public String getName() {
        return "crawlnovel";
    }

    protected CrawlNovel() {
    }


    public void setCurrentCrawlNovelEngine(final String engineName) {
        m_currEngineIndex = 0;
        for(int i = 0 ; i < m_s_crawlNovelEngines.size(); i ++){
            ICrawlNovel crawlNovel = m_s_crawlNovelEngines.get(i);
            if(crawlNovel.getName().compareTo(engineName) == 0){
                m_currEngineIndex = i;
                break;
            }
        }
    }

    private static ArrayList<ICrawlNovel> m_s_crawlNovelEngines = new ArrayList<ICrawlNovel>();
    private int m_currEngineIndex = 0;

    static {
        ICrawlNovel crawlNovel = new Crawl88dusNovel();
        m_s_crawlNovelEngines.add(crawlNovel);

        crawlNovel = new CrawlTianYiNovel();
        m_s_crawlNovelEngines.add(crawlNovel);

//        crawlNovel = new CrawlMockNovel();
//        m_s_crawlNovelEngines.add(crawlNovel);
    }

}
