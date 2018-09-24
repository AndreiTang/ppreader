package org.andrei.ppreader.ui.helper;

import android.view.Gravity;

import java.util.ArrayList;

public class PPNovelTextPage {
    public final static int STATUS_OK = 0;
    public final static int STATUS_LOADING = 1;
    public final static int STATUS_LOADED = 2;
    public final static int STATUS_FAIL = 3;
    public final static int STATUS_INIT = 4;



    public String text = "";
    public int offset = 0;
    public boolean isSplit = false;
    public String title = "";
    public String chapter = "";
    public int status = STATUS_INIT;
    public ArrayList<String> lines = new ArrayList<String>();
    int gravity = Gravity.BOTTOM ;
}
