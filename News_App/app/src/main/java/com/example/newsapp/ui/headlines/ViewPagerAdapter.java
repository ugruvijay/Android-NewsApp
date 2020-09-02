package com.example.newsapp.ui.headlines;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private int numOfTabs;

    public ViewPagerAdapter(FragmentManager fm, int numOfTabs){
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment tab_fragment = null;
        switch (position){
            case 0:
                tab_fragment =  new WorldHeadlineFragment();
                break;
            case 1:
                tab_fragment = new BusinessHeadlineFragment();
                break;
            case 2:
                tab_fragment = new PoliticsHeadlineFragment();
                break;
            case 3:
                tab_fragment = new SportsHeadlineFragment();
                break;
            case 4:
                tab_fragment = new TechnologyHeadlineFragment();
                break;
            case 5:
                tab_fragment = new ScienceHeadlineFragment();
                break;
        }
        return tab_fragment;
    }

    @Override
    public CharSequence getPageTitle(int position){
        CharSequence title = "";
        switch (position){
            case 0:
                title = "World";
                break;
            case 1:
                title = "Business";
                break;
            case 2:
                title = "Politics";
                break;
            case 3:
                title = "Sports";
                break;
            case 4:
                title = "Technology";
                break;
            case 5:
                title = "Science";
                break;
        }
        return title;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
