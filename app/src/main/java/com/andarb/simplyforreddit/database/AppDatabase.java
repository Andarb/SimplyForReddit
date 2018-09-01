package com.andarb.simplyforreddit.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.andarb.simplyforreddit.data.Comment;
import com.andarb.simplyforreddit.data.Post;

@Database(entities = {Post.class, Comment.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static com.andarb.simplyforreddit.database.AppDatabase sAppDatabase;

    public static com.andarb.simplyforreddit.database.AppDatabase getDatabase(Context context) {
        if (sAppDatabase == null) {
            sAppDatabase = Room.databaseBuilder(context.getApplicationContext(),
                    com.andarb.simplyforreddit.database.AppDatabase.class, "redditposts").build();
        }
        return sAppDatabase;
    }

    public abstract PostDao postDao();

    public abstract CommentDao commentDao();
}

