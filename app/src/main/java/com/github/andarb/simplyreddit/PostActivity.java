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
import com.github.andarb.simplyreddit.data.RedditPost;
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
        Call<RedditPost> getCall;
        getCall = RetrofitClient.getPost(mPostUrl);

        getCall.enqueue(new Callback<RedditPost>() {
            @Override
            public void onResponse(Call<RedditPost> call,
                                   Response<RedditPost> response) {
                if (response.isSuccessful()) {
                    RedditPost post = response.body();

                    if (post == null) {
                        Log.w(TAG, "Failed deserializing JSON");
                        return;
                    }

                    String imageUrl = post.getPosts().get(0).getImageUrl();
                    String title = post.getPosts().get(0).getTitle();
                    int score = post.getPosts().get(0).getScore();
                    String author = post.getPosts().get(0).getAuthor();
                    int time = post.getPosts().get(0).getCreated();

                    Picasso.get().load(imageUrl).into(mImageIV);
                    mPostTitleTV.setText(title);
                    mPostScoreTV.setText(String.valueOf(score));
                    mPostTimeAuthorTV.setText(getString(R.string.prefix_user, author) + " " + time);

                    CommentAdapter commentAdapter =
                            new CommentAdapter(PostActivity.this, post.getComments());
                    mCommentsRV.setLayoutManager(new LinearLayoutManager(PostActivity.this,
                            LinearLayoutManager.VERTICAL, false));
                    mCommentsRV.setHasFixedSize(false);
                    mCommentsRV.setAdapter(commentAdapter);

                } else {
                    Log.w(TAG, "Response code:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<RedditPost> call, Throwable t) {
                Log.w(TAG, "Response failed");
                Log.w(TAG, t.getMessage());
            }
        });
    }
}
