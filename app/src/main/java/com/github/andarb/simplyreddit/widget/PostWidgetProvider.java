package com.github.andarb.simplyreddit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

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
    private static final String ACTION_REFRESH =
            "com.github.andarb.simplyreddit.widget.ACTION_REFRESH";
    public static final String EXTRA_WIDGET_POST =
            "com.github.andarb.simplyreddit.widget.EXTRA_POST";
    public static final String EXTRA_WIDGET_SUBREDDIT =
            "com.github.andarb.simplyreddit.widget.EXTRA_SUBREDDIT";

    /* Start an activity to display clicked item's post details. Or, reload ListView items. */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (action.equals(ACTION_POST)) {
            String post = intent.getStringExtra(EXTRA_WIDGET_POST);
            String subreddit = intent.getStringExtra(EXTRA_WIDGET_SUBREDDIT);

            Intent postActivityIntent = new Intent(context, PostActivity.class);
            postActivityIntent.putExtra(PostActivity.EXTRA_POST, post);
            postActivityIntent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, subreddit);

            context.startActivity(postActivityIntent);
        } else if (action.equals(ACTION_REFRESH)) {
            Toast.makeText(context, context.getString(R.string.refresh_toast),
                    Toast.LENGTH_SHORT).show();
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            int widgetIds[] = widgetManager.getAppWidgetIds(new ComponentName(context,
                    PostWidgetProvider.class));

            widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_post_list_view);
        }
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Set up intent for the list view
            Intent listViewIntent = new Intent(context, PostWidgetService.class);
            listViewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            listViewIntent.setData(Uri.parse(listViewIntent.toUri(Intent.URI_INTENT_SCHEME)));

            // Set up list view adapter
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.post_widget);
            rv.setRemoteAdapter(R.id.widget_post_list_view, listViewIntent);
            rv.setEmptyView(R.id.widget_post_list_view, R.id.widget_empty_view);

            // Set up pending template to make list view items react to clicks
            Intent listItemIntent = new Intent(context, PostWidgetProvider.class);
            listItemIntent.setAction(PostWidgetProvider.ACTION_POST);
            listItemIntent.setData(Uri.parse(listItemIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0,
                    listItemIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_post_list_view, toastPendingIntent);

            // Set an intent for refreshing the list on icon click
            Intent refreshIntent = new Intent(context, PostWidgetProvider.class);
            refreshIntent.setAction(ACTION_REFRESH);
            PendingIntent refreshPendingIntent =
                    PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
            rv.setOnClickPendingIntent(R.id.widget_refresh_iv, refreshPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }
}
