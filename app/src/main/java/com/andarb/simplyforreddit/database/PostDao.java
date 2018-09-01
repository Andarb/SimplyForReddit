package com.andarb.simplyforreddit.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts WHERE category = :category")
    LiveData<List<com.andarb.simplyforreddit.data.Post>> getPosts(String category);

    @Insert
    void insertAll(List<com.andarb.simplyforreddit.data.Post> posts);

    @Query("DELETE FROM posts WHERE category = :category")
    void deletePosts(String category);
}
