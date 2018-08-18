package com.github.andarb.simplyreddit.utils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.github.andarb.simplyreddit.R;
import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.database.AppDatabase;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class PostPullService extends IntentService {

    public static final String STATUS_SUCCESS = "com.github.andarb.simplyreddit.status.SUCCESS";

    // Sent intent extras
    public static final String ACTION_BROADCAST =
            "com.github.andarb.simplyreddit.action.BROADCAST";
    public static final String EXTRA_BROADCAST = "com.github.andarb.simplyreddit.extra.BROADCAST";
    public static final String EXTRA_STATUS = "com.github.andarb.simplyreddit.extra.STATUS";

    // Received intent extras
    public static final String EXTRA_CATEGORY = "com.github.andarb.simplyreddit.extra.CATEGORY";
    public static final String EXTRA_AFTER = "com.github.andarb.simplyreddit.extra.AFTER";


    public PostPullService() {
        super("PostPullService");
    }

    private Intent mStatusIntent;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        String category = intent.getStringExtra(EXTRA_CATEGORY);
        String afterKey = intent.getStringExtra(EXTRA_AFTER);
        Call<RedditPosts> call = RetrofitClient.getCategory(category, afterKey);

        mStatusIntent = new Intent(ACTION_BROADCAST);
        mStatusIntent.putExtra(EXTRA_BROADCAST, category);

        Response<RedditPosts> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            sendStatus(getString(R.string.status_no_internet));
            return;
        }

        if (response.isSuccessful()) {
            final RedditPosts redditPosts = response.body();

            // If no posts found, return
            if (redditPosts == null) {
                sendStatus(getString(R.string.status_no_posts));
                return;
            }

            // When retrieving first page of posts, delete the old ones
            if (afterKey == null) {
                db.postDao().deletePosts(category);
            }

            db.postDao().insertAll(redditPosts.getPosts());

            // Populate db with comments if there are any
            if (!(redditPosts.getComments() == null)) {
                if (!redditPosts.getComments().isEmpty()) {
                    db.commentDao().deleteComments(category);
                    db.commentDao().insertAll(redditPosts.getComments());
                } else {
                    sendStatus(getString(R.string.status_no_comments));
                }
            }

        } else {
            sendStatus(getString(R.string.status_server_error));
        }

        sendStatus(STATUS_SUCCESS);
    }

    // Send a broadcast back to the calling activity with a status report
    private void sendStatus(String status) {
        mStatusIntent.putExtra(EXTRA_STATUS, status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(mStatusIntent);
    }
}
