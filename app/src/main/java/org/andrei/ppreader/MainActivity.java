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

import java.util.List;

public class MainActivity extends FragmentActivity {

    @Override
    public void onBackPressed() {
        List<android.support.v4.app.Fragment> fs  = getSupportFragmentManager().getFragments();
        int sz = fs.size();
        // super.onBackPressed();//注释掉这行,back键不退出activity
        if(this.getSupportFragmentManager().findFragmentByTag(PPNovelMainFragment.TAG) != null){
            super.onBackPressed();
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
            //PPNovelReaderFragment fragment = new PPNovelReaderFragment();
            //fragment.setPPNovel(MockData.novel);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment,PPNovelCoverFragment.TAG).commit();
        }
        else{

        }
        changeStatusBarColor();
    }

    private void switchToMainFragment(){
        android.support.v4.app.Fragment fragment = new PPNovelMainFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container,fragment,PPNovelMainFragment.TAG);
        transaction.commit();
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
