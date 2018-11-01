package org.andrei.ppreader.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.andrei.ppreader.ui.fragments.PPNovelListFragment;
import org.andrei.ppreader.ui.fragments.PPNovelSearchFragment;

public class PPNovelMainAdapter extends FragmentPagerAdapter {

    public PPNovelMainAdapter(FragmentManager fm,Fragment[] fragments) {
        super(fm);
        m_fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return m_fragments[position];
    }

    @Override
    public int getCount() {
        return m_fragments.length;
    }

    Fragment[] m_fragments = null;

}
