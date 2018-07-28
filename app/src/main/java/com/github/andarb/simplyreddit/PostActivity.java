package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.utils.RetrofitClient;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = PostActivity.class.getSimpleName();
    public static final String EXTRA_POST = "com.github.andarb.simplyreddit.extra.POST";

    @BindView(R.id.post_image_iv)
    ImageView mImageIV;
    @BindView(R.id.post_title_tv)
    TextView mPostTitleTV;
    @BindView(R.id.post_upvote_count_tv)
    TextView mPostScoreTV;
    @BindView(R.id.post_time_and_author_tv)
    TextView mPostTimeAuthorTV;

    String mPostUrl;
    String mSubreddit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        mPostUrl = getIntent().getStringExtra(EXTRA_POST);
        mSubreddit = getIntent().getStringExtra(SubredditActivity.EXTRA_SUBREDDIT);

        if (mSubreddit.isEmpty()) mSubreddit = SubredditActivity.DEFAULT_SUBREDDIT;

        setTitle(getString(R.string.prefix_subreddit, mSubreddit));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        retrievePost();
    }

    /* Download and parse a Reddit post */
    private void retrievePost() {
        Call<RedditPosts> getCall;
        getCall = RetrofitClient.getPost(mPostUrl);

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

                    String imageUrl = redditPosts.getChildren().get(0).getData().getPreview()
                            .getImages().get(0).getSource().getUrl();
                    String title = redditPosts.getChildren().get(0).getData().getTitle();
                    int score = redditPosts.getChildren().get(0).getData().getScore();
                    String author = redditPosts.getChildren().get(0).getData().getAuthor();
                    int time = redditPosts.getChildren().get(0).getData().getCreated();

                    Picasso.get().load(imageUrl).into(mImageIV);
                    mPostTitleTV.setText(title);
                    mPostScoreTV.setText(String.valueOf(score));
                    mPostTimeAuthorTV.setText(getString(R.string.prefix_user, author) + " " + time);

                } else {
                    Log.w(TAG, "Response code:" + response.code());
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
