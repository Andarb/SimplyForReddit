package com.github.andarb.simplyreddit.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.andarb.simplyreddit.PostActivity;
import com.github.andarb.simplyreddit.R;
import com.github.andarb.simplyreddit.SubredditActivity;
import com.github.andarb.simplyreddit.data.Post;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> mRedditPosts;
    private Context mContext;

    public PostAdapter(Context context) {
        mContext = context;
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
        @BindView(R.id.post_time_and_author_tv)
        TextView mPostTimeAuthorTV;

        public PostViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            // Show posts of a clicked subreddit
            mPostSubredditTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position < 0) return;

                    Intent intent = new Intent(mContext, SubredditActivity.class);
                    intent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, mRedditPosts.get(position)
                            .getSubreddit());

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
        long score = mRedditPosts.get(position).getScore();
        String author = mRedditPosts.get(position).getAuthor();
        String time = DateUtils.getRelativeTimeSpanString(mRedditPosts.get(position).getCreated(),
                System.currentTimeMillis(), 0).toString();

        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Glide.with(mContext).load(thumbnailUrl).into(holder.mThumbnailIV);
        }
        holder.mPostTitleTV.setText(title);
        holder.mPostSubredditTV.setText(mContext.getString(R.string.prefix_subreddit, subreddit));
        holder.mPostScoreTV.setText(String.valueOf(score));
        holder.mPostTimeAuthorTV.setText(mContext
                .getString(R.string.prefix_user_time, author, time));
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
