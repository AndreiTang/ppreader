package org.andrei.ppreader.ui.helper;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlNovelThrowable;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter;

import java.util.ArrayList;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static org.andrei.ppreader.ui.adapters.PPNovelReaderAdapter.PPNovelTextPage.STATUS_OK;

public class PPNovelReaderPageManager {

    public PPNovelReaderPageManager(@NonNull final PPNovel novel, int tvHeight){
        initializePages(novel);
        m_tvHeight = tvHeight;
        m_novel = novel;
    }

    public PPNovelTextPage getItem(int pos){
        if(pos >= m_pages.size()){
            return null;
        }
        return m_pages.get(pos);
    }

    public int getFirstChapterItemPosition(final String chapter){
        return -1;
    }

    public PPNovelTextPage getItem(final String chapter, final int offset){
        return null;
    }

    public ArrayList<PPNovelTextPage> getPages(){
        return m_pages;
    }

    public Observable<Integer> getPPNovelTextPageObservable(){
        return m_textPageObservable;
    }

    public Observable<String> divideChapterToPages(@NonNull final TextView tv, final int pos){

        return Observable.create(new ObservableOnSubscribe<String>(){
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                final PPNovelTextPage page = m_pages.get(pos);
                final String body = adjustParagraph(page.text);
                final StringBuilder text =  new StringBuilder();

                text.append("J\n");
                //using dummy title to occupy title place which is just one line.
                // If the real title is length than the width of textview. it will occupy more than 1 line which will cause error.
                text.append("This is dummy\n");
                text.append("J\n");
                text.append(body);
                tv.setText(text);

                final ObservableEmitter<String> emit = e;
                tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        splitChapter(tv,page);
                        emit.onNext(page.chapter);
                    }
                });
            }
        });
    }
    public void fetchChapterText(@NonNull final PPNovelTextPage page) {
        PPNovelChapter chapter = m_novel.getPPNovelChapter(page.chapter);
        if(chapter == null){
            return;
        }
        m_fetchList.add(chapter);
        if (!m_bRunning) {
            fetchChapterTextProc();
        }
    }

    private void fetchChapterTextProc() {
        if (m_crawlNovel == null) {
            m_crawlNovel = CrawlNovelService.instance().builder(m_novel.engineIndex);
        }
        if (m_fetchList.size() == 0) {
            return;
        }
        m_bRunning = true;
        PPNovelChapter chapter = m_fetchList.remove(0);
        m_crawlNovel.fetchNovelText(m_novel.chapterUrl, chapter.url).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<CrawlTextResult>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(CrawlTextResult value) {
                PPNovelChapter item = m_novel.getPPNovelChapter(value.chapterUrl);
                item.text = value.text;
                PPNovelTextPage page = getItem(value.chapterUrl,0);
                assert (page != null);
                page.text = value.text;
                page.status = PPNovelTextPage.STATUS_LOADED;
                int index = getFirstChapterItemPosition(value.chapterUrl);
                if(m_textPageObservable.m_observer != null){
                    m_textPageObservable.m_observer.onNext(index);
                }
                //if this text is not in the current page, don't immedially set value. Otherwise, the page will incorrectly be shown.For example , 前面的页一刷新，会把现在的页面刷掉
            }

            @Override
            public void onError(Throwable e) {
                fetchChapterTextProc();
                if (m_fetchList.size() == 0) {
                    m_bRunning = false;
                }

                CrawlNovelThrowable err = (CrawlNovelThrowable) e;
                PPNovelTextPage page = getItem(err.chapterUrl,0);
                assert (page != null);
                page.status = PPNovelTextPage.STATUS_FAIL;
                int index = getFirstChapterItemPosition(err.chapterUrl);
                if(m_textPageObservable.m_observer != null){
                    m_textPageObservable.m_observer.onNext(index);
                }
            }

            @Override
            public void onComplete() {
                fetchChapterTextProc();
                if (m_fetchList.size() == 0) {
                    m_bRunning = false;
                }
            }
        });
    }

    private void splitChapter(@NonNull final TextView tv,final PPNovelTextPage page){
        int pageTextHeight = 0;
        int offset = 0;
        ArrayList<PPNovelTextPage> pages = new ArrayList<PPNovelTextPage>();
        final String text = tv.getText().toString();
        int lineHeight = tv.getLineHeight();
        int lineCount = tv.getLineCount();
        float lineSpace = tv.getLineSpacingExtra();

        int beginPos = getFirstChapterItemPosition(page.chapter);
        PPNovelTextPage firstPage =  m_pages.get(beginPos);
        PPNovelTextPage item = firstPage;

        for (int i = 0; i < lineCount; i++) {
            int begin = tv.getLayout().getLineStart(i);
            int end = tv.getLayout().getLineEnd(i);
            String lineText = text.substring(begin, end);
            pageTextHeight +=lineHeight;

            if (pageTextHeight <m_tvHeight) {
                item.lines.add(lineText);
            }
            else {
                if(pageTextHeight - lineSpace <= m_tvHeight){
                    item.lines.add(lineText);
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
                    item = getItem(firstPage.chapter,offset);
                    if(item == null){
                        item = new PPNovelTextPage();
                        item.offset = offset;
                        pages.add(item);
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
            pp.isSplit = true;
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
    }

    private void initializePages(final PPNovel novel){
        for (int i = 0; i < novel.chapters.size(); i++) {
            PPNovelChapter chapter = novel.chapters.get(i);
            PPNovelTextPage page = new PPNovelTextPage();
            page.chapter = chapter.url;
            page.title = chapter.name;
            page.text = chapter.text;
            if (page.text.length() > 0) {
                page.status = PPNovelTextPage.STATUS_LOADING;
            } else {
                page.status = PPNovelTextPage.STATUS_INIT;
            }
            m_pages.add(page);
            if (i == novel.currentChapterIndex && page.text.length() > 0 && novel.currentChapterOffset > 0) {
                for (int j = 1; j <= novel.currentChapterOffset; j++) {
                    PPNovelTextPage pp = new PPNovelTextPage();
                    pp.offset = j;
                    pp.chapter = page.chapter;
                    pp.isSplit = false;
                    pp.status = PPNovelTextPage.STATUS_LOADING;
                    if (j == novel.currentChapterOffset) {
                        pp.text = page.text;
                    }
                    m_pages.add(pp);
                }
            }
        }

        //if the current index is beyond the array, reset all to be 0.
        if (novel.currentChapterIndex >= novel.chapters.size()) {
            novel.currentChapterIndex = 0;
            novel.currentChapterOffset = 0;
        } else {
            PPNovelChapter chapter = novel.chapters.get(novel.currentChapterIndex);
            //it mean this text isn't downloaded. its offset must be 0.
            if (chapter.text.length() == 0) {
                novel.currentChapterOffset = 0;
            }
        }
        if(novel.currentChapterIndex == 0 && novel.currentChapterOffset == 0){
            PPNovelTextPage pp = m_pages.get(0);
            pp.status = PPNovelTextPage.STATUS_OK;
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

    private class PPNovelTextPageObservable extends Observable<Integer> {

        public Observer<? super Integer> m_observer = null;
        @Override
        protected void subscribeActual(Observer<? super Integer> observer) {
            m_observer = observer;
        }
    }
    private PPNovelTextPageObservable m_textPageObservable = new PPNovelTextPageObservable();
    private ArrayList<PPNovelTextPage> m_pages = new ArrayList<PPNovelTextPage>();
    private int m_tvHeight = 0;
    private CrawlNovel m_crawlNovel = null;
    private PPNovel m_novel;
    private ArrayList<PPNovelChapter> m_fetchList = new ArrayList<PPNovelChapter>();
    private boolean m_bRunning = false;

}
