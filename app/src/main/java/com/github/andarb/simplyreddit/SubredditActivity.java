package com.github.andarb.simplyreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.andarb.simplyreddit.adapters.PostAdapter;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.database.AppDatabase;
import com.github.andarb.simplyreddit.models.PostsViewModel;
import com.github.andarb.simplyreddit.models.PostsViewModelFactory;
import com.github.andarb.simplyreddit.utils.PostPullService;
import com.paginate.Paginate;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Posts for one chosen subreddit will be displayed in this activity.
 */
public class SubredditActivity extends AppCompatActivity {

    private static final String TAG = SubredditActivity.class.getSimpleName();
    public static final String DEFAULT_SUBREDDIT = "all";
    public static final String EXTRA_SUBREDDIT = "com.github.andarb.simplyreddit.extra.SUBREDDIT";

    @BindView(R.id.posts_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.subreddit_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.post_list_pb)
    ProgressBar mProgressBar;

    private String mSubreddit;
    private AppDatabase mDb;
    private PostAdapter mAdapter;
    private StatusReceiver mStatusReceiver;
    private boolean mIsNewActivity;
    private boolean mIsLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);
        ButterKnife.bind(this);

        mDb = AppDatabase.getDatabase(getApplicationContext());

        // Retrieve subreddit name to be loaded
        mSubreddit = getIntent().getStringExtra(EXTRA_SUBREDDIT);
        if (mSubreddit.isEmpty()) mSubreddit = DEFAULT_SUBREDDIT;

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle(getString(R.string.prefix_subreddit, mSubreddit));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will help us prevent unnecessary network calls when going back in the stack
                finish();
            }
        });

        setupRvAdapter();

        // Setup viewmodel for adapter data
        PostsViewModelFactory factory = new PostsViewModelFactory(mDb, mSubreddit);
        PostsViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(PostsViewModel.class);
        viewModel.getPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(@Nullable List<Post> posts) {
                mAdapter.setPosts(posts);
                mAdapter.notifyDataSetChanged();
            }
        });

        if (savedInstanceState == null) mIsNewActivity = true;
    }

    /* Pull new data from the internet */
    private void refreshList() {
        mIsLoading = true;
        mProgressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, PostPullService.class);
        intent.putExtra(PostPullService.EXTRA_CATEGORY, mSubreddit);
        startService(intent);
    }

    /* Setup RecyclerView adapter */
    private void setupRvAdapter() {
        mAdapter = new PostAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        Paginate.Callbacks callbacks = new Paginate.Callbacks() {
            @Override
            public void onLoadMore() {
                mIsLoading = true;
                Intent intent = new Intent(SubredditActivity.this,
                        PostPullService.class);
                intent.putExtra(PostPullService.EXTRA_CATEGORY, mSubreddit);
                intent.putExtra(PostPullService.EXTRA_AFTER, mAdapter.getAfterKey());
                startService(intent);
            }

            @Override
            public boolean isLoading() {
                return mIsLoading;
            }

            @Override
            public boolean hasLoadedAllItems() {
                // If there is no "after" key, then we have reached the end
                return mAdapter.getAfterKey() == null;
            }
        };

        Paginate.with(mRecyclerView, callbacks)
                .setLoadingTriggerThreshold(1)
                .addLoadingListItem(true)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register download status receiver
        mStatusReceiver = new StatusReceiver();
        IntentFilter intentFilter = new IntentFilter(PostPullService.ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mStatusReceiver, intentFilter);

        // On configuration change, or when coming back from another activity,
        // retrieve posts from ViewModel instead of making another network call
        if (mIsNewActivity) {
            refreshList();
            mIsNewActivity = false;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusReceiver);
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
                refreshList();
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

            if (action != null && action.equals(PostPullService.ACTION_BROADCAST)) {
                if (extra != null && extra.equals(mSubreddit)) {
                    mIsLoading = false;
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        }
    }
}
