package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.andarb.simplyreddit.adapters.CommentAdapter;
import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.utils.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.util.List;

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
    @BindView(R.id.comments_recycler_view)
    RecyclerView mCommentsRV;

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
        Call<List<RedditPosts>> getCall;
        getCall = RetrofitClient.getPost(mPostUrl);

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

                    String imageUrl = redditPosts.get(0).getData().getChildren().get(0).getData()
                            .getPreview().getImages().get(0).getSource().getUrl();
                    String title = redditPosts.get(0).getData().getChildren().get(0).getData()
                            .getTitle();
                    int score = redditPosts.get(0).getData().getChildren().get(0).getData()
                            .getScore();
                    String author = redditPosts.get(0).getData().getChildren().get(0).getData()
                            .getAuthor();
                    int time = redditPosts.get(0).getData().getChildren().get(0).getData()
                            .getCreated();

                    Picasso.get().load(imageUrl).into(mImageIV);
                    mPostTitleTV.setText(title);
                    mPostScoreTV.setText(String.valueOf(score));
                    mPostTimeAuthorTV.setText(getString(R.string.prefix_user, author) + " " + time);

                    CommentAdapter commentAdapter =
                            new CommentAdapter(PostActivity.this, redditPosts.get(1));
                    mCommentsRV.setLayoutManager(new LinearLayoutManager(PostActivity.this,
                            LinearLayoutManager.VERTICAL, false));
                    mCommentsRV.setHasFixedSize(false);
                    mCommentsRV.setAdapter(commentAdapter);

                } else {
                    Log.w(TAG, "Response code:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RedditPosts>> call, Throwable t) {
                Log.w(TAG, "Response failed");
                Log.w(TAG, t.getMessage());
            }
        });
    }
}
