package org.andrei.ppreader.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.PPNovel;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**

 */
public class PPNovelCoverFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    final static public String TAG = "PPNovelCoverFragment";


    public PPNovelCoverFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ppnovel_cover, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/wawa.ttf");
        TextView tv = (TextView) getActivity().findViewById(R.id.cover_title);
        tv.setTypeface(typeface);

        this.getActivity().findViewById(android.R.id.content).setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        m_s_isShowed = true;

        //fix the bug that Glide will on the top of the page. It cause the fragment can't exit.
        Glide.with(this).load(R.drawable.rm);

        final FragmentActivity activity = (FragmentActivity)this.getActivity();
        CrawlNovelService.instance().loadPPNovels(getActivity().getApplicationContext().getFilesDir().getAbsolutePath()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<PPNovel>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(PPNovel value) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Fragment fragment = new PPNovelMainFragment();
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container,fragment,PPNovelMainFragment.TAG);
                transaction.commit();
            }
        });
    }

    public static boolean isShowed(){
        return m_s_isShowed;
    }

    private static boolean m_s_isShowed = false;

}
