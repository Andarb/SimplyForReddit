package com.github.andarb.simplyreddit.utils;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.database.AppDatabase;

import java.util.List;

public class PostsViewModel extends ViewModel {

    private LiveData<List<Post>> posts;

    public PostsViewModel(AppDatabase database, String category) {
        posts = database.postDao().getCategory(category);
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }
}