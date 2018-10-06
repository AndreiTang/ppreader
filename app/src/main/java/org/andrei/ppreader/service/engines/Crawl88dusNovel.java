package org.andrei.ppreader.service.engines;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlNovelThrowable;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.ICrawlNovel;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class Crawl88dusNovel implements ICrawlNovel {
    @Override
    public Observable<PPNovel> search(final String name) {
        return Observable.create(new ObservableOnSubscribe<PPNovel>() {
            @Override
            public void subscribe(ObservableEmitter<PPNovel> e) throws Exception {
                try {
                    String url = "https://so.88dus.com/search/so.php?search_field=0&q=" + name;
                    Document doc = Jsoup.connect(url).timeout(6000).get();
                    Elements els = doc.getElementsByClass("ops_cover");
                    if (els.size() == 0) {
                        Throwable err = new Throwable(new Integer(R.string.err_not_found).toString());
                        e.onError(err);
                        return;
                    }
                    Elements items = els.get(0).getElementsByClass("block");
                    if (items.size() == 0) {
                        Throwable err = new Throwable(new Integer(R.string.err_not_found).toString());
                        e.onError(err);
                        return;
                    }

                    boolean isFetched = false;
                    for (Element item : items) {
                        PPNovel novel = new PPNovel();
                        if (fetchNovel(item, novel)) {
                            isFetched = true;
                            e.onNext(novel);
                        }
                    }
                    if (!isFetched) {
                        Throwable err = new Throwable(new Integer(R.string.err_not_found).toString());
                        e.onError(err);
                    }
                    else{
                        e.onComplete();
                    }
                } catch (Exception exception) {
                    if (!e.isDisposed()) {
                        Integer i = R.string.err_network;
                        Throwable err = new Throwable(i.toString());
                        e.onError(err);
                    }
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<CrawlChapterResult> fetchChapters(final PPNovel novel) {
        return Observable.create(new ObservableOnSubscribe<CrawlChapterResult>() {
            @Override
            public void subscribe(ObservableEmitter<CrawlChapterResult> e) throws Exception {
                CrawlChapterResult ret = new CrawlChapterResult();
                ret.chapterUrl = novel.chapterUrl;
                boolean bRet = fetchChaptersInner(novel.chapterUrl,ret.chapters,null);
                if(bRet){
                    e.onNext(ret);
                    e.onComplete();
                }
                else{
                    Throwable err = new Throwable();
                    e.onError(err);

                }
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<CrawlTextResult> fetchNovelText(final String novelId, final String chapterUrl) {
        return Observable.create(new ObservableOnSubscribe<CrawlTextResult>() {
            @Override
            public void subscribe(ObservableEmitter<CrawlTextResult> e) throws Exception {
                try{
                    Document doc = Jsoup.connect(chapterUrl).timeout(6000).get();
                    Element item = doc.getElementsByClass("yd_text2").get(0);
                    String text = item.text();
                    if(text.isEmpty()){
                        CrawlNovelThrowable err = new CrawlNovelThrowable();
                        err.novelUrl = novelId;
                        err.chapterUrl = chapterUrl;
                        e.onError(err);
                        return;
                    }
                    text = text.replaceAll("&nbsp;","");
                    text = text.replaceAll("<br /><br />","\n");

                    CrawlTextResult ret = new CrawlTextResult();
                    ret.chapterUrl = chapterUrl;
                    ret.novelUrl = novelId;
                    ret.text = text;
                    e.onNext(ret);
                    e.onComplete();
                }
                catch (Exception ex){
                    if(!e.isDisposed()){
                        CrawlNovelThrowable err = new CrawlNovelThrowable();
                        err.novelUrl = novelId;
                        err.chapterUrl = chapterUrl;
                        e.onError(err);
                    }
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private boolean fetchNovel(final Element element, final PPNovel novel) {
        try {
            Element item = element.getElementsByTag("a").get(0);
            novel.chapterUrl = item.attr("href");
            item = element.getElementsByTag("img").get(0);
            novel.imgUrl = item.attr("src");
            novel.name = item.attr("alt");
            Elements pps = element.getElementsByTag("p");
            item = pps.get(2);
            novel.author = item.text();
            novel.author = novel.author.substring(3);
            item = pps.get(4);
            novel.desc = item.text();
            novel.desc = novel.desc.trim();
            novel.desc = novel.desc.replaceAll("\n", "");
            Integer type = new Integer(0);
            if(fetchChaptersInner(novel.chapterUrl,novel.chapters,type)){
                novel.type = type;
            }
            else{
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private boolean fetchChaptersInner(final String url, final ArrayList<PPNovelChapter> chapters, Integer type) {
        try {
            Document doc = Jsoup.connect(url).timeout(6000).get();
            Element item = doc.getElementsByTag("em").get(1);

            if(type != null){
                if(item.text().indexOf("连载") != -1){
                    type = PPNovel.TYPE_ING;
                }
                else{
                    type = PPNovel.TYPE_OVER;
                }
            }

            Element root = doc.getElementsByClass("mulu").get(0);
            Elements cs = root.getElementsByTag("a");
            for(Element c :cs){
                PPNovelChapter chapter = new PPNovelChapter();
                chapter.url = url + c.attr("href");
                chapter.name = c.text();
                chapters.add(chapter);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
