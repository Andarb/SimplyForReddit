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
    private long score;
    private String thumbnail;
    private long created;       // the time post was created at
    private String author;
    private String permalink;   // url to the chosen reddit post
    private String sourceUrl;   // original media source
    private String mediaUrl;    // preview of the media
    private String category;    // helper field for distinguishing different posts in db
    private boolean isVideo;    // if the post contains video - true, in any other case - false

    @Ignore
    public Post(String subreddit, String title, long score, String thumbnail, long created,
                String author, String permalink, String sourceUrl, String mediaUrl, String category,
                boolean isVideo) {
        this.subreddit = subreddit;
        this.title = title;
        this.score = score;
        this.thumbnail = thumbnail;
        this.created = created;
        this.author = author;
        this.permalink = permalink;
        this.sourceUrl = sourceUrl;
        this.mediaUrl = mediaUrl;
        this.category = category;
        this.isVideo = isVideo;
    }

    public Post(int id, String subreddit, String title, long score, String thumbnail, long created,
                String author, String permalink, String sourceUrl, String mediaUrl, String category,
                boolean isVideo) {
        this.id = id;
        this.subreddit = subreddit;
        this.title = title;
        this.score = score;
        this.thumbnail = thumbnail;
        this.created = created;
        this.author = author;
        this.permalink = permalink;
        this.sourceUrl = sourceUrl;
        this.mediaUrl = mediaUrl;
        this.category = category;
        this.isVideo = isVideo;
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

    public void setScore(long score) {
        this.score = score;
    }

    public long getScore() {
        return this.score;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getCreated() {
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

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }
}
