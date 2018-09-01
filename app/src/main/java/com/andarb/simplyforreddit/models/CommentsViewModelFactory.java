package com.andarb.simplyforreddit.models;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.andarb.simplyforreddit.database.AppDatabase;

public class CommentsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final String mPost;

    public CommentsViewModelFactory(AppDatabase database, String post) {
        mDb = database;
        mPost = post;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new CommentsViewModel(mDb, mPost);
    }
}