package org.andrei.ppreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.ui.fragments.PPNovelCoverFragment;
import org.andrei.ppreader.ui.fragments.PPNovelMainFragment;
import org.andrei.ppreader.ui.fragments.PPNovelReaderFragment;

import java.io.Console;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends FragmentActivity {

    public static final String TAG_FRAGMENT = "fragment";
    public static final String MAIN_FRAGMENT = "main";
    public static final String READER_FRAGMENT = "reader";
    public static final String NOVEL = "novel";

    @Override
    public void onBackPressed() {
        // super.onBackPressed();//注释掉这行,back键不退出activity
        if(this.getSupportFragmentManager().findFragmentByTag(PPNovelMainFragment.TAG) != null){
            finish();
            System.exit(0);
        }
        else if(this.getSupportFragmentManager().findFragmentByTag(PPNovelReaderFragment.TAG) != null){
            PPNovelReaderFragment  readerFragment = (PPNovelReaderFragment)getSupportFragmentManager().findFragmentByTag(PPNovelReaderFragment.TAG);
            readerFragment.switchToMainFragment(0);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            PPNovelCoverFragment fragment = new PPNovelCoverFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment,PPNovelCoverFragment.TAG).commit();
        }
        else{
            String frag = savedInstanceState.getString(TAG_FRAGMENT);
            if(frag.compareTo(READER_FRAGMENT) == 0){
                PPNovelMainFragment fragment = new PPNovelMainFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment,PPNovelMainFragment.TAG).commit();
            }
            else{
                PPNovelReaderFragment fragment = new PPNovelReaderFragment();
                String id  =  savedInstanceState.getString(NOVEL);
                PPNovel novel = CrawlNovelService.instance().getNovel(id);
                Bundle arg  = new Bundle();
                arg.putSerializable(PPNovelReaderFragment.NOVEL,novel);
                fragment.setArguments(arg);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment,PPNovelReaderFragment.TAG).commit();
            }
        }
        changeStatusBarColor();

        //test code
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                try{
                    while (true){
                        Thread.sleep(10);
                        //e.onNext(new Object());
                    }
                }
                catch (Exception ex){
                    int j = 0;
                    j++;
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Object>() {
            Disposable m_d = null;
            @Override
            public void onSubscribe(Disposable d) {
                m_d = d;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        m_d.dispose();
                    }
                }, 3000);
            }

            @Override
            public void onNext(Object value) {
                m_d.dispose();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        if(this.getSupportFragmentManager().findFragmentByTag(PPNovelMainFragment.TAG) != null){
            outState.putString(TAG_FRAGMENT,MAIN_FRAGMENT);
        }
        else if(this.getSupportFragmentManager().findFragmentByTag(PPNovelReaderFragment.TAG) != null){
            outState.putString(TAG_FRAGMENT,READER_FRAGMENT);
            PPNovelReaderFragment  readerFragment = (PPNovelReaderFragment)getSupportFragmentManager().findFragmentByTag(PPNovelReaderFragment.TAG);
            outState.putString(NOVEL,readerFragment.getNovel().chapterUrl);
        }
    }

    private void changeStatusBarColor(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.setStatusBarColor(Color.parseColor("#DBC49B"));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
