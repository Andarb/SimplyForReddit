package com.github.andarb.simplyreddit.data;

import java.util.List;

public class RedditPosts {

    private List<Children> children;

    private String after;

    private String before;

    public void setChildren(List<Children> children) {
        this.children = children;
    }

    public List<Children> getChildren() {
        return this.children;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getAfter() {
        return this.after;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getBefore() {
        return this.before;
    }
}

