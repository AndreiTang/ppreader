package org.andrei.ppreader.service;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.andrei.ppreader.MockData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

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

    public CrawlNovel builder(@NonNull final String  name){
        CrawlNovel crawlNovel =  new CrawlNovel();
        crawlNovel.setCurrentCrawlNovelEngine(name);
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
        m_novels.remove(novel);
    }

    public Observable<PPNovel> loadPPNovels(final String folder){
        Observable<PPNovel> observable = Observable.create(new ObservableOnSubscribe<PPNovel>() {
            @Override
            public void subscribe(ObservableEmitter<PPNovel> emitter) throws Exception {

                File file = new File(folder);
                File files[] = file.listFiles();
                if(files.length == 0){
                    Thread.sleep(3000);
                    emitter.onComplete();
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
                            novel.status = PPNovel.STATUS_UNCHECKED;
                            novel.needRemove = false;
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
