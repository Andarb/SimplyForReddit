package com.github.andarb.simplyreddit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.andarb.simplyreddit.utils.PostPullService;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    // Reddit post categories
    public static final String[] PAGES = {"HOT", "TOP", "NEW"};

    // App id used with admob for testing purposes only
    private static final String ADMOB_ID = "ca-app-pub-3940256099942544~3347511713";

    @BindView(R.id.pager)
    ViewPager mPager;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.pager_toolbar)
    Toolbar mToolbar;

    private StatusReceiver mStatusReceiver;
    private boolean mIsNewActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mIsNewActivity = true;
        setSupportActionBar(mToolbar);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Register download status receiver
        mStatusReceiver = new StatusReceiver();
        IntentFilter intentFilter = new IntentFilter(PostPullService.ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mStatusReceiver, intentFilter);

        // This will help us prevent unnecessary network calls when going back in the stack
        if (mIsNewActivity) {
            PostsPagerAdapter viewPagerAdapter = new PostsPagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(viewPagerAdapter);
            mPager.setOffscreenPageLimit(2);
            mTabLayout.setupWithViewPager(mPager);
            mIsNewActivity = false;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusReceiver);
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

    /* Hides progressbar when new data is received, or the retrieval fails */
    private class StatusReceiver extends BroadcastReceiver {
        private StatusReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String extra = intent.getStringExtra(PostPullService.EXTRA_BROADCAST);
            String status = intent.getStringExtra(PostPullService.EXTRA_STATUS);
            int page = Arrays.asList(MainActivity.PAGES).indexOf(extra);

            if (action != null && action.equals(PostPullService.ACTION_BROADCAST) && page != -1) {
                ViewPagerFragment pagerFragment = (ViewPagerFragment) mPager.getAdapter()
                        .instantiateItem(mPager, page);

                pagerFragment.reportStatus(status);
            }
        }
    }
}
