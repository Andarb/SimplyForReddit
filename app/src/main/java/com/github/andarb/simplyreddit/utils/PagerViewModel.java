package com.github.andarb.simplyreddit.utils;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.database.AppDatabase;

import java.util.List;

public class PagerViewModel extends ViewModel {

    private LiveData<List<Post>> posts;

    public PagerViewModel(AppDatabase database, int page) {
        posts = database.postDao().getCategory(page);
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }
}