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

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class CrawlTianYiNovel implements ICrawlNovel {
    @Override
    public Observable<PPNovel> search(final String name) {
        return Observable.create(new ObservableOnSubscribe<PPNovel>() {
            @Override
            public void subscribe(ObservableEmitter<PPNovel> e) throws Exception {
                try{
                    String url = "https://www.tywx.la/searchbook.php?keyword=" + name;
                    Document doc = Jsoup.connect(url).timeout(6000).get();
                    Elements root = doc.getElementsByAttributeValue("id","alistbox");
                    if(root == null || root.size() == 0){
                        Integer i = R.string.err_not_found;
                        Throwable err = new Throwable(i.toString());
                        e.onError(err);
                        return;
                    }
                    boolean bFetch = false;
                    for(int i = 0 ; i <root.size(); i++){
                        Element item = root.get(i);
                        PPNovel novel = new PPNovel();
                        boolean bRet = fetchNovel(item,novel);
                        if(bRet){
                            bFetch = true;
                            e.onNext(novel);
                        }
                    }
                    if(!bFetch){
                        Integer i = R.string.err_not_found;
                        Throwable err = new Throwable(i.toString());
                        e.onError(err);
                        return;
                    }
                    else{
                        e.onComplete();
                    }
                }
                catch(Exception ex){
                    if(!e.isDisposed()){
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
    public Observable<CrawlTextResult> fetchNovelText(final String novelId,final String chapterUrl) {
        return Observable.create(new ObservableOnSubscribe<CrawlTextResult>() {
            @Override
            public void subscribe(ObservableEmitter<CrawlTextResult> e) throws Exception {
                try{
                    Document doc = Jsoup.connect(chapterUrl).timeout(6000).get();
                    Element item = doc.getElementById("content");
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

    private boolean fetchNovel(final Element element, final PPNovel novel){
        try{
            Element item = element.getElementsByTag("img").get(0);
            novel.imgUrl = "https://www.tywx.la" + item.attr("src");
            novel.name = item.attr("alt");
            item = element.getElementsByTag("a").get(0);
            novel.chapterUrl = "https://www.tywx.la" + item.attr("href");
            item = element.getElementsByTag("span").get(0);
            novel.author = item.text();
            novel.author = novel.author.substring(3);
            item = element.getElementsByClass("intro").get(0);
            novel.desc = item.text();
            novel.desc = novel.desc.replaceAll(" ","");
            Integer type = new Integer(0);
            boolean bRet = fetchChaptersInner(novel.chapterUrl,novel.chapters,type);
            if(!bRet)
                return false;
            novel.type = type;
        }catch(Exception ex){
            return false;
        }

        return true;
    }

    private boolean fetchChaptersInner(final String url, final ArrayList<PPNovelChapter> chapters, Integer type){
        try{
            Document doc = Jsoup.connect(url).timeout(6000).get();
            if(type != null){
                Element item = doc.getElementsByClass("ui_tb1").get(0);
                Elements tds = item.getElementsByTag("td");
                for(int i = 0 ; i <tds.size(); i++){
                    Element it = tds.get(i);
                    Elements bs = it.getElementsByTag("b");
                    if(bs.size() >0 && bs.get(0).text().indexOf("小说分类") != -1){
                        if(it.text().indexOf("连载") != -1){
                            type = PPNovel.TYPE_ING;
                        }
                        else{
                            type = PPNovel.TYPE_OVER;
                        }
                        break;
                    }
                }
            }
            Elements cs = doc.getElementsByClass("chapterlist");
            if(cs.size() <= 1){
                return false;
            }
            for(int i = 1 ; i < cs.size() ; i++){
                Element item = cs.get(i);
                Elements cl = item.getElementsByTag("a");
                for(int j = 0 ; j < cl.size() ; j++){
                    Element it = cl.get(j);
                    String style = it.attr("style");
                    if(style.indexOf("color:Gray") != -1){
                        continue;
                    }
                    PPNovelChapter chapter = new PPNovelChapter();
                    chapter.name = it.text();
                    chapter.url = "https://www.tywx.la" + it.attr("href");
                    chapters.add(chapter);
                }
            }

            if(chapters.size() == 0){
                return false;
            }

        }
        catch(Exception ex){
            return false;
        }

        return true;
    }
}
