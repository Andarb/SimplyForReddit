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

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private RedditPosts mComments;
    private Context mContext;

    public CommentAdapter(Context context, RedditPosts comments) {
        mContext = context;
        mComments = comments;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.comment_body_tv)
        TextView mBodyTV;
        @BindView(R.id.comment_author_tv)
        TextView mAuthorTV;
        @BindView(R.id.comment_score_tv)
        TextView mScoreTV;
        @BindView(R.id.comment_time_tv)
        TextView mTimeTv;

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
        String body = mComments.getData().getChildren().get(position).getData().getBody();
        String author = mComments.getData().getChildren().get(position).getData().getAuthor();
        int score = mComments.getData().getChildren().get(position).getData().getScore();
        int time = mComments.getData().getChildren().get(position).getData().getCreated();

        holder.mBodyTV.setText(body);
        holder.mAuthorTV.setText(author);
        holder.mScoreTV.setText(String.valueOf(score));
        holder.mTimeTv.setText(String.valueOf(time));
    }

    @Override
    public int getItemCount() {
        return mComments.getData().getChildren().size();
    }
}

