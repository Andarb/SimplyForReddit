package com.github.andarb.simplyreddit.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.github.andarb.simplyreddit.data.Post;

import java.util.List;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts")
    LiveData<List<Post>> getAll();

    @Insert
    void insert(Post post);

    @Insert
    void insertAll(List<Post> posts);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Post post);

    @Delete
    void delete(Post post);
}
