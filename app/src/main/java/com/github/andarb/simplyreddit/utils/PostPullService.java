package com.github.andarb.simplyreddit.utils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.database.AppDatabase;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class PostPullService extends IntentService {
    private static final String TAG = PostPullService.class.getSimpleName();
    public static final String ACTION_BROADCAST =
            "com.github.andarb.simplyreddit.action.BROADCAST";
    public static final String EXTRA_BROADCAST = "com.github.andarb.simplyreddit.extra.BROADCAST";
    public static final String EXTRA_CATEGORY = "com.github.andarb.simplyreddit.extra.CATEGORY";

    public PostPullService() {
        super("PostPullService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        final String category = intent.getStringExtra(EXTRA_CATEGORY);
        Intent statusIntent = new Intent(ACTION_BROADCAST);
        statusIntent.putExtra(EXTRA_BROADCAST, category);

        Call<RedditPosts> call = RetrofitClient.getCategory(category);
        Response<RedditPosts> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            Log.w(TAG, "No internet connection");
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
            return;
        }

        if (response.isSuccessful()) {
            final RedditPosts redditPosts = response.body();

            if (redditPosts == null) {
                Log.w(TAG, "Failed deserializing JSON");
                LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
                return;
            }

            db.postDao().deletePosts(category);
            db.postDao().insertAll(redditPosts.getPosts());

            if (!(redditPosts.getComments() == null)) {
                if (!(redditPosts.getComments().isEmpty())) {
                    db.commentDao().deleteComments(category);
                    db.commentDao().insertAll(redditPosts.getComments());
                }
            }
        } else {
            Log.w(TAG, "Response unsuccessful:" + response.code());
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
    }
}
