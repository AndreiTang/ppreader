package org.andrei.ppreader.service;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by andrei on 2018/9/11.
 */

public class PPNovel implements Serializable{



    public static final int STATUS_UNCHECKED = 0;
    public static final int STATUS_CHECKED = 1;
    public static final int STATUS_CONFIRMED = 2;
    public static final int TYPE_ING = 0;
    public static final int TYPE_OVER = 1;
    private static final long serialVersionUID = 5160714128467001373L;

    @Expose
    public int engineIndex = 0;
    @Expose
    public String chapterUrl = "";
    @Expose
    public String imgUrl = "";
    @Expose
    public String name = "";
    @Expose
    public String desc = "";
    @Expose
    public String author = "";

    public transient int status = STATUS_UNCHECKED;
    @Expose
    public ArrayList<PPNovelChapter> chapters = new ArrayList<PPNovelChapter>();
    public transient boolean needRemove = false;
    @Expose
    public int type = TYPE_ING;
    @Expose
    public long duration = 0;
    @Expose
    public long lastReadTime = 0;
    @Expose
    public int currentChapterIndex= 0;
    @Expose
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
