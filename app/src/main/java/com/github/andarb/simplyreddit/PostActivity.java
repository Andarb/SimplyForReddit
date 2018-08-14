package com.github.andarb.simplyreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.andarb.simplyreddit.adapters.CommentAdapter;
import com.github.andarb.simplyreddit.data.Comment;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.database.AppDatabase;
import com.github.andarb.simplyreddit.models.CommentsViewModel;
import com.github.andarb.simplyreddit.models.CommentsViewModelFactory;
import com.github.andarb.simplyreddit.models.PostsViewModel;
import com.github.andarb.simplyreddit.models.PostsViewModelFactory;
import com.github.andarb.simplyreddit.utils.PostPullService;
import com.github.andarb.simplyreddit.utils.RetrofitClient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostActivity extends AppCompatActivity {

    public static final String EXTRA_POST = "com.github.andarb.simplyreddit.extra.POST";

    @BindView(R.id.post_image_iv)
    ImageView mImageIV;
    @BindView(R.id.play_icon_iv)
    ImageView mPlayIconIV;
    @BindView(R.id.post_title_tv)
    TextView mTitleTV;
    @BindView(R.id.post_url_tv)
    TextView mUrlTV;
    @BindView(R.id.post_body_tv)
    TextView mBodyTV;
    @BindView(R.id.post_score_tv)
    TextView mScoreTV;
    @BindView(R.id.post_author_tv)
    TextView mAuthorTV;
    @BindView(R.id.post_time_tv)
    TextView mTimeTV;
    @BindView(R.id.see_all_button_tv)
    Button mSeeAllButton;
    @BindView(R.id.comments_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.post_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.post_details_pb)
    ProgressBar mProgressBar;
    @BindView(R.id.image_placeholder_iv)
    ImageView mPlaceholderIv;

    private String mPostUrl;
    private AppDatabase mDb;
    private CommentAdapter mAdapter;
    private StatusReceiver mStatusReceiver;
    private Intent mShareIntent;
    private boolean mIsNewActivity = false;

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

        mToolbar.setTitle(getString(R.string.prefix_subreddit, subreddit));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will help us prevent unnecessary network calls when going back in the stack
                finish();
            }
        });


        // Setup recyclerview adapter for comments
        mAdapter = new CommentAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mAdapter);

        setupViewModels();

        if (savedInstanceState == null) mIsNewActivity = true;
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
                if (post != null && !post.isEmpty()) {

                    // Retrieve post details
                    final String redditUrl = RetrofitClient.BASE_URL + post.get(0).getPermalink();
                    String mediaUrl = post.get(0).getMediaUrl();
                    final String sourceUrl = post.get(0).getSourceUrl();
                    String title = post.get(0).getTitle();
                    String body = post.get(0).getBody();
                    String score = post.get(0).getScore();
                    String author = post.get(0).getAuthor();
                    final boolean isVideo = post.get(0).isVideo();
                    String time = DateUtils.getRelativeTimeSpanString(post.get(0).getCreated(),
                            System.currentTimeMillis(), 0).toString();

                    loadMedia(mediaUrl, isVideo);

                    // Populate TextViews
                    mTitleTV.setText(title);
                    mAuthorTV.setText(getString(R.string.prefix_user, author));
                    mTimeTV.setText(getString(R.string.prefix_time, time));
                    mScoreTV.setText(score);
                    mUrlTV.setText(parseLink(sourceUrl));
                    mUrlTV.setPaintFlags(mUrlTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    if (body != null && !body.isEmpty()) {
                        mBodyTV.setVisibility(View.VISIBLE);
                        mBodyTV.setText(body);
                    }

                    setClickListeners(sourceUrl, redditUrl);
                }
            }

            /* Load the placeholder animation and the preview image of the media */
            private void loadMedia(String url, final boolean isVideo) {
                if (url != null && !url.isEmpty()) {

                    // ImageView does not support GIF by default, so load placeholder with Glide
                    Glide.with(PostActivity.this)
                            .load(R.drawable.loading_animation)
                            .apply(new RequestOptions().override(160, 24))
                            .into(mPlaceholderIv);
                    mPlaceholderIv.setVisibility(View.VISIBLE);

                    // Load the media preview
                    Glide.with(PostActivity.this)
                            .load(url)
                            .apply(new RequestOptions().error(R.drawable.broken_image_black_48))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(
                                        @Nullable GlideException e, Object model,
                                        Target<Drawable> target, boolean isFirstResource) {
                                    mPlaceholderIv.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(
                                        Drawable resource, Object model, Target<Drawable> target,
                                        DataSource dataSource, boolean isFirstResource) {
                                    mPlaceholderIv.setVisibility(View.GONE);

                                    // If this is a video, display a play icon on top of the
                                    // he preview image, and add tint
                                    if (isVideo) {
                                        mImageIV.setColorFilter(
                                                getResources().getColor(R.color.colorImageTint));
                                        mPlayIconIV.setVisibility(View.VISIBLE);
                                    }
                                    return false;
                                }
                            })
                            .into(mImageIV);
                }
            }

            private void setClickListeners(final String source, final String reddit) {
                // Clicking on the preview image or URL will open the media source
                mImageIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchUrl(source);
                    }
                });
                mUrlTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchUrl(source);
                    }
                });

                // Clicking on the title or on the "see all" button will open reddit's webpage
                mTitleTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchUrl(reddit);
                    }
                });
                mSeeAllButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchUrl(reddit);
                    }
                });

                mShareIntent = new Intent();
                mShareIntent.setAction(Intent.ACTION_SEND);
                mShareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_format, source));
                mShareIntent.setType("text/plain");
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

    /* Pull new data from the internet */
    private void refreshpost() {
        mProgressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, PostPullService.class);
        intent.putExtra(PostPullService.EXTRA_CATEGORY, mPostUrl);
        startService(intent);
    }

    // Opens a browser to view the media source URL
    private void launchUrl(String url) {
        Uri sourceUrl = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, sourceUrl);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // For better readability remove "http(s)" and "www" from the URL
    private String parseLink(String url) {
        int index = url.indexOf("://");
        if (index != -1) {
            url = url.substring(index + 3, url.length());
        }

        if (url.substring(0, 4).contains("www.")) {
            url = url.substring(4, url.length());
        }

        return url;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register download status receiver
        mStatusReceiver = new StatusReceiver();
        IntentFilter intentFilter = new IntentFilter(PostPullService.ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mStatusReceiver, intentFilter);

        // On configuration change retrieve posts from ViewModel instead of making a network call
        if (mIsNewActivity) refreshpost();
    }


    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mStatusReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_and_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshpost();

                return true;
            case R.id.action_share:
                startActivity(Intent.createChooser(mShareIntent, getString(R.string.action_share)));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Hides progressbar when new data is received, or the retrieval fails */
    private class StatusReceiver extends BroadcastReceiver {
        private StatusReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String extra = intent.getStringExtra(PostPullService.EXTRA_BROADCAST);

            if (action != null && action.equals(PostPullService.ACTION_BROADCAST)) {
                if (extra != null && extra.equals(mPostUrl)) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        }
    }
}
