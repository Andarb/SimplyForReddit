package com.github.andarb.simplyreddit.data;

public class Data {
    private String subreddit;

    private String title;

    private int score;

    private String thumbnail;

    private int created;

    private boolean over_18;

    private String author;

    private String permalink;

    private String url;

    private Preview preview;

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

    public void setOver_18(boolean over_18) {
        this.over_18 = over_18;
    }

    public boolean getOver_18() {
        return this.over_18;
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

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    public Preview getPreview() {
        return this.preview;
    }
}
