package org.andrei.ppreader.service;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by andrei on 2018/9/12.
 */

public class CrawlNovelService {


    public static  CrawlNovelService instance(){
        return m_s_ins;
    }

    public CrawlNovel builder(){
        return new CrawlNovel();
    }

    public CrawlNovel builder(int index){
        CrawlNovel crawlNovel =  new CrawlNovel();
        crawlNovel.setCurrentCrawlNovelEngine(index);
        return crawlNovel;
    }

    public  ArrayList<PPNovel> getPPNovels(){
        return m_novels;
    }

    public PPNovel getNovel(String id){
        return null;
    }

    public void removeNovel(String id){

    }

    public Observable<PPNovel> loadPPNovels(){
        Observable<PPNovel> observable = Observable.create(new ObservableOnSubscribe<PPNovel>() {
            @Override
            public void subscribe(ObservableEmitter<PPNovel> emitter) throws Exception {
                Thread.sleep(5000);
                emitter.onComplete();
            }
        });
        return observable.subscribeOn(Schedulers.io());
    }

    private static CrawlNovelService  m_s_ins = new CrawlNovelService();
    private ArrayList<PPNovel> m_novels = new ArrayList<PPNovel>();
}
