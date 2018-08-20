package com.github.andarb.simplyreddit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.andarb.simplyreddit.adapters.PostPagerAdapter;
import com.github.andarb.simplyreddit.utils.AdMob;
import com.github.andarb.simplyreddit.utils.PostPullService;

import java.util.Arrays;

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

    private StatusReceiver mStatusReceiver;
    private PostPagerAdapter mPagerAdapter;
    private boolean mIsNewActivity; // member that tracks if this is a newly created activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AdMob.initialize(this, findViewById(android.R.id.content));

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

        // To save on network calls, do not reload ViewPager when coming back to this Activity
        if (mIsNewActivity) {
            setupViewPager();
            mIsNewActivity = false;
        }

        // Highlight selected tab on first activity launch, and on configuration changes
        mPagerAdapter.highlightTab(this, mTabLayout.getTabAt(mPager.getCurrentItem()));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusReceiver);
    }

    /* Configure and intialize the ViewPager */
    private void setupViewPager() {
        mPagerAdapter = new PostPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(2);
        mTabLayout.setupWithViewPager(mPager);

        // Inflate custom tabs, and setup a tab listener to (un)highlight them
        mPagerAdapter.setupCustomTabs(this, mTabLayout);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPagerAdapter.highlightTab(MainActivity.this, tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mPagerAdapter.unHighlightTab(MainActivity.this, tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
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
                View refreshView = findViewById(R.id.action_refresh);
                Animation rotate = AnimationUtils.loadAnimation(this,
                        R.anim.rotate_clockwise);
                refreshView.startAnimation(rotate);

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
            int page = Arrays.asList(PAGES).indexOf(extra);

            if (action != null && action.equals(PostPullService.ACTION_BROADCAST) && page != -1) {
                ViewPagerFragment pagerFragment = (ViewPagerFragment) mPager.getAdapter()
                        .instantiateItem(mPager, page);

                pagerFragment.reportStatus(status);
            }
        }
    }
}
