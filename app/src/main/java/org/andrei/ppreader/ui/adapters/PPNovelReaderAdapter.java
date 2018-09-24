package org.andrei.ppreader.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.ui.PPNovelLineSpan;
import org.andrei.ppreader.ui.PPNovelTitleCenterBoldSpan;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

import static org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter.PPNovelTextPage.STATUS_LOADING;
import static org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter.PPNovelTextPage.STATUS_OK;

public class PPNovelReaderAdapter extends PagerAdapter {


    public PPNovelReaderAdapter(@NonNull Fragment parent, @NonNull ClickPPNovelChapter callback) {
        m_parent = parent;
        m_callback = callback;
    }

    public void addPage(PPNovelTextPage page, boolean isValidate) {
        m_pages.add(page);
        if (isValidate) {
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
    public Object instantiateItem(ViewGroup container, final int position) {
        final View v = m_parent.getActivity().getLayoutInflater().inflate(R.layout.view_ppnovel_reader, null);
        updateView(v, position);
        m_views.add(v);
        container.addView(v);
        RxView.clicks(v.findViewById(R.id.novel_reader_err)).throttleFirst(300, TimeUnit.MILLISECONDS).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                String chapter = (String) v.getTag(R.id.tag_chapter);
                PPNovelTextPage page = getFirstItemByChapter(chapter);
                page.status = STATUS_LOADING;
                update(position);
                if (m_callback != null) {
                    m_callback.onClick(chapter);
                }
            }
        });

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
        container.removeView((View) object);
        m_views.remove((View) object);
    }

    public PPNovelTextPage getItem(int pos) {
        if (pos >= m_pages.size()) {
            return null;
        }
        return m_pages.get(pos);
    }

    public PPNovelTextPage getFirstItemByChapter(String chapterUrl) {
        for (PPNovelTextPage page : m_pages) {
            if (page.chapter.compareTo(chapterUrl) == 0) {
                return page;
            }
        }
        return null;
    }

    public int getFirstItemPositionByChapter(String chapterUrl) {
        for (int i = 0; i < m_pages.size(); i++) {
            PPNovelTextPage page = m_pages.get(i);
            if (page.chapter.compareTo(chapterUrl) == 0) {
                return i;
            }
        }
        return -1;
    }

    public void update(int pos) {
        assert (pos < m_pages.size());
        PPNovelTextPage page = m_pages.get(pos);
        for (View v : m_views) {
            int p = (Integer) v.getTag(R.id.tag_pos);
            if (p == pos) {
                int status = (Integer) v.getTag(R.id.tag_status);
                String chapter = (String) v.getTag(R.id.tag_chapter);
                if (page.status != status || chapter.compareTo(page.chapter) != 0) {
                    updateView(v, pos);
                }
                return;
            }
        }
    }

    public void setTextViewHeight(int tvHeight){
        m_tvHeight = tvHeight;
    }


    private void updateView(View v, int position) {
        final TextView tv = (TextView) v.findViewById(R.id.novel_reader_text);
        final PPNovelTextPage page = m_pages.get(position);
        if (page.status == PPNovelTextPage.STATUS_OK) {
            loadText(page, tv, position);
            v.findViewById(R.id.novel_reader_loading).setVisibility(View.GONE);
            v.findViewById(R.id.novel_reader_err).setVisibility(View.GONE);
            v.findViewById(R.id.novel_reader_text).setVisibility(View.VISIBLE);
        } else if (page.status == PPNovelTextPage.STATUS_FAIL) {
            v.findViewById(R.id.novel_reader_loading).setVisibility(View.GONE);
            v.findViewById(R.id.novel_reader_err).setVisibility(View.VISIBLE);
            v.findViewById(R.id.novel_reader_text).setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.novel_reader_loading).setVisibility(View.VISIBLE);
            v.findViewById(R.id.novel_reader_err).setVisibility(View.GONE);
            v.findViewById(R.id.novel_reader_text).setVisibility(View.GONE);
        }
        v.setTag(R.id.tag_pos, position);
        v.setTag(R.id.tag_chapter, page.chapter);
        v.setTag(R.id.tag_status, page.status);
    }

    private void loadText(final PPNovelTextPage page, final TextView tv, final int pos) {
        if (page.isSplited) {
           loadPage(page,tv);
        } else {
            segmentationText(page, tv, pos);
        }
    }

    private String adjustParagraph(final String text) {
        StringBuilder newText = new StringBuilder();
        String paragraphs[] = text.replaceAll("\r", "").split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.length() == 0) {
                continue;
            }
            //there are two space at the beginning of each paragraph.
            char space = 12288;
            newText.append(space);
            newText.append(space);

            //Except the beginning, all the space are removed
            paragraph = paragraph.replaceAll("\\s*", "");
            newText.append(paragraph);

            //there is a '\n' at the end of each line
            newText.append("\n");
        }
        //remove the '\n' at the end. Or textview will a new empty.
        newText.deleteCharAt(newText.length() - 1);
        return newText.toString();
    }

    private void segmentationText(final PPNovelTextPage page, final TextView tv, final int pos) {

        final StringBuilder text =  new StringBuilder();
        text.append("J\n");
        //using dummy title to occupy title place which is just one line.
        // If the real title is length than the width of textview. it will occupy more than 1 line which will cause error.
        text.append("This is dummy\n");
        text.append("J\n");
        text.append(adjustParagraph(page.text));
        tv.setText(text);

        tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                splitText(tv, pos);
            }
        });
    }

    private void splitText(final TextView tv, final int pos) {
        int pageTextHeight = 0;
        int offset = 0;
        ArrayList<PPNovelTextPage> pages = new ArrayList<PPNovelTextPage>();
        final String text = tv.getText().toString();
        int lineHeight = tv.getLineHeight();
        int lineCount = tv.getLineCount();
        float lineSpace = tv.getLineSpacingExtra();

        PPNovelTextPage page = m_pages.get(pos);
        int beginPos = getFirstItemPositionByChapter(page.chapter);
        page = m_pages.get(beginPos);
        PPNovelTextPage firstPage = page;

        for (int i = 0; i < lineCount; i++) {
            int begin = tv.getLayout().getLineStart(i);
            int end = tv.getLayout().getLineEnd(i);
            String lineText = text.substring(begin, end);
            pageTextHeight +=lineHeight;

            if (pageTextHeight <m_tvHeight) {
                page.lines.add(lineText);
            }
            else {
                if(pageTextHeight - lineSpace <= m_tvHeight){
                    page.lines.add(lineText);
                }
                else{
                    i--;
                }

                //the the font size of title is bigger than lines in body. So the line size in body decrease 1
                if (offset == 0) {
                    i--;
                }

                if(i != lineCount - 1){
                    offset++;
                    page = getPage(firstPage.chapter,offset);
                    if(page == null){
                        page = new PPNovelTextPage();
                        page.offset = offset;
                        pages.add(page);
                    }
                    pageTextHeight = 0;
                }
            }
        }

        for(PPNovelTextPage pp  : pages){
            m_pages.add(beginPos+pp.offset,pp);
        }

        for(int i = beginPos ; i <= beginPos+offset; i++){
            PPNovelTextPage pp = m_pages.get(i);
            pp.isSplited = true;
            pp.status = STATUS_OK;
            if(i == beginPos){
                //remove dummy title
                pp.lines.remove(0);
                pp.lines.remove(0);
                pp.lines.remove(0);
                //remove the decreased line, due to the title size.
                if(offset > 0){
                    pp.lines.remove(pp.lines.size()-1);
                }
                pp.gravity = Gravity.BOTTOM;
            }
            else if(i == beginPos + offset){
                pp.chapter = firstPage.chapter;
                pp.gravity = Gravity.TOP;

                //add '\n' at end of the last line of last page in each chapter.
                //it mean this line doesn't change the letter space.
                String lastLine = pp.lines.get(pp.lines.size()-1);
                if(lastLine.indexOf('\n')==-1){
                    lastLine += "\n";
                    pp.lines.set(pp.lines.size()-1,lastLine);
                }
            }
            else{
                pp.chapter = firstPage.chapter;
                pp.gravity = Gravity.CENTER_VERTICAL;
            }
        }

        //validate the ui
        for(int i = beginPos ; i <= beginPos+offset; i++){
            if(pos == i){
                loadPage(m_pages.get(i),tv);
            }
            else{
                update(i);
            }
        }
        m_bNeedUpdate = true;
        notifyDataSetChanged();
    }

    private PPNovelTextPage getPage(final String chapter, final int offset){
        for(PPNovelTextPage page : m_pages){
            if(page.chapter.compareTo(chapter) == 0 && page.offset == offset){
                return page;
            }
        }
        return null;
    }

    private void loadPage(final PPNovelTextPage page, final TextView tv){
        if (page.offset == 0) {
            loadFirstPage(tv, page);
        } else {
            loadNormalPage(tv, page);
        }
    }

    private void loadFirstPage(final TextView tv, final PPNovelTextPage page) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        text.append('\n');
        float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, tv.getResources().getDisplayMetrics());
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, tv.getResources().getDisplayMetrics());
        SpannableString title = new SpannableString(page.title);
        title.setSpan(new PPNovelTitleCenterBoldSpan(fontSize, padding), 0, page.title.length(), 0);
        text.append(title);
        text.append('\n');
        text.append('\n');
        SpannableStringBuilder body = getPageText(page,tv);
        text.append(body);
        tv.setText(text);
        tv.setGravity(page.gravity);
    }

    private void loadNormalPage(final TextView tv, final PPNovelTextPage page) {
        SpannableStringBuilder body = getPageText(page,tv);
        tv.setText(body);
        tv.setGravity(page.gravity);
    }

    private SpannableStringBuilder getPageText(PPNovelTextPage page,final TextView tv) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 17, tv.getResources().getDisplayMetrics());
        float left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, tv.getResources().getDisplayMetrics());
        float right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, tv.getResources().getDisplayMetrics());
        for (int i = 0; i < page.lines.size() ; i++) {
            String str = page.lines.get(i);
            if (str.indexOf('\n') == -1) {
                SpannableStringBuilder item = new SpannableStringBuilder(str);
                item.setSpan(new PPNovelLineSpan(fontSize,left,right),0,str.length(),0);
                sb.append(item);
                sb.append("\n");
            } else {
                sb.append(str);
            }
        }
        //the last line can't be \n  at the end. otherwise, it will add a new empty line.
        if(sb.charAt(sb.length() - 1) == '\n'){
            sb.delete(sb.length() - 1,sb.length());
        }
        return sb;
    }

    private Fragment m_parent;
    private ArrayList<PPNovelTextPage> m_pages = new ArrayList<PPNovelTextPage>();
    private boolean m_bNeedUpdate = false;
    private ClickPPNovelChapter m_callback = null;
    private ArrayList<View> m_views = new ArrayList<View>();
    private int m_tvHeight = 0;

    static public class PPNovelTextPage {

        public final static int STATUS_OK = 0;
        public final static int STATUS_LOADING = 1;
        public final static int STATUS_FAIL = 2;
        public final static int STATUS_INIT = 3;


        public String text = "";
        public int offset = 0;
        public boolean isSplited = false;
        public String title = "";
        public String chapter = "";
        public int status = STATUS_INIT;
        public ArrayList<String> lines = new ArrayList<String>();
        int gravity = Gravity.BOTTOM ;
    }

    public interface ClickPPNovelChapter {
        void onClick(String chapter);
    }
    ;
}
