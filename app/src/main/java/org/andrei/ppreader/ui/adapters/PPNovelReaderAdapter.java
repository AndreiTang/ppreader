package org.andrei.ppreader.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.ui.helper.PPNovelLineSpan;
import org.andrei.ppreader.ui.helper.PPNovelTitleCenterBoldSpan;
import org.andrei.ppreader.ui.helper.PPNovelReaderPageManager;
import org.andrei.ppreader.ui.helper.PPNovelTextPage;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


public class PPNovelReaderAdapter extends PagerAdapter {


    public PPNovelReaderAdapter(@NonNull Fragment parent, @NonNull PPNovelReaderPageManager pageMgr) {
        m_parent = parent;
        m_pageMgr = pageMgr;
    }


    @Override
    public int getCount() {
        return m_pageMgr.getPages().size();
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
                PPNovelTextPage page = m_pageMgr.getItem(chapter,0);//getFirstItemByChapter(chapter);
                page.status = PPNovelTextPage.STATUS_LOADING;
                update(position);
                m_pageMgr.fetchChapterText(page);
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

    public void update(int pos) {
        PPNovelTextPage page = m_pageMgr.getItem(pos);
        if(page == null){
            return;
        }
        for (View v : m_views) {
            int p = (Integer) v.getTag(R.id.tag_pos);
            if (p == pos) {
                int status = (Integer) v.getTag(R.id.tag_status);
                String chapter = (String) v.getTag(R.id.tag_chapter);
                boolean isSplit = (Boolean) v.getTag(R.id.tag_issplit);
                if (page.status != status || chapter.compareTo(page.chapter) != 0 || isSplit!=page.isSplit) {
                    updateView(v, pos);
                }
                return;
            }
        }
    }

    private void updateView(View v, int position) {
        final TextView tv = (TextView) v.findViewById(R.id.novel_reader_text);
        final PPNovelTextPage page = m_pageMgr.getItem(position);
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
        v.setTag(R.id.tag_issplit, page.isSplit);
    }

    private void loadText(final PPNovelTextPage page, final TextView tv, final int pos) {
        if (page.isSplit) {
           loadPage(page,tv);
        } else {
            m_pageMgr.divideChapterToPages(tv,pos).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer s) throws Exception {
                    validate(tv,pos,s);
                }
            });
        }
    }

    private void validate(final TextView tv, final int curPos,final int offset){
        PPNovelTextPage tp = m_pageMgr.getItem(curPos);
        int beginPos = m_pageMgr.getFirstChapterItemPosition(tp.chapter);
        for(int i = beginPos ; i <= beginPos+offset; i++){
            if(i == curPos){
                loadPage(tp,tv);
            }
            else{
                update(i);
            }
        }
        m_bNeedUpdate = true;
        notifyDataSetChanged();
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

    private SpannableStringBuilder getPageText(PPNovelTextPage page, final TextView tv) {
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
    private boolean m_bNeedUpdate = false;
    private ArrayList<View> m_views = new ArrayList<View>();
    final private PPNovelReaderPageManager m_pageMgr;;
}
