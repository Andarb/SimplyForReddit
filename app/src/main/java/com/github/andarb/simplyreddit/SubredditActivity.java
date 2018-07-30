package com.github.andarb.simplyreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.andarb.simplyreddit.adapters.PostAdapter;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.data.RedditPost;
import com.github.andarb.simplyreddit.database.AppDatabase;
import com.github.andarb.simplyreddit.utils.AppExecutor;
import com.github.andarb.simplyreddit.utils.PostsViewModel;
import com.github.andarb.simplyreddit.utils.PostsViewModelFactory;
import com.github.andarb.simplyreddit.utils.RetrofitClient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
            retrievePosts();
        }
    }

    /* Download and parse Reddit posts */
    private void retrievePosts() {
        Call<RedditPost> getCall;
        getCall = RetrofitClient.getSubreddit(mSubreddit);

        getCall.enqueue(new Callback<RedditPost>() {
            @Override
            public void onResponse(Call<RedditPost> call,
                                   Response<RedditPost> response) {
                if (response.isSuccessful()) {
                    final RedditPost redditPosts = response.body();

                    if (redditPosts == null) {
                        Log.w(TAG, "Failed deserializing JSON");
                        return;
                    }

                    AppExecutor.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.postDao().deleteCategory(mSubreddit);
                        }
                    });

                    AppExecutor.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.postDao().insertAll(redditPosts.getPosts());
                        }
                    });
                } else {
                    Log.w(TAG, "Response not successful:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<RedditPost> call, Throwable t) {
                Log.w(TAG, "Response failed");
            }
        });
    }
}
