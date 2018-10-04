package org.andrei.ppreader.service;

import android.support.annotation.NonNull;

import org.andrei.ppreader.service.engines.Crawl88dusNovel;
import org.andrei.ppreader.service.engines.CrawlMockNovel;
import org.andrei.ppreader.service.engines.CrawlTianYiNovel;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by andrei on 2018/9/11.
 */

public class CrawlNovel implements ICrawlNovel {

    @Override
    public Observable<PPNovel> search(final String name) {

        //cancel the last searching.
        if (m_searchDisposable != null) {
            m_searchDisposable.dispose();
            m_searchDisposable = null;
        }
        final ArrayList<ICrawlNovel> crawlNovels = new ArrayList<ICrawlNovel>(m_s_crawlNovelEngines);
        return Observable.create(new ObservableOnSubscribe<PPNovel>() {
            @Override
            public void subscribe(ObservableEmitter<PPNovel> e) throws Exception {
                searchProc(name, crawlNovels, e);
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (m_searchDisposable != null) {
                    m_searchDisposable = null;
                }
            }
        }).doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                if (m_searchDisposable != null) {
                    m_searchDisposable = null;
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<CrawlChapterResult> fetchChapters(final PPNovel novel) {
        ICrawlNovel crawlNovels = m_s_crawlNovelEngines.get(m_currEngineIndex);
        return crawlNovels.fetchChapters(novel);
    }

    @Override
    public Observable<CrawlTextResult> fetchNovelText(final String novelId, final String chapterUrl) {
        ICrawlNovel crawlNovels = m_s_crawlNovelEngines.get(m_currEngineIndex);
        return crawlNovels.fetchNovelText(novelId, chapterUrl);
    }

    protected CrawlNovel() {
    }


    private void searchProc(final String name, final ArrayList<ICrawlNovel> crawlNovels, final ObservableEmitter<PPNovel> emitter) {
        m_searchDisposable = null;
        if (crawlNovels.size() == 0) {
            return;
        }

        ICrawlNovel crawlNovel = crawlNovels.remove(0);
        crawlNovel.search(name).subscribeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<PPNovel>() {

            Disposable m_disposable = null;
            @Override
            public void onSubscribe(Disposable d) {
                m_disposable = d;
                m_searchDisposable = d;
            }

            @Override
            public void onNext(PPNovel value) {
                emitter.onNext(value);
            }

            @Override
            public void onError(Throwable e) {
                if(m_disposable.isDisposed()){
                    return;
                }
                if (crawlNovels.size() == 0) {
                    Throwable err = new Throwable(name);
                    emitter.onError(err);
                } else {
                    searchProc(name, crawlNovels, emitter);
                }
            }

            @Override
            public void onComplete() {
                emitter.onComplete();
            }
        });
    }

    public void setCurrentCrawlNovelEngine(int index) {
        m_currEngineIndex = index;
    }

    private static ArrayList<ICrawlNovel> m_s_crawlNovelEngines = new ArrayList<ICrawlNovel>();
    private int m_currEngineIndex = 0;
    volatile Disposable m_searchDisposable = null;

    static {
        ICrawlNovel crawlNovel = new CrawlTianYiNovel();
        m_s_crawlNovelEngines.add(crawlNovel);

        crawlNovel = new Crawl88dusNovel();
        m_s_crawlNovelEngines.add(crawlNovel);

        crawlNovel = new CrawlMockNovel();
        m_s_crawlNovelEngines.add(crawlNovel);
    }

}
