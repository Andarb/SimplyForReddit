package com.github.andarb.simplyreddit.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.github.andarb.simplyreddit.data.Post;

import java.util.List;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts WHERE category = :category")
    LiveData<List<Post>> getCategory(String category);

    @Insert
    void insertAll(List<Post> posts);

    @Query("DELETE FROM posts WHERE category = :category")
    void deleteCategory(String category);
}
