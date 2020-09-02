package com.example.newsapp.ui.headlines;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.example.newsapp.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class HeadlinesFragment extends Fragment {
    private boolean shouldRefreshOnResume = false;
    private HeadlinesViewModel dashboardViewModel;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    View root;
    HeadlinesFragment headlinesFragment;

    public HeadlinesFragment(){
        headlinesFragment = this;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(HeadlinesViewModel.class);
        root = inflater.inflate(R.layout.fragment_headlines, container, false);
        tabLayout = root.findViewById(R.id.tab_layout);

        viewPager = root.findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getFragmentManager(), 6);

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        viewPager.setCurrentItem(0);


        return root;
    }
}
