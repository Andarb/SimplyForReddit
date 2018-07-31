package com.github.andarb.simplyreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.andarb.simplyreddit.adapters.CommentAdapter;
import com.github.andarb.simplyreddit.data.Comment;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.database.AppDatabase;
import com.github.andarb.simplyreddit.models.CommentsViewModel;
import com.github.andarb.simplyreddit.models.CommentsViewModelFactory;
import com.github.andarb.simplyreddit.models.PostsViewModel;
import com.github.andarb.simplyreddit.models.PostsViewModelFactory;
import com.github.andarb.simplyreddit.utils.PostPullService;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
    RecyclerView mRecyclerView;

    private String mPostUrl;
    private String mSubreddit;
    private AppDatabase mDb;
    private CommentAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        mDb = AppDatabase.getDatabase(getApplicationContext());

        mPostUrl = getIntent().getStringExtra(EXTRA_POST);
        mSubreddit = getIntent().getStringExtra(SubredditActivity.EXTRA_SUBREDDIT);
        if (mSubreddit.isEmpty()) mSubreddit = SubredditActivity.DEFAULT_SUBREDDIT;

        setTitle(getString(R.string.prefix_subreddit, mSubreddit));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup viewmodel for post details
        PostsViewModelFactory postsFactory = new PostsViewModelFactory(mDb, mPostUrl);
        PostsViewModel postsViewModel = ViewModelProviders.of(this, postsFactory)
                .get(PostsViewModel.class);
        postsViewModel.getPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(@Nullable List<Post> post) {
                if (!(post == null || post.isEmpty())) {
                    String imageUrl = post.get(0).getImageUrl();
                    String title = post.get(0).getTitle();
                    int score = post.get(0).getScore();
                    String author = post.get(0).getAuthor();
                    int time = post.get(0).getCreated();

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).into(mImageIV);
                    }
                    mPostTitleTV.setText(title);
                    mPostScoreTV.setText(String.valueOf(score));
                    mPostTimeAuthorTV.setText(getString(R.string.prefix_user, author) + " " + time);
                }
            }
        });

        mAdapter = new CommentAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mAdapter);

        // Setup viewmodel for comments
        CommentsViewModelFactory commentsFactory = new CommentsViewModelFactory(mDb, mPostUrl);
        CommentsViewModel commentsViewModel = ViewModelProviders.of(this, commentsFactory)
                .get(CommentsViewModel.class);
        commentsViewModel.getComments().observe(this, new Observer<List<Comment>>() {
            @Override
            public void onChanged(@Nullable List<Comment> comments) {
                mAdapter.setComments(comments);
                mAdapter.notifyDataSetChanged();
            }
        });

        // Retrieve post details and comments from ViewModel instead of making a network call
        // on configuration change
        if (savedInstanceState == null) {
            Intent intent = new Intent(this, PostPullService.class);
            intent.putExtra(PostPullService.EXTRA_CATEGORY, mPostUrl);
            startService(intent);
        }
    }
}
