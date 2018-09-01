package com.andarb.simplyforreddit.models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.andarb.simplyforreddit.data.Comment;
import com.andarb.simplyforreddit.database.AppDatabase;

import java.util.List;

public class CommentsViewModel extends ViewModel {

    private LiveData<List<Comment>> comments;

    public CommentsViewModel(AppDatabase database, String post) {
        comments = database.commentDao().getComments(post);
    }

    public LiveData<List<Comment>> getComments() {
        return comments;
    }
}
