package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.utils.RetrofitClient;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = PostActivity.class.getSimpleName();
    public static final String EXTRA_POST = "com.github.andarb.simplyreddit.extra.POST";

    String mPost;
    String mSubreddit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        mPost = getIntent().getStringExtra(EXTRA_POST);
        mSubreddit = getIntent().getStringExtra(SubredditActivity.EXTRA_SUBREDDIT);
        if (mSubreddit.isEmpty()) mSubreddit = SubredditActivity.DEFAULT_SUBREDDIT;

        Log.w(TAG, "post:" + mPost);

        setTitle(getString(R.string.prefix_subreddit, mSubreddit));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        retrievePost();
    }

    /* Download and parse a Reddit post */
    private void retrievePost() {
        Call<RedditPosts> getCall;
        getCall = RetrofitClient.getPost(mPost);

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

                    Toast.makeText(PostActivity.this, redditPosts.getChildren()
                            .get(0).getData().getAuthor(), Toast.LENGTH_LONG).show();

                } else {
                    Log.w(TAG, "Response code:" + response.code());
                    Log.w(TAG, "Response message:" + response.message());
                    Log.w(TAG, "Response headers:" + response.headers());
                    Log.w(TAG, "Response error body:" + response.errorBody());
                    Log.w(TAG, "Response body:" + response.toString());
                }
            }

            @Override
            public void onFailure(Call<RedditPosts> call, Throwable t) {
                Log.w(TAG, "Response failed");
                Log.w(TAG, t.getMessage());
            }
        });
    }
}
