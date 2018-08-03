package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    // Reddit post categories
    public static final String[] PAGES = {"HOT", "TOP", "NEW"};

    @BindView(R.id.pager)
    ViewPager mPager;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.pager_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        // Setup ViewPager
        PostsPagerAdapter viewPagerAdapter = new PostsPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(viewPagerAdapter);
        mPager.setOffscreenPageLimit(2);
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
            return ViewPagerFragment.newInstance(PAGES[position]);
        }

        // Return page title
        @Override
        public CharSequence getPageTitle(int position) {
            return PAGES[position];
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                ViewPagerFragment pagerFragment = (ViewPagerFragment) mPager.getAdapter()
                        .instantiateItem(mPager, mPager.getCurrentItem());
                pagerFragment.refreshPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
