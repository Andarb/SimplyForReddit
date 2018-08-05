package com.github.andarb.simplyreddit.data;

import java.util.List;

public class RedditPosts {

    private List<Post> posts;
    private List<Comment> comments;

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
