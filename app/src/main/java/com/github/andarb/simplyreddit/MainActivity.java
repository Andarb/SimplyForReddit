package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    // Reddit post categories
    public static final String[] PAGES = {"HOT", "TOP", "NEW"};

    @BindView(R.id.pager)
    ViewPager mPager;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PostsPagerAdapter viewPagerAdapter = new PostsPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(viewPagerAdapter);
        mTabLayout.setupWithViewPager(mPager);
    }


    /* Switch between different post categories (see PAGES[]) */
    public static class PostsPagerAdapter extends FragmentPagerAdapter {

        public PostsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // Number of pages
        @Override
        public int getCount() {
            return PAGES.length;
        }

        // Page to display
        @Override
        public Fragment getItem(int position) {
            return PostsFragment.newInstance(position);
        }

        // Return page title
        @Override
        public CharSequence getPageTitle(int position) {
            return PAGES[position];
        }

    }
}
