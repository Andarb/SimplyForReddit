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
    public static final String EXTRA_AFTER = "com.github.andarb.simplyreddit.extra.AFTER";

    private AppDatabase mDb;
    private String mCategory;
    private String mAfterKey;
    private Call<RedditPosts> mCall;

    public PostPullService() {
        super("PostPullService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mDb = AppDatabase.getDatabase(getApplicationContext());
        mCategory = intent.getStringExtra(EXTRA_CATEGORY);
        mAfterKey = intent.getStringExtra(EXTRA_AFTER);
        mCall = RetrofitClient.getCategory(mCategory, mAfterKey);

        Intent statusIntent = new Intent(ACTION_BROADCAST);
        statusIntent.putExtra(EXTRA_BROADCAST, mCategory);

        Response<RedditPosts> response;
        try {
            response = mCall.execute();
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

            if (mAfterKey == null) {
                mDb.postDao().deletePosts(mCategory);
            }
            mDb.postDao().insertAll(redditPosts.getPosts());

            if (!(redditPosts.getComments() == null)) {
                if (!(redditPosts.getComments().isEmpty())) {
                    mDb.commentDao().deleteComments(mCategory);
                    mDb.commentDao().insertAll(redditPosts.getComments());
                }
            }
        } else {
            Log.w(TAG, "Response unsuccessful:" + response.code());
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
    }
}
