package com.andarb.simplyforreddit.models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.andarb.simplyforreddit.data.Post;
import com.andarb.simplyforreddit.database.AppDatabase;

import java.util.List;

public class PostsViewModel extends ViewModel {

    private LiveData<List<Post>> posts;

    public PostsViewModel(AppDatabase database, String category) {
        posts = database.postDao().getPosts(category);
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }
}