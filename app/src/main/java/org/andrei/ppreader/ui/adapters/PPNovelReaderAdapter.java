package org.andrei.ppreader.ui.adapters;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.PPNovelLineSpan;
import org.andrei.ppreader.ui.PPNovelTitleCenterBoldSpan;
import org.andrei.ppreader.ui.Utils;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import io.reactivex.functions.Consumer;
import static org.andrei.ppreader.ui.Utils.half2full;
import static org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter.PPNovelTextPage.STATUS_LOADING;
import static org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter.PPNovelTextPage.STATUS_OK;

public class PPNovelReaderAdapter extends PagerAdapter {


    public PPNovelReaderAdapter(Fragment parent, ClickPPNovelChapter callback) {
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
                PPNovelTextPage page = getItemByChapter(chapter);
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

    public PPNovelTextPage getItemByChapter(String chapterUrl) {
        for (PPNovelTextPage page : m_pages) {
            if (page.chapter.compareTo(chapterUrl) == 0) {
                return page;
            }
        }
        return null;
    }

    public int getItemPositionByChapter(String chapterUrl) {
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
            if (page.offset == 0) {
                setFirstPageOfChapter(tv, page);
            } else {
                setNormalPageOfChapter(tv, page);
            }
        } else {
            justifyText(page, tv, pos);
        }

    }


    ///////////////////////////////////

    String adjustParagraph(final String text) {
        StringBuilder newText = new StringBuilder();
        String paragraphs[] = half2full(text).replaceAll("\r", "").split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.length() == 0) {
                continue;
            }
            char space = 12288;
            newText.append(space);
            newText.append(space);
            paragraph = paragraph.replaceAll("\\s*", "");
            newText.append(paragraph);
            newText.append("\n");
        }
        //remove the \n at the end
        newText.deleteCharAt(newText.length() - 1);
        return newText.toString();
    }

    private void justifyText(final PPNovelTextPage page, final TextView tv, final int pos) {

        final StringBuilder text =  new StringBuilder();
        text.append('\n');
        //using dummy title to occupy title place which is just one line.
        // If the real title is length than the width of textview. it will occupy more than 1 line which will cause error.
        text.append("this is dummy");
        text.append("\n\n");
        text.append(adjustParagraph(page.text));
        tv.setText(text);
        tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                splitText2Pages(tv, pos);
            }
        });
    }

    private void splitText2Pages(final TextView tv, final int pos) {
        final String text = tv.getText().toString();
        int pageTextHeight = 0;
        Rect rc = new Rect();
        ArrayList<String> lines = new ArrayList<String>();
        PPNovelTextPage page = m_pages.get(pos);
        PPNovelTextPage firstPage = page;
        int offset = 0;
        for (int i = 0; i < tv.getLineCount(); i++) {
            tv.getLineBounds(i, rc);
            pageTextHeight += rc.height();
            int begin = tv.getLayout().getLineStart(i);
            int end = tv.getLayout().getLineEnd(i);
            String lineText = text.substring(begin, end);
            if (i == tv.getLineCount() - 1) {
                if (lineText.indexOf('\n') == -1) {
                    lineText += '\n';
                }
                lines.add(lineText);
                page.lines = lines;
                page.isSplited = true;
                page.offset = offset;
                page.status = STATUS_OK;
                page.chapter = firstPage.chapter;
                m_pages.add(pos + offset, page);
            } else if (pageTextHeight < tv.getHeight()) {
                lines.add(lineText);

            } else if (pageTextHeight >= tv.getHeight()) {
                if (offset == 0) {
                    i--;
                }
                else if ((pageTextHeight - tv.getLineSpacingExtra()) <= tv.getHeight()) {
                    lines.add(lineText);

                } else {
                    i--;
                }

                page.lines = lines;
                page.isSplited = true;
                page.offset = offset;
                page.status = STATUS_OK;

                if (offset > 0) {
                    page.chapter = firstPage.chapter;
                    m_pages.add(pos + offset, page);
                    update(pos + offset);
                }
                else{
                    page.lines.remove(0);
                    page.lines.remove(0);
                    page.lines.remove(0);
                }
                offset++;
                page = new PPNovelTextPage();
                lines = new ArrayList<String>();
                pageTextHeight = 0;
            }
        }
        setFirstPageOfChapter(tv, firstPage);
        m_bNeedUpdate = true;
        this.notifyDataSetChanged();
    }

    private void setFirstPageOfChapter(final TextView tv, final PPNovelTextPage page) {
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
    }

    private void setNormalPageOfChapter(final TextView tv, final PPNovelTextPage page) {
        SpannableStringBuilder body = getPageText(page,tv);
        tv.setText(body);
    }

    private SpannableStringBuilder getPageText(PPNovelTextPage page,final TextView tv) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 17, tv.getResources().getDisplayMetrics());
        float left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, tv.getResources().getDisplayMetrics());
        float right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, tv.getResources().getDisplayMetrics());
        for (String str : page.lines) {
            if (str.indexOf('\n') == -1) {
                SpannableStringBuilder item = new SpannableStringBuilder(str);
                item.setSpan(new PPNovelLineSpan(fontSize,left,right),0,str.length(),0);
                sb.append(item);
                sb.append("\n");
            } else {
                sb.append(str);
            }
        }
        return sb;
    }

    private Fragment m_parent;
    private ArrayList<PPNovelTextPage> m_pages = new ArrayList<PPNovelTextPage>();
    private boolean m_bNeedUpdate = false;
    private ClickPPNovelChapter m_callback = null;
    private ArrayList<View> m_views = new ArrayList<View>();

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
    }

    public interface ClickPPNovelChapter {
        void onClick(String chapter);
    }
    ;
}
