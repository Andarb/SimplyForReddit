package com.github.andarb.simplyreddit.utils;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.database.AppDatabase;

import java.util.List;

public class PostViewModel extends AndroidViewModel {

    private LiveData<List<Post>> posts;

    public PostViewModel(Application application) {
        super(application);

        AppDatabase database = AppDatabase.getDatabase(this.getApplication());
        posts = database.postDao().getAll();
    }

    public LiveData<List<Post>> getPosts() {
        return posts;
    }
}