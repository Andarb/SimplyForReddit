package com.andarb.simplyforreddit.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andarb.simplyforreddit.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<com.andarb.simplyforreddit.data.Comment> mComments;
    private Context mContext;

    public CommentAdapter(Context context) {
        mContext = context;
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
        String body = mComments.get(position).getBody();
        String author = mComments.get(position).getAuthor();
        long score = mComments.get(position).getScore();
        String time = DateUtils.getRelativeTimeSpanString(mComments.get(position).getCreated(),
                System.currentTimeMillis(), 0).toString();

        holder.mBodyTV.setText(body);
        holder.mAuthorTV.setText(mContext.getString(R.string.prefix_user_short, author));
        holder.mScoreTV.setText(String.valueOf(score));
        holder.mTimeTv.setText(time);
    }

    @Override
    public int getItemCount() {
        return mComments == null ? 0 : mComments.size();
    }

    public void setComments(List<com.andarb.simplyforreddit.data.Comment> comments) {
        mComments = comments;
    }
}

