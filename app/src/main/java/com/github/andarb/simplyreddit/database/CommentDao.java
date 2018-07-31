package com.github.andarb.simplyreddit.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.github.andarb.simplyreddit.data.Comment;

import java.util.List;

@Dao
public interface CommentDao {
    @Query("SELECT * FROM comments WHERE category = :post")
    LiveData<List<Comment>> getComments(String post);

    @Insert
    void insertAll(List<Comment> comments);

    @Query("DELETE FROM comments WHERE category = :post")
    void deleteComments(String post);
}
