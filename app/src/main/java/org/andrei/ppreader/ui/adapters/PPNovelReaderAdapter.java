package org.andrei.ppreader.ui.adapters;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.PPNovelTitleCenterBoldSpan;


import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static org.andrei.ppreader.ui.Utils.autoSplitText;
import static org.andrei.ppreader.ui.Utils.half2full;

public class PPNovelReaderAdapter extends PagerAdapter {


    public PPNovelReaderAdapter(Fragment parent){
        m_parent = parent;
        //mock
        PPNovelTextPage page = new PPNovelTextPage();
    }

    public void addPage(PPNovelTextPage page,boolean isValidate){
        m_pages.add(page);
        if(isValidate){
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return m_pages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        View v = m_parent.getActivity().getLayoutInflater().inflate(R.layout.view_ppnovel_reader,null);
        updateView(v,position);
        m_views.add(v);
        container.addView(v);
        return v;
    }

    @Override
    public int getItemPosition(Object object) {
        if (m_bNeedUpdate) {
            m_bNeedUpdate = false;
            return POSITION_NONE;
        } else {
            return super.getItemPosition(object);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
        m_views.remove((View)object);
    }

    public PPNovelTextPage getItem(int pos){
        if(pos >= m_pages.size()){
            return null;
        }
        return  m_pages.get(pos);
    }

    public PPNovelTextPage getItemByChapter(String chapterUrl){
        for(PPNovelTextPage page: m_pages){
            if(page.chapter.compareTo(chapterUrl) == 0){
                return page;
            }
        }
        return null;
    }

    public int getItemPositionByChapter(String chapterUrl){
       for(int i = 0 ;i < m_pages.size(); i++){
           PPNovelTextPage page = m_pages.get(i);
           if(page.chapter.compareTo(chapterUrl) == 0){
               return i;
           }
       }
       return -1;
    }

    public void update(int pos){
        assert (pos < m_pages.size());
        PPNovelTextPage page = m_pages.get(pos);
        for(View v : m_views){
            int p = (Integer) v.getTag(R.id.tag_pos);
            if(p == pos){
                int status = (Integer)v.getTag(R.id.tag_status);
                String chapter = (String)v.getTag(R.id.tag_chapter);
                if(page.status != status || chapter.compareTo(page.chapter)!=0 ){
                    updateView(v,pos);
                }
                return;
            }
        }
    }



    private void updateView(View v, int position){
        final TextView tv = (TextView)v.findViewById(R.id.novel_reader_text);
        final PPNovelTextPage page = m_pages.get(position);
        if(page.status == PPNovelTextPage.STATUS_OK){
            loadText(page,tv,position);
            v.findViewById(R.id.novel_reader_loading).setVisibility(View.GONE);
            v.findViewById(R.id.novel_reader_err).setVisibility(View.GONE);
            v.findViewById(R.id.novel_reader_text).setVisibility(View.VISIBLE);
        }
        else if(page.status == PPNovelTextPage.STATUS_FAIL){
            v.findViewById(R.id.novel_reader_loading).setVisibility(View.GONE);
            v.findViewById(R.id.novel_reader_err).setVisibility(View.VISIBLE);
            v.findViewById(R.id.novel_reader_text).setVisibility(View.GONE);
        }
        else{
            v.findViewById(R.id.novel_reader_loading).setVisibility(View.VISIBLE);
            v.findViewById(R.id.novel_reader_err).setVisibility(View.GONE);
            v.findViewById(R.id.novel_reader_text).setVisibility(View.GONE);
        }
        v.setTag(R.id.tag_pos,position);
        v.setTag(R.id.tag_chapter,page.chapter);
        v.setTag(R.id.tag_status,page.status);
    }

    private void loadText(final PPNovelTextPage page,final TextView tv, final int pos){
        if(page.isSplited){
            if(page.offset == 0){
                setZeroOffsetPageText(tv,page.text);
            }
            else{
                tv.setText(page.text);
            }
        }
        else{
            final String text = half2full(page.text);
            tv.setText(text);
            tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    tv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    reallocateText(tv,text,page.title,pos);
                }
            });
        }
    }

    private void reallocateText(final TextView tv,final String text, final String title,final int pos ){
        String newText = title +  autoSplitText(tv,text);
        setZeroOffsetPageText(tv,newText);
        tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                splitText(tv,pos);
            }
        });
    }
    private void setZeroOffsetPageText(final TextView tv,final String text){
        SpannableString sp = new SpannableString(text);
        int end = text.indexOf('\n',1) ;
        float fontSize =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, tv.getResources().getDisplayMetrics());
        float padding =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, tv.getResources().getDisplayMetrics());
        sp.setSpan(new PPNovelTitleCenterBoldSpan(fontSize,padding),1,end,0);
        tv.setText(sp);
    }

    private void splitText(TextView tv,int pos){
        String text = tv.getText().toString();
        float height = tv.getHeight();
        int count = tv.getLineCount();
        float txtHeight = 0;
        float endLineBottomMargin = tv.getLineSpacingExtra();
        int offset  = 0;
        int begin = 0;
        Rect rc = new Rect();
        String chapter="";
        PPNovelTextPage first = null;
        for(int i = 0; i < count; i++){
            tv.getLineBounds(i,rc);
            txtHeight += rc.height();
            if(txtHeight >= height || i == count - 1){
                PPNovelTextPage page = null;
                if(begin == 0){
                    //the height of the title should be greater than the normal line, because the title font size is more bigger. But the system thinks they are the sane height, we should -1 ,
                    // otherwise, the text will be beyond the page
                    page = m_pages.get(pos);
                    first = page;
                    chapter = page.chapter;
                    i --;
                }
                else{
                    if(txtHeight - endLineBottomMargin > height ){
                        i--;
                    }
                    page = new PPNovelTextPage();
                    m_pages.add(pos+offset,page);
                }
                page.chapter = chapter;
                page.isSplited = true;
                page.offset = offset;
                page.status = PPNovelTextPage.STATUS_OK;
                int end = tv.getLayout().getLineEnd(i);
                page.text = text.substring(begin, end);
                if(offset!=0){
                    update(pos + offset);
                }
                offset++;
                if(i != count - 1){
                    begin = tv.getLayout().getLineStart(i + 1);
                    txtHeight = 0;
                }
            }
        }

        setZeroOffsetPageText(tv,first.text);
        m_bNeedUpdate = true;
        this.notifyDataSetChanged();
    }


    private Fragment m_parent;
    private ArrayList<PPNovelTextPage> m_pages = new ArrayList<PPNovelTextPage>();
    private boolean m_bNeedUpdate = false;
    private ClickPPNovelChapter m_callback = null;
    private ArrayList<View> m_views = new ArrayList<View>();

    static public class PPNovelTextPage{

        public final static int STATUS_OK = 0;
        public final static int STATUS_LOADING = 1;
        public final static int STATUS_FAIL = 2;
        public final static int STATUS_INIT = 3;

        public String text="";
        public int offset = 0;
        public boolean isSplited = false;
        public String title="";
        public String chapter="" ;
        public int status = STATUS_INIT;
    }

    public interface ClickPPNovelChapter{
      void onClick(String chapter);
    };
}
