package org.andrei.ppreader.service;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Created by andrei on 2018/9/11.
 */

public interface ICrawlNovel {
    public int search(final String name,ObservableEmitter<PPNovel> e);
    public int fetchChapters( final PPNovel novel, CrawlChapterResult ret);
    public int fetchNovelText(final String novelId, final String chapterUrl,CrawlTextResult ret);
    public String getName();
}

