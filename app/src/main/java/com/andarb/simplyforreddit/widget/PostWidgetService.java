package com.andarb.simplyforreddit.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.andarb.simplyforreddit.R;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class PostWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new com.andarb.simplyforreddit.widget.PostRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class PostRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = PostRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private List<com.andarb.simplyforreddit.data.Post> mPosts = null;

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
        Call<com.andarb.simplyforreddit.data.RedditPosts> call = com.andarb.simplyforreddit.utils.RetrofitClient.getCategory(com.andarb.simplyforreddit.MainActivity.PAGES[0], null);
        Response<com.andarb.simplyforreddit.data.RedditPosts> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            return;
        }

        if (response.isSuccessful()) {
            final com.andarb.simplyforreddit.data.RedditPosts redditPosts = response.body();

            if (redditPosts == null) {
                return;
            }

            mPosts = redditPosts.getPosts();
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
        extras.putString(com.andarb.simplyforreddit.widget.PostWidgetProvider.EXTRA_WIDGET_POST,
                mPosts.get(position).getPermalink());
        extras.putString(com.andarb.simplyforreddit.widget.PostWidgetProvider.EXTRA_WIDGET_SUBREDDIT, subreddit);
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