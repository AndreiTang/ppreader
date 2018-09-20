package org.andrei.ppreader.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.andrei.ppreader.ui.fragments.PPNovelListFragment;
import org.andrei.ppreader.ui.fragments.PPNovelSearchFragment;

public class PPNovelMainAdapter extends FragmentPagerAdapter {

    public PPNovelMainAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {
        return m_fragments[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

    Fragment[] m_fragments = {new PPNovelListFragment(),new PPNovelSearchFragment()};

}
