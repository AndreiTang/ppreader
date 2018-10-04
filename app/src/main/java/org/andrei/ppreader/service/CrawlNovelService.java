package org.andrei.ppreader.service;

import com.google.gson.Gson;

import org.andrei.ppreader.MockData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by andrei on 2018/9/12.
 */

public class CrawlNovelService {


    public static  CrawlNovelService instance(){
        return m_s_ins;
    }

    public CrawlNovel builder(){
        return new CrawlNovel();
    }

    public CrawlNovel builder(int index){
        CrawlNovel crawlNovel =  new CrawlNovel();
        crawlNovel.setCurrentCrawlNovelEngine(index);
        return crawlNovel;
    }

    public void saveNovel(final String folder, final PPNovel novel){
        String novelFile = folder + "/" + novel.name + ".json";
        Gson gson = new Gson();
        String txt = gson.toJson(novel);
        File file = new File(novelFile);
        if(file.exists()){
            file.delete();
        }
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(txt.getBytes());
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public  ArrayList<PPNovel> getPPNovels(){
        return m_novels;
    }

    public PPNovel getNovel(String id){
        for(int  i = 0 ; i <m_novels.size(); i++){
            PPNovel novel = m_novels.get(i);
            if(novel.chapterUrl.compareTo(id) == 0){
                return novel;
            }
        }
        return null;
    }

    public void removeNovel(final String folder,final PPNovel novel){
        String novelFile = folder + "/" + novel.name + ".json";
        File file = new File(novelFile);
        if(file.exists()){
            file.delete();
        }
    }

    public Observable<PPNovel> loadPPNovels(final String folder){
        Observable<PPNovel> observable = Observable.create(new ObservableOnSubscribe<PPNovel>() {
            @Override
            public void subscribe(ObservableEmitter<PPNovel> emitter) throws Exception {

                File file = new File(folder);
                File files[] = file.listFiles();
                if(files.length == 0){

                    Thread.sleep(5000);
                    emitter.onComplete();

//                    PPNovel novel = new PPNovel();
//                    novel.author = "爱潜水的乌贼";
//                    novel.chapterUrl = "https://www.88dus.com/xiaoshuo/38/38089/";
//                    novel.desc = "知识就等于力量。所谓神，不过是强大一点的奥术师。带着一大堆知识的夏风穿越而来了。";
//                    novel.engineIndex = 0;
//                    novel.imgUrl = "https://fm.88dus.com/38/38089/38089s.jpg";
//                    novel.name = "奥术神座";
//                    novel = MockData.novel;
//                    m_novels.add(novel);
//
//                    novel = new PPNovel();
//                    novel.author = "爱潜水的乌贼";
//                    novel.chapterUrl = "https://www.88dus.com/xiaoshuo/81/81340/";
//                    novel.desc = " 第一，不要笑书名。第二，不要笑封面。第三，不要笑简介。如果大家上面三句话会心笑了，说明本书风格应该挺适合你们的。在这里，武道不再是虚无缥缈的传说，而是切切实实的传承，经过与科技的对抗后，彻底融入了社会，有了各种各样的武道比赛，文无第一，武无第二！楼成得到武道一大流派断绝的传承后，向着最初的梦想，向着心里的荣耀......";
//                    novel.engineIndex = 0;
//                    novel.imgUrl = "https://fm.88dus.com/81/81340/81340s.jpg";
//                    novel.name = "武道宗师";
//                    m_novels.add(novel);
//
//                    novel = new PPNovel();
//                    novel.author = "爱潜水的乌贼";
//                    novel.chapterUrl = "https://www.88dus.com/xiaoshuo/102/102560/";
//                    novel.desc = "蒸汽与机械的浪潮中，谁能触及非凡？历史和黑暗的迷雾里，又是谁在耳语？我从诡秘中醒来，睁眼看见这个世界：枪械，大炮，巨舰，飞空艇，差分机；魔药，占卜，诅咒，倒吊人，封印物……光明依旧照耀，神秘从未远离，这是一段“愚者”的传说。......";
//                    novel.engineIndex = 0;
//                    novel.imgUrl = "https://fm.88dus.com/102/102560/102560s.jpg";
//                    novel.name = "诡秘之主";
//                    m_novels.add(novel);
//
//                    novel = new PPNovel();
//                    novel.author = "爱潜水的乌贼";
//                    novel.chapterUrl = "https://www.88dus.com/xiaoshuo/26/26085/";
//                    novel.desc = "修真，去假存真，照见本性。能达到这点的，则被称为“真人”，他们成就元神，超脱生死。灭运图录，灭运道种？一个偶得上古仙法的穿越客在这诸天万界、亿兆大千世界的修炼故事。";
//                    novel.engineIndex = 0;
//                    novel.imgUrl = "https://fm.88dus.com/26/26085/26085s.jpg";
//                    novel.name = "灭运图录";
//                    m_novels.add(novel);
//
//                    novel = new PPNovel();
//                    novel.author = "幸运的苏拉";
//                    novel.chapterUrl = "https://www.88dus.com/xiaoshuo/39/39670/";
//                    novel.desc = "条条大路通罗马，但来的却并不都是罗马想要的。\n" +
//                            " 斗兽场是小的罗马，罗马是大的斗兽场。\n" +
//                            " 他，李必达，不过是个精通语言学的普通大学生，却有幸穿越到了彼时，罗马建城678年，耶稣诞辰前66年，但他没有成为显贵，因为不可能；没有成为所谓发明家，因为不可能；他最初的身份，就是个军队奴隶，也是最合情合理的身份，自此开始了波澜壮阔的冒险生涯。\n" +
//                            " 凯撒、庞培、西塞罗、克拉苏、安东尼、屋大维、米特拉达梯不再是呆滞的蜡像。\n" +
//                            " 百夫长、商贩、拍卖人、修辞学家、女奴、祭司、娼妓不再是死板的文字。\n" +
//                            " 本都王女、贵族遗孀、亚马逊女王、埃及艳后不再是桃色的梦想。\n" +
//                            " 奴隶、自由奴、有产公民、骑士、度支官、军事护民官、骑兵长官、元老......狄克推多，这条铺满骸骨、头颅的道路走下来，谁能成为笑到最后的，独一无二的奥古斯都？";
//                    novel.engineIndex = 0;
//                    novel.imgUrl = "https://fm.88dus.com/39/39670/39670s.jpg";
//                    novel.name = "奥古斯都之路";
//                    m_novels.add(novel);
//
//
//                    Thread.sleep(5000);
//
//                    emitter.onComplete();
                }
                else{
                    for(File item : files){
                        String novelTxt = "";
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(item));
                            String line = "";
                            while ((line = reader.readLine()) != null) {
                                novelTxt += line + "\n";
                            }
                            reader.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!novelTxt.isEmpty()){
                            Gson gson = new Gson();
                            PPNovel novel = gson.fromJson(novelTxt,PPNovel.class);
                            m_novels.add(novel);
                        }
                    }
                    emitter.onComplete();
                }

            }
        });
        return observable.subscribeOn(Schedulers.io());
    }

    private static CrawlNovelService  m_s_ins = new CrawlNovelService();
    private ArrayList<PPNovel> m_novels = new ArrayList<PPNovel>();
}
