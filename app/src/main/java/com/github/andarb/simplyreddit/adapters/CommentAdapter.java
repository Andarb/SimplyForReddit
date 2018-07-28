package com.github.andarb.simplyreddit.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.andarb.simplyreddit.R;
import com.github.andarb.simplyreddit.data.RedditPosts;

import butterknife.ButterKnife;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private RedditPosts mRedditPosts;
    private Context mContext;

    public CommentAdapter(Context context, RedditPosts redditPosts) {
        mContext = context;
        mRedditPosts = redditPosts;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {


        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.comment_list_item, parent, false);

        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mRedditPosts.getChildren().size();
    }
}

