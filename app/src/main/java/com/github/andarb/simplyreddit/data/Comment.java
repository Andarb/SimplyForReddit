package com.github.andarb.simplyreddit.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int score;
    private int created;
    private String author;
    private String body;
    private String category;

    @Ignore
    public Comment(int score, int created, String author, String body, String category) {
        this.score = score;
        this.created = created;
        this.author = author;
        this.body = body;
        this.category = category;
    }

    public Comment(int id, int score, int created, String author, String body, String category) {
        this.id = id;
        this.score = score;
        this.created = created;
        this.author = author;
        this.body = body;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getCreated() {
        return this.created;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
