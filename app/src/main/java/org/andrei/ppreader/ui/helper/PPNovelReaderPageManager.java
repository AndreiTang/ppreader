package org.andrei.ppreader.ui.helper;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelError;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlNovelThrowable;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;


import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class PPNovelReaderPageManager {


    public PPNovelReaderPageManager(@NonNull final PPNovel novel, int tvHeight) {
        m_tvHeight = tvHeight;
        m_novel = novel;
        m_crawlNovel = CrawlNovelService.instance().builder(m_novel.engineName);
        initializePages(novel);
    }

    public PPNovelTextPage getItem(int pos) {
        if (pos >= m_pages.size()) {
            return null;
        }
        return m_pages.get(pos);
    }

    public int getFirstChapterItemPosition(final String chapter) {
        for(int i = 0; i < m_pages.size(); i++){
            PPNovelTextPage page = m_pages.get(i);
            if(page.chapter.compareTo(chapter) == 0){
                return i;
            }
        }
        return -1;
    }

    public PPNovelTextPage getItem(final String chapter, final int offset) {

        for (PPNovelTextPage page : m_pages) {
            if (page.chapter.compareTo(chapter) == 0 && page.offset == offset) {
                return page;
            }
        }
        return null;
    }

    public ArrayList<PPNovelTextPage> getPages() {
        return m_pages;
    }

    public void fetchChapterText(@NonNull final PPNovelTextPage page) {
        PPNovelChapter chapter = m_novel.getPPNovelChapter(page.chapter);
        if (chapter == null) {
            return;
        }
        m_fetchList.add(chapter);
    }

    public Observable<Integer> fetchChapterTextObserve(){
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                try{
                    while (true) {
                        if(m_fetchList.size() == 0){
                            Thread.sleep(100);
                            continue;
                        }
                        PPNovelChapter chapter = m_fetchList.remove(0);
                        CrawlTextResult crawlTextResult = new CrawlTextResult();
                        int ret = m_crawlNovel.fetchNovelText(m_novel.chapterUrl, chapter.url,crawlTextResult);
                        PPNovelTextPage page = getItem(crawlTextResult.chapterUrl, 0);
                        int index = getFirstChapterItemPosition(crawlTextResult.chapterUrl);
                        if(ret == CrawlNovelError.ERR_NONE){
                            chapter.text = crawlTextResult.text;
                            page.text = crawlTextResult.text;
                            page.status = PPNovelTextPage.STATUS_LOADED;

                        }
                        else{
                            page.status = PPNovelTextPage.STATUS_FAIL;
                        }
                        e.onNext(index);
                    }
                }
                catch (Exception ex){
                }

            }
        }).subscribeOn(Schedulers.io());
    }

    public int splitChapter(@NonNull final TextView tv, final PPNovelTextPage page) {
        int pageTextHeight = 0;
        int offset = 0;
        ArrayList<PPNovelTextPage> pages = new ArrayList<PPNovelTextPage>();
        final String text = tv.getText().toString();
        int lineHeight = tv.getLineHeight();
        int lineCount = tv.getLineCount();

        int beginPos = getFirstChapterItemPosition(page.chapter);
        PPNovelTextPage firstPage = m_pages.get(beginPos);
        PPNovelTextPage item = firstPage;

        for (int i = 0; i < lineCount; i++) {
            int begin = tv.getLayout().getLineStart(i);
            int end = tv.getLayout().getLineEnd(i);
            String lineText = text.substring(begin, end);
            pageTextHeight += lineHeight;

            if (pageTextHeight < m_tvHeight) {
                item.lines.add(lineText);
            } else {
                i--;
                //the the font size of title is bigger than lines in body. So the line size in body decrease 1
                if (offset == 0) {
                    i--;
                }

                if (i != lineCount - 1) {
                    offset++;
                    item = getItem(firstPage.chapter, offset);
                    if (item == null) {
                        item = new PPNovelTextPage();
                        item.offset = offset;
                        pages.add(item);
                    }
                    pageTextHeight = 0;
                }
            }
        }

        for (PPNovelTextPage pp : pages) {
            m_pages.add(beginPos + pp.offset, pp);
        }

        for (int i = beginPos; i <= beginPos + offset; i++) {
            PPNovelTextPage pp = m_pages.get(i);
            pp.isSplit = true;
            pp.status = PPNovelTextPage.STATUS_OK;
            if (i == beginPos) {
                //remove dummy title
                pp.lines.remove(0);
                pp.lines.remove(0);
                pp.lines.remove(0);
                //remove the decreased line, due to the title size.
                if (offset > 0) {
                    pp.lines.remove(pp.lines.size() - 1);
                }
                pp.gravity = Gravity.BOTTOM;
            } else if (i == beginPos + offset) {
                pp.chapter = firstPage.chapter;
                pp.gravity = Gravity.TOP;

                //add '\n' at end of the last line of last page in each chapter.
                //it mean this line doesn't change the letter space.
                String lastLine = pp.lines.get(pp.lines.size() - 1);
                if (lastLine.indexOf('\n') == -1) {
                    lastLine += "\n";
                    pp.lines.set(pp.lines.size() - 1, lastLine);
                }
            } else {
                pp.chapter = firstPage.chapter;
                pp.gravity = Gravity.CENTER_VERTICAL;
            }
        }

        return offset;
    }


    private void initializePages(final PPNovel novel) {
        for (int i = 0; i < novel.chapters.size(); i++) {
            PPNovelChapter chapter = novel.chapters.get(i);
            PPNovelTextPage page = new PPNovelTextPage();
            page.chapter = chapter.url;
            page.title = chapter.name;
            page.text = chapter.text;
            if (page.text.length() > 0) {
                page.status = PPNovelTextPage.STATUS_LOADED;
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
                    pp.status = PPNovelTextPage.STATUS_LOADED;
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
        if (novel.currentChapterIndex == 0 && novel.currentChapterOffset == 0) {
            PPNovelTextPage pp = m_pages.get(0);
            if(pp.text.length() >0) {
                pp.status = PPNovelTextPage.STATUS_OK;
            }
            else{
                pp.status = PPNovelTextPage.STATUS_LOADING;
                fetchChapterText(pp);
            }
        }
    }

    private ArrayList<PPNovelTextPage> m_pages = new ArrayList<PPNovelTextPage>();
    private int m_tvHeight = 0;
    private CrawlNovel m_crawlNovel = null;
    private PPNovel m_novel;
    private ArrayList<PPNovelChapter> m_fetchList = new ArrayList<PPNovelChapter>();
}
