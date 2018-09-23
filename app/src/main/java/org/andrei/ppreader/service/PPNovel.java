package org.andrei.ppreader.service;

import java.util.ArrayList;

/**
 * Created by andrei on 2018/9/11.
 */

public class PPNovel {

    public static final int STATUS_UNCHECKED = 0;
    public static final int STATUS_CHECKED = 1;
    public static final int STATUS_CONFIRMED = 2;
    public static final int TYPE_ING = 0;
    public static final int TYPE_OVER = 1;

    public int engineIndex = 0;
    public String chapterUrl = "";
    public String imgUrl = "";
    public String name = "";
    public String desc = "";
    public String author = "";
    public int status = STATUS_UNCHECKED;
    public ArrayList<PPNovelChapter> chapters = new ArrayList<PPNovelChapter>();
    public boolean needRemove = false;
    public int type = TYPE_ING;
    public int currentChapterIndex= 0;
    public int currentChapterOffset = 0;

    public PPNovelChapter getPPNovelChapter(String chapterUrl){
        for(PPNovelChapter chapter : chapters){
            if(chapter.url.compareTo(chapterUrl) == 0){
                return chapter;
            }
        }
        return null;
    }

    public int getPPNovelPosition(String chapterUrl){
        for(int i = 0 ; i < chapters.size() ; i++){
            PPNovelChapter chapter = chapters.get(i);
            if(chapter.url.compareTo(chapterUrl) == 0){
                return i;
            }
        }
        return -1;
    }

}
