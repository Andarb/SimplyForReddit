package com.github.andarb.simplyreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.github.andarb.simplyreddit.adapters.PostAdapter;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.database.AppDatabase;
import com.github.andarb.simplyreddit.models.PostsViewModel;
import com.github.andarb.simplyreddit.models.PostsViewModelFactory;
import com.github.andarb.simplyreddit.utils.PostPullService;

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

    private String mSubreddit;
    private AppDatabase mDb;
    private PostAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_list);
        ButterKnife.bind(this);

        mDb = AppDatabase.getDatabase(getApplicationContext());

        // Retrieve subreddit name to be loaded
        mSubreddit = getIntent().getStringExtra(EXTRA_SUBREDDIT);
        if (mSubreddit.isEmpty()) mSubreddit = DEFAULT_SUBREDDIT;

        setTitle(getString(R.string.prefix_subreddit, mSubreddit));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup recyclerview adapter
        mAdapter = new PostAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

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

        // Retrieve posts from ViewModel instead of making a network call on configuration change
        if (savedInstanceState == null) {
            Intent intent = new Intent(this, PostPullService.class);
            intent.putExtra(PostPullService.EXTRA_CATEGORY, mSubreddit);
            startService(intent);
        }
    }
}
