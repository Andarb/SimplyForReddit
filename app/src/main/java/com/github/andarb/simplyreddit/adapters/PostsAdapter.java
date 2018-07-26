package com.github.andarb.simplyreddit.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.andarb.simplyreddit.R;
import com.github.andarb.simplyreddit.data.RedditPosts;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder> {

    RedditPosts mRedditPosts;
    Context mContext;

    public PostsAdapter(Context context, RedditPosts redditPosts) {
        mContext = context;
        mRedditPosts = redditPosts;
    }


    class PostsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.post_title_tv)
        TextView mPostTitleTV;
        @BindView(R.id.post_subreddit_tv)
        TextView mPostSubredditTV;
        @BindView(R.id.post_upvote_count_tv)
        TextView mPostScoreTV;
        @BindView(R.id.post_time_and_author_tv)
        TextView mPostTimeAuthorTV;

        public PostsViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

        }
    }

    @NonNull
    @Override
    public PostsAdapter.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.post_list_item, parent, false);

        return new PostsAdapter.PostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.PostsViewHolder holder, int position) {
        String title = mRedditPosts.getChildren().get(position).getData().getTitle();
        String subreddit = mRedditPosts.getChildren().get(position).getData().getSubreddit();
        int score = mRedditPosts.getChildren().get(position).getData().getScore();
        String author = mRedditPosts.getChildren().get(position).getData().getAuthor();
        int time = mRedditPosts.getChildren().get(position).getData().getCreated();

        holder.mPostTitleTV.setText(title);
        holder.mPostSubredditTV.setText(subreddit);
        holder.mPostScoreTV.setText(String.valueOf(score));
        holder.mPostTimeAuthorTV.setText(author + " " + time);
    }

    @Override
    public int getItemCount() {
        return mRedditPosts.getChildren().size();
    }
}
