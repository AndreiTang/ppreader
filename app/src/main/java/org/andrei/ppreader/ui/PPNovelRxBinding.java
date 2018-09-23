package org.andrei.ppreader.ui;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;




import io.reactivex.Observable;

import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

import static com.jakewharton.rxbinding2.internal.Preconditions.checkMainThread;

public class PPNovelRxBinding {
  public static Observable<Integer> pageSelected(@NonNull ViewPager vp){
      return new PageSelectedObservable(vp);
  }


  static class PageSelectedObservable extends Observable<Integer>{

      final ViewPager m_vp;

      PageSelectedObservable(ViewPager vp){
          m_vp = vp;
      }

      @Override
      protected void subscribeActual(Observer<? super Integer> observer) {
          Listener listener = new Listener(m_vp, observer);
          observer.onSubscribe(listener);
          m_vp.addOnPageChangeListener(listener);
      }

      static final class Listener extends MainThreadDisposable implements ViewPager.OnPageChangeListener {
          private final ViewPager m_vp;
          private final Observer<? super Integer> m_observer;

          Listener(ViewPager vp, Observer<? super Integer> observer) {
              m_vp = vp;
              m_observer = observer;
          }


          @Override protected void onDispose() {
              m_vp.removeOnPageChangeListener(this);
          }

          @Override
          public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

          }

          @Override
          public void onPageSelected(int position) {
              if(!isDisposed()){
                  m_observer.onNext(position);
              }

          }

          @Override
          public void onPageScrollStateChanged(int state) {

          }
      }
  }


}
