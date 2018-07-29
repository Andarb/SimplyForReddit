package com.github.andarb.simplyreddit.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.github.andarb.simplyreddit.data.Post;

@Database(entities = {Post.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sAppDatabase;

    public static AppDatabase getDatabase(Context context) {
        if (sAppDatabase == null) {
            sAppDatabase = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "redditposts").build();
        }
        return sAppDatabase;
    }

    public abstract PostDao postDao();
}

