package org.andrei.ppreader.service.engines;

import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.ICrawlNovel;
import org.andrei.ppreader.service.PPNovel;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static io.reactivex.Observable.create;

public class Crawl88dusNovel implements ICrawlNovel {
    @Override
    public int search(String name, ArrayList<PPNovel> novels) {

        //mock data


        return 0;
    }

    @Override
    public Observable<CrawlChapterResult> fetchChapters(PPNovel novel) {
        return null;
    }

    @Override
    public Observable<CrawlTextResult> fetchNovelText(String novelId, String chapterUrl) {
        return null;
    }
}
