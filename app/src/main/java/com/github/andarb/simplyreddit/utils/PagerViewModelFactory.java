package com.github.andarb.simplyreddit.utils;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.github.andarb.simplyreddit.database.AppDatabase;

public class PagerViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mPage;

    public PagerViewModelFactory(AppDatabase database, int page) {
        mDb = database;
        mPage = page;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new PagerViewModel(mDb, mPage);
    }
}