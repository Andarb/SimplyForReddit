package com.github.andarb.simplyreddit.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.github.andarb.simplyreddit.MainActivity;
import com.github.andarb.simplyreddit.R;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.data.RedditPosts;
import com.github.andarb.simplyreddit.utils.RetrofitClient;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class PostWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PostRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class PostRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = PostRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private List<Post> mPosts = null;

    public PostRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
    }

    /* Download subreddit posts */
    @Override
    public void onDataSetChanged() {
        // PAGES[0] is "HOT". Retrieve only one page, so second parameter is null.
        Call<RedditPosts> call = RetrofitClient.getCategory(MainActivity.PAGES[0], null);
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

            mPosts = redditPosts.getPosts();

        } else {
            Log.w(TAG, "Response not successful:" + response.code());
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mPosts == null ? 0 : mPosts.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        String title = mPosts.get(position).getTitle();
        String time = DateUtils.getRelativeTimeSpanString(mPosts.get(position).getCreated(),
                System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        String subreddit = mPosts.get(position).getSubreddit();

        // Populate list
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.post_widget_list_item);
        rv.setTextViewText(R.id.widget_title_tv, title);
        rv.setTextViewText(R.id.widget_subreddit_tv, mContext.getString(R.string.prefix_subreddit,
                subreddit));
        rv.setTextViewText(R.id.widget_time_tv, time);

        // Set up extras for list item click events
        Bundle extras = new Bundle();
        extras.putString(PostWidgetProvider.EXTRA_WIDGET_POST,
                mPosts.get(position).getPermalink());
        extras.putString(PostWidgetProvider.EXTRA_WIDGET_SUBREDDIT, subreddit);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.list_item_layout, fillInIntent);

        return rv;
    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}