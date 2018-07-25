package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.utils.RetrofitClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.post_title_tv)
    TextView mPostTitleTV;
    @BindView(R.id.post_subreddit_tv)
    TextView mPostSubredditTV;
    @BindView(R.id.post_upvote_count_tv)
    TextView mPostScoreTV;
    @BindView(R.id.post_time_and_author_tv)
    TextView mPostTimeAuthorTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        retrievePosts();
    }

    /* Download and parse Reddit posts */
    private void retrievePosts() {
        Call<RedditPosts> getCall = RetrofitClient.getHot();

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

                    String title = redditPosts.getChildren().get(0).getData().getTitle();
                    String subreddit = redditPosts.getChildren().get(0).getData().getSubreddit();
                    int score = redditPosts.getChildren().get(0).getData().getScore();
                    String author = redditPosts.getChildren().get(0).getData().getAuthor();
                    int time = redditPosts.getChildren().get(0).getData().getCreated();

                    mPostTitleTV.setText(title);
                    mPostSubredditTV.setText(subreddit);
                    mPostScoreTV.setText(String.valueOf(score));
                    mPostTimeAuthorTV.setText(author + " " + time);
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
