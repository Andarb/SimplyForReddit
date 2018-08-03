package com.github.andarb.simplyreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.andarb.simplyreddit.adapters.CommentAdapter;
import com.github.andarb.simplyreddit.data.Comment;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.database.AppDatabase;
import com.github.andarb.simplyreddit.models.CommentsViewModel;
import com.github.andarb.simplyreddit.models.CommentsViewModelFactory;
import com.github.andarb.simplyreddit.models.PostsViewModel;
import com.github.andarb.simplyreddit.models.PostsViewModelFactory;
import com.github.andarb.simplyreddit.utils.PostPullService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = PostActivity.class.getSimpleName();
    public static final String EXTRA_POST = "com.github.andarb.simplyreddit.extra.POST";

    @BindView(R.id.post_image_iv)
    ImageView mImageIV;
    @BindView(R.id.play_icon_iv)
    ImageView mPlayIconIV;
    @BindView(R.id.post_title_tv)
    TextView mTitleTV;
    @BindView(R.id.post_url_tv)
    TextView mUrlTV;
    @BindView(R.id.post_upvote_count_tv)
    TextView mScoreTV;
    @BindView(R.id.post_time_and_author_tv)
    TextView mTimeAuthorTV;
    @BindView(R.id.comments_recycler_view)
    RecyclerView mRecyclerView;

    private String mPostUrl;
    private AppDatabase mDb;
    private CommentAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        mDb = AppDatabase.getDatabase(getApplicationContext());

        // Retrieve extras
        mPostUrl = getIntent().getStringExtra(EXTRA_POST);
        String subreddit = getIntent().getStringExtra(SubredditActivity.EXTRA_SUBREDDIT);
        if (subreddit.isEmpty()) subreddit = SubredditActivity.DEFAULT_SUBREDDIT;

        setTitle(getString(R.string.prefix_subreddit, subreddit));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup recyclerview adapter for comments
        mAdapter = new CommentAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mAdapter);

        setupViewModels();

        // On configuration change, retrieve data from ViewModel instead of making a network call
        if (savedInstanceState == null) {
            Intent intent = new Intent(this, PostPullService.class);
            intent.putExtra(PostPullService.EXTRA_CATEGORY, mPostUrl);
            startService(intent);
        }
    }

    /* Configures ViewModels for post details and comments */
    private void setupViewModels() {
        // ViewModel for post details
        PostsViewModelFactory postsFactory = new PostsViewModelFactory(mDb, mPostUrl);
        PostsViewModel postsViewModel = ViewModelProviders.of(this, postsFactory)
                .get(PostsViewModel.class);
        postsViewModel.getPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(@Nullable final List<Post> post) {
                if (!(post == null || post.isEmpty())) {
                    // Retrieve post details
                    String mediaUrl = post.get(0).getMediaUrl();
                    final String sourceUrl = post.get(0).getSourceUrl();
                    String title = post.get(0).getTitle();
                    long score = post.get(0).getScore();
                    String author = post.get(0).getAuthor();
                    boolean isVideo = post.get(0).isVideo();
                    String time = DateUtils.getRelativeTimeSpanString(post.get(0).getCreated(),
                            System.currentTimeMillis(), 0).toString();

                    // Load the preview image
                    if (mediaUrl != null && !mediaUrl.isEmpty()) {
                        Glide.with(PostActivity.this)
                                .load(mediaUrl)
                                .into(mImageIV);

                        // If this is a video, display a play icon on top of the preview image,
                        // and tint it
                        if (isVideo) {
                            mImageIV.setColorFilter(getResources().getColor(R.color.imageTint));
                            mPlayIconIV.setVisibility(View.VISIBLE);
                        }
                    }

                    // Clicking on the preview image, or URL will open the source
                    mImageIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            launchUrl(sourceUrl);
                        }
                    });
                    mUrlTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            launchUrl(sourceUrl);
                        }
                    });

                    mTimeAuthorTV.setText(getString(R.string.prefix_user_time, author, time));
                    mTitleTV.setText(title);
                    mUrlTV.setText(removeHttpString(sourceUrl));
                    mScoreTV.setText(String.valueOf(score));
                }
            }
        });

        // ViewModel for comments
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
    }

    // Opens a browser to view the media source URL
    private void launchUrl(String url) {
        Uri sourceUrl = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, sourceUrl);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // For better readability, remove "https://", "http://", "www." from the full URL
    private String removeHttpString(String url) {
        int index = url.indexOf("://");
        if (index != -1) {
            url = url.substring(index + 3, url.length());
        }

        if (url.substring(0, 4).contains("www.")) {
            url = url.substring(4, url.length());
        }

        return url;
    }
}
