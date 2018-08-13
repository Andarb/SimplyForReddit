package com.github.andarb.simplyreddit.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.andarb.simplyreddit.PostActivity;
import com.github.andarb.simplyreddit.R;
import com.github.andarb.simplyreddit.SubredditActivity;
import com.github.andarb.simplyreddit.data.Post;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private String ANALYTICS_EVENT = "subreddit_view";
    private String ANALYTICS_PROPERTY = "subreddit_name";

    private FirebaseAnalytics mFirebaseAnalytics;
    private List<Post> mRedditPosts;
    private Context mContext;

    public PostAdapter(Context context) {
        mContext = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
    }


    class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.post_thumbnail_iv)
        ImageView mThumbnailIV;
        @BindView(R.id.post_title_tv)
        TextView mPostTitleTV;
        @BindView(R.id.post_subreddit_tv)
        TextView mPostSubredditTV;
        @BindView(R.id.post_upvote_count_tv)
        TextView mPostScoreTV;
        @BindView(R.id.post_time_tv)
        TextView mPostTimeTV;

        public PostViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            // Show posts of a clicked subreddit
            mPostSubredditTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position < 0) return;

                    String subreddit = mRedditPosts.get(position).getSubreddit();

                    // Log viewing of this subreddit
                    Bundle bundle = new Bundle();
                    bundle.putString(ANALYTICS_PROPERTY, subreddit);
                    mFirebaseAnalytics.logEvent(ANALYTICS_EVENT, bundle);

                    Intent intent = new Intent(mContext, SubredditActivity.class);
                    intent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, subreddit);

                    mContext.startActivity(intent);
                }
            });

            // If list item is clicked (anywhere but the subreddit name) - open the chosen post page
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position < 0) return;

            Intent intent = new Intent(mContext, PostActivity.class);
            intent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, mRedditPosts.get(position)
                    .getSubreddit());
            intent.putExtra(PostActivity.EXTRA_POST, mRedditPosts.get(position).getPermalink());

            mContext.startActivity(intent);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.post_list_item, parent, false);

        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        String thumbnailUrl = mRedditPosts.get(position).getThumbnail();
        String title = mRedditPosts.get(position).getTitle();
        String subreddit = mRedditPosts.get(position).getSubreddit();
        String score = mRedditPosts.get(position).getScore();
        String time = DateUtils.getRelativeTimeSpanString(mRedditPosts.get(position).getCreated(),
                System.currentTimeMillis(), 0).toString();

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty() && thumbnailUrl.contains("http")) {
            Glide.with(mContext)
                    .load(thumbnailUrl)
                    .apply(new RequestOptions().error(R.drawable.broken_image_black_48))
                    .into(holder.mThumbnailIV);
        } else {
            holder.mThumbnailIV.setImageResource(R.drawable.text_icon_black_48);
        }
        holder.mPostTitleTV.setText(title);
        holder.mPostSubredditTV.setText(mContext.getString(R.string.prefix_subreddit, subreddit));
        holder.mPostScoreTV.setText(score);
        holder.mPostTimeTV.setText(mContext.getString(R.string.prefix_time, time));
    }

    @Override
    public int getItemCount() {
        return mRedditPosts == null ? 0 : mRedditPosts.size();
    }

    public void setPosts(List<Post> posts) {
        mRedditPosts = posts;
    }

    public String getAfterKey() {
        if (mRedditPosts != null && !mRedditPosts.isEmpty()) {
            return mRedditPosts.get(getItemCount() - 1).getAfter();
        } else {
            return null;
        }

    }
}
