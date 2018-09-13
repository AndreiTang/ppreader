package org.andrei.ppreader.service;


import java.util.List;

import io.reactivex.Observable;

/**
 * Created by andrei on 2018/9/11.
 */

public interface ICrawlNovel {
    public Observable<PPNovel> search(String name);
    public Observable<CrawlChapterResult> fetchChapters( PPNovel novel);
    public Observable<CrawlTextResult> fetchNovelText(String novelId, String chapterUrl);
}

