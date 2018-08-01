package com.github.andarb.simplyreddit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.github.andarb.simplyreddit.PostActivity;
import com.github.andarb.simplyreddit.R;
import com.github.andarb.simplyreddit.SubredditActivity;


/**
 * Widget will download and display a list of hot subreddit posts.
 * Posts are displayed in a list view.
 * List view items can be clicked to open post details within the app.
 */
public class PostWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_POST = "com.github.andarb.simplyreddit.widget.ACTION_POST";
    public static final String EXTRA_WIDGET_POST =
            "com.github.andarb.simplyreddit.widget.EXTRA_POST";
    public static final String EXTRA_WIDGET_SUBREDDIT =
            "com.github.andarb.simplyreddit.widget.EXTRA_SUBREDDIT";

    /* Start an activity to display clicked item's post details */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_POST)) {
            String post = intent.getStringExtra(EXTRA_WIDGET_POST);
            String subreddit = intent.getStringExtra(EXTRA_WIDGET_SUBREDDIT);

            Intent postActivityIntent = new Intent(context, PostActivity.class);
            postActivityIntent.putExtra(PostActivity.EXTRA_POST, post);
            postActivityIntent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, subreddit);

            context.startActivity(postActivityIntent);
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Set up intent for the list view
            Intent intent = new Intent(context, PostWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Set up list view adapter
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.post_widget);
            rv.setRemoteAdapter(R.id.widget_post_list_view, intent);
            rv.setEmptyView(R.id.widget_post_list_view, R.id.widget_empty_view);

            // Set up pending template to make list view items react to clicks
            Intent listItemIntent = new Intent(context, PostWidgetProvider.class);
            listItemIntent.setAction(PostWidgetProvider.ACTION_POST);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0,
                    listItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_post_list_view, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }
}
