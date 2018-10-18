package org.andrei.ppreader.service.engines;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelError;
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
    public String getName(){
        return "88dus";
    }

    @Override
    public int search(final String name,ObservableEmitter<PPNovel> e) {
        String url = "https://so.88dus.com/search/so.php?search_field=0&q=" + name;
        Document doc = null;
        try {
            doc = Jsoup.connect(url).timeout(18000).get();
        } catch (IOException e1) {
            return CrawlNovelError.ERR_NETWORK;
        }
        Elements els = doc.getElementsByClass("ops_cover");
        if (els.size() == 0) {
            return CrawlNovelError.ERR_NONE_FETCHED;
        }
        Elements items = els.get(0).getElementsByClass("block");
        if (items.size() == 0) {
            return  CrawlNovelError.ERR_NONE_FETCHED;
        }

        boolean isFetched = false;
        for (Element item : items) {
            PPNovel novel = new PPNovel();
            if (fetchNovel(item, novel) == CrawlNovelError.ERR_NONE) {
                isFetched = true;
                novel.engineName = getName();
                e.onNext(novel);
            }
        }
        if (!isFetched) {
            return CrawlNovelError.ERR_NONE_FETCHED;
        }

        return CrawlNovelError.ERR_NONE;
    }

    @Override
    public int fetchChapters(final PPNovel novel, CrawlChapterResult ret) {
        ret = new CrawlChapterResult();
        ret.chapterUrl = novel.chapterUrl;
        Document doc = null;
        try {
            doc = Jsoup.connect(novel.chapterUrl).timeout(18000).get();
            return  fetchChaptersInner(doc,novel.chapterUrl,ret.chapters);
        } catch (IOException e) {
            return CrawlNovelError.ERR_NETWORK;
        }
    }

    @Override
    public int fetchNovelText(final String novelId, final String chapterUrl,CrawlTextResult ret) {
        try{
            Document doc = Jsoup.connect(chapterUrl).timeout(18000).get();
            Element item = doc.getElementsByClass("yd_text2").get(0);
            String text = item.html();
            if(text.isEmpty()){
                return CrawlNovelError.ERR_NONE_FETCHED;
            }
            text = Utils.adjustText(text);

            ret.chapterUrl = chapterUrl;
            ret.novelUrl = novelId;
            ret.text = text;

            return CrawlNovelError.ERR_NONE;

        }
        catch (IOException ex){
            return CrawlNovelError.ERR_NETWORK;
        }
    }

    private int fetchNovel(final Element element, final PPNovel novel) {
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

            Document doc = Jsoup.connect(novel.chapterUrl).timeout(18000).get();

            item = doc.getElementsByTag("em").get(1);
            if(item.text().indexOf("连载") != -1){
                novel.type =  PPNovel.TYPE_ING;
            }
            else{
                novel.type = PPNovel.TYPE_OVER;
            }
            return  fetchChaptersInner(doc,novel.chapterUrl,novel.chapters);
        } catch (IOException ex) {
            return CrawlNovelError.ERR_NETWORK;
        }
    }

    private int fetchChaptersInner(final Document doc,final String url, final ArrayList<PPNovelChapter> chapters) {
        Elements mulus = doc.getElementsByClass("mulu");
        if(mulus == null || mulus.size() == 0){
            return CrawlNovelError.ERR_NONE_FETCHED;
        }
        Element root = mulus.get(0);
        Elements cs = root.getElementsByTag("a");
        for(Element c :cs){
            PPNovelChapter chapter = new PPNovelChapter();
            chapter.url = url + c.attr("href");
            chapter.name = c.text();
            chapters.add(chapter);
        }
        return CrawlNovelError.ERR_NONE;
    }
}
