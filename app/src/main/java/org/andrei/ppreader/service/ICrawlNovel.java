package org.andrei.ppreader.service;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by andrei on 2018/9/11.
 */

public interface ICrawlNovel {
    public int search(String name, ArrayList<PPNovel> novels);
    public Observable<CrawlChapterResult> fetchChapters( PPNovel novel);
    public Observable<CrawlTextResult> fetchNovelText(String novelId, String chapterUrl);
}

