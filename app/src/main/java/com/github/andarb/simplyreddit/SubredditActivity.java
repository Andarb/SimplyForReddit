package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.andarb.simplyreddit.adapters.PostAdapter;
import com.github.andarb.simplyreddit.data.RedditPosts;
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
        Call<List<RedditPosts>> getCall;
        getCall = RetrofitClient.getSubreddit(mSubreddit);

        getCall.enqueue(new Callback<List<RedditPosts>>() {
            @Override
            public void onResponse(Call<List<RedditPosts>> call,
                                   Response<List<RedditPosts>> response) {
                if (response.isSuccessful()) {
                    List<RedditPosts> redditPosts = response.body();

                    if (redditPosts == null) {
                        Log.w(TAG, "Failed deserializing JSON");
                        return;
                    }

                    PostAdapter postAdapter = new PostAdapter(SubredditActivity.this, redditPosts.get(0));
                    mPostsRV.setLayoutManager(new LinearLayoutManager(SubredditActivity.this,
                            LinearLayoutManager.VERTICAL, false));
                    mPostsRV.setHasFixedSize(true);
                    mPostsRV.setAdapter(postAdapter);
                } else {
                    Log.w(TAG, "Response not successful:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RedditPosts>> call, Throwable t) {
                Log.w(TAG, "Response failed");
            }
        });
    }
}
