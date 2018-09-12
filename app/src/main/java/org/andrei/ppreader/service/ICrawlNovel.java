package org.andrei.ppreader.service;


import java.util.List;

/**
 * Created by andrei on 2018/9/11.
 */

public interface ICrawlNovel {
    public int search(String name,List<PPNovel> novels);
    public int fetchChapters(String novelUrl,List<Character> chapters);
    public int fetchNovelText(String chapterUrl, String text);
}

