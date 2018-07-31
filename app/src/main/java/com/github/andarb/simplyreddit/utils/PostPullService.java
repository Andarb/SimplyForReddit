package com.github.andarb.simplyreddit.utils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.database.AppDatabase;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class PostPullService extends IntentService {
    private static final String TAG = PostPullService.class.getSimpleName();
    public static final String EXTRA_CATEGORY = "com.github.andarb.simplyreddit.extra.CATEGORY";

    public PostPullService() {
        super("PostPullService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        final String category = intent.getStringExtra(EXTRA_CATEGORY);

        Call<RedditPosts> call = RetrofitClient.getCategory(category);
        Response<RedditPosts> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            Log.w(TAG, "No internet connection");
            return;
        }

        if (response.isSuccessful()) {
            final RedditPosts redditPosts = response.body();

            if (redditPosts == null) {
                Log.w(TAG, "Failed deserializing JSON");
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
            Log.w(TAG, "Response not successful:" + response.code());
        }
    }

}
