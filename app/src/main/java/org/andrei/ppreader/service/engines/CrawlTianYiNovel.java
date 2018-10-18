package org.andrei.ppreader.service.engines;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlChapterResult;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelError;
import org.andrei.ppreader.service.CrawlNovelService;
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

public class CrawlTianYiNovel implements ICrawlNovel {
    @Override
    public int search(final String name,ObservableEmitter<PPNovel> e) {
        try{
            String url = "https://www.tywx.la/searchbook.php?keyword=" + name;
            Document doc = Jsoup.connect(url).timeout(60000).get();
            Elements root = doc.getElementsByAttributeValue("id","alistbox");
            if(root == null || root.size() == 0){
                return CrawlNovelError.ERR_NONE_FETCHED;
            }
            boolean bFetch = false;
            for(int i = 0 ; i <root.size(); i++){
                Element item = root.get(i);
                PPNovel novel = new PPNovel();
                if(fetchNovel(item,novel) == CrawlNovelError.ERR_NONE){
                    bFetch = true;
                    novel.engineName = getName();
                    e.onNext(novel);
                }
            }
            if(!bFetch){
                return CrawlNovelError.ERR_NONE_FETCHED;
            }
            else{
               return CrawlNovelError.ERR_NONE;
            }
        }
        catch(IOException ex){
           return CrawlNovelError.ERR_NETWORK;
        }
        catch (Exception ex){
            return CrawlNovelError.ERR_NONE_FETCHED;
        }
    }

    @Override
    public int fetchChapters(final PPNovel novel,CrawlChapterResult ret) {
        ret.chapterUrl = novel.chapterUrl;
        try {
            Document doc = Jsoup.connect(novel.chapterUrl).timeout(60000).get();
            return fetchChaptersInner(doc,ret.chapters);
        } catch (IOException e) {
            return CrawlNovelError.ERR_NETWORK;
        }
    }

    @Override
    public int fetchNovelText(final String novelId,final String chapterUrl, CrawlTextResult ret) {
        try{
            Document doc = Jsoup.connect(chapterUrl).timeout(60000).get();
            Element item = doc.getElementById("content");
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
        catch (IOException e){
            return CrawlNovelError.ERR_NETWORK;
        }
        catch (Exception ex){
            return CrawlNovelError.ERR_NONE_FETCHED;
        }
    }

    @Override
    public String getName() {
        return "tianyi";
    }

    private int fetchNovel(final Element element, final PPNovel novel){
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

            Document doc = Jsoup.connect(novel.chapterUrl).timeout(60000).get();

            item = doc.getElementsByClass("ui_tb1").get(0);
            Elements tds = item.getElementsByTag("td");
            for(int i = 0 ; i <tds.size(); i++){
                Element it = tds.get(i);
                Elements bs = it.getElementsByTag("b");
                if(bs.size() >0 && bs.get(0).text().indexOf("小说分类") != -1){
                    if(it.text().indexOf("连载") != -1){
                        novel.type = PPNovel.TYPE_ING;
                    }
                    else{
                        novel.type = PPNovel.TYPE_OVER;
                    }
                    break;
                }
            }

            return fetchChaptersInner(doc,novel.chapters);
        }catch (IOException iex){
            return CrawlNovelError.ERR_NETWORK;
        }
        catch(Exception ex){
            return CrawlNovelError.ERR_NONE_FETCHED;
        }
    }

    private int fetchChaptersInner(final Document doc, final ArrayList<PPNovelChapter> chapters){
        try{
            Elements cs = doc.getElementsByClass("chapterlist");
            if(cs.size() <= 1){
                return CrawlNovelError.ERR_NONE_FETCHED;
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
                return CrawlNovelError.ERR_NONE_FETCHED;
            }

        }
        catch(Exception ex){
            return CrawlNovelError.ERR_NONE_FETCHED;
        }

        return CrawlNovelError.ERR_NONE;

    }
}
