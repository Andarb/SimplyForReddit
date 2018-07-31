package com.github.andarb.simplyreddit.models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.github.andarb.simplyreddit.data.Comment;
import com.github.andarb.simplyreddit.database.AppDatabase;

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
