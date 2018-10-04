package org.andrei.ppreader.service;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by andrei on 2018/9/11.
 */

public class PPNovelChapter implements Serializable{
    private static final long serialVersionUID = 8989030620935315777L;
    @Expose
    public String url="";
    @Expose
    public String text="";
    @Expose
    public String name="";
}
