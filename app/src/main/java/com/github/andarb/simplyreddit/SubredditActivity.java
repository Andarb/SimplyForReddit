package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.andarb.simplyreddit.adapters.PostsAdapter;
import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.utils.RetrofitClient;

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
    RecyclerView mPostsRV;

    String mSubreddit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_list);
        ButterKnife.bind(this);

        mSubreddit = getIntent().getStringExtra(EXTRA_SUBREDDIT);
        if (mSubreddit.isEmpty()) mSubreddit = DEFAULT_SUBREDDIT;

        setTitle(getString(R.string.prefix_subreddit, mSubreddit));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        retrievePosts();
    }

    /* Download and parse Reddit posts */
    private void retrievePosts() {
        Call<RedditPosts> getCall;
        getCall = RetrofitClient.getSubreddit(mSubreddit);

        getCall.enqueue(new Callback<RedditPosts>() {
            @Override
            public void onResponse(Call<RedditPosts> call,
                                   Response<RedditPosts> response) {
                if (response.isSuccessful()) {
                    RedditPosts redditPosts = response.body();

                    if (redditPosts == null) {
                        Log.w(TAG, "Failed deserializing JSON");
                        return;
                    }

                    PostsAdapter postsAdapter = new PostsAdapter(SubredditActivity.this, redditPosts);
                    mPostsRV.setLayoutManager(new LinearLayoutManager(SubredditActivity.this,
                            LinearLayoutManager.VERTICAL, false));
                    mPostsRV.setHasFixedSize(true);
                    mPostsRV.setAdapter(postsAdapter);
                } else {
                    Log.w(TAG, "Response not successful:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<RedditPosts> call, Throwable t) {
                Log.w(TAG, "Response failed");
            }
        });
    }
}
