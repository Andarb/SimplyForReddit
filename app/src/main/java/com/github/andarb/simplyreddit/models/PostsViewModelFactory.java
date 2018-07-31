package com.github.andarb.simplyreddit.models;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.github.andarb.simplyreddit.database.AppDatabase;

public class PostsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final String mCategory;

    public PostsViewModelFactory(AppDatabase database, String category) {
        mDb = database;
        mCategory = category;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new PostsViewModel(mDb, mCategory);
    }
}