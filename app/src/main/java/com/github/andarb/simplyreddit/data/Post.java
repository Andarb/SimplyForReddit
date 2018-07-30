package com.github.andarb.simplyreddit.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "posts")
public class Post {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String subreddit;
    private String title;
    private int score;
    private String thumbnail;
    private int created;
    private String author;
    private String permalink;
    private String sourceUrl;
    private String imageUrl;
    private String category;

    @Ignore
    public Post(String subreddit, String title, int score, String thumbnail, int created,
                String author, String permalink, String sourceUrl, String imageUrl, String category) {
        this.subreddit = subreddit;
        this.title = title;
        this.score = score;
        this.thumbnail = thumbnail;
        this.created = created;
        this.author = author;
        this.permalink = permalink;
        this.sourceUrl = sourceUrl;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public Post(int id, String subreddit, String title, int score, String thumbnail, int created,
                String author, String permalink, String sourceUrl, String imageUrl, String category) {
        this.id = id;
        this.subreddit = subreddit;
        this.title = title;
        this.score = score;
        this.thumbnail = thumbnail;
        this.created = created;
        this.author = author;
        this.permalink = permalink;
        this.sourceUrl = sourceUrl;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getSubreddit() {
        return this.subreddit;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return this.score;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnail() {
        return this.thumbnail;
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

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getPermalink() {
        return this.permalink;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceUrl() {
        return this.sourceUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
