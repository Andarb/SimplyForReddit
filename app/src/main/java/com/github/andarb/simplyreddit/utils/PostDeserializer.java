package com.github.andarb.simplyreddit.utils;

import com.github.andarb.simplyreddit.data.Comment;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.data.RedditPosts;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Custom JSON deserializer due to complex nested structure of the JSON returned by Reddit API.
 * This class will retrieve a list of posts, post details and comments when applicable.
 */
public class PostDeserializer implements JsonDeserializer<RedditPosts> {
    String mCategory;

    public PostDeserializer(String category) {
        mCategory = category;
    }

    @Override
    public RedditPosts deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        RedditPosts redditPosts = new RedditPosts();

        try {
            // If root JSON is an object, it will contain a list of posts. Otherwise, it's an
            // array which will contain post details and a list of comments.
            JsonObject postsRootObject;
            if (json.isJsonObject()) {
                postsRootObject = json.getAsJsonObject();
            } else {
                postsRootObject = json.getAsJsonArray().get(0).getAsJsonObject();

                // Retrieve post comments
                JsonObject commentsRootObject = json.getAsJsonArray().get(1).getAsJsonObject();
                List<Comment> comments = deserializeComments(commentsRootObject);
                redditPosts.setComments(comments);
            }

            // Retrieve `after` and `before` for pagination
            JsonObject postsDataObject = postsRootObject.get("data").getAsJsonObject();
            String before = checkNull(postsDataObject, "before");
            String after = checkNull(postsDataObject, "after");
            redditPosts.setBefore(before);
            redditPosts.setAfter(after);

            // Retrieve posts
            List<Post> posts = deserializePosts(postsDataObject);
            redditPosts.setPosts(posts);

        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }

        return redditPosts;
    }

    private List<Comment> deserializeComments(JsonObject commentsRootObject) {
        // Move down through JSON nested properties
        JsonObject commentsDataObject = commentsRootObject.get("data").getAsJsonObject();
        JsonArray commentsChildrenArray = commentsDataObject.get("children").getAsJsonArray();

        // Iterate through all comments retrieving relevant information and populating POJOs
        List<Comment> commentList = new ArrayList<>();
        for (JsonElement commentElement : commentsChildrenArray) {
            JsonObject commentObject = commentElement.getAsJsonObject();

            String kind = checkNull(commentObject, "kind");
            if (Objects.equals(kind, "more")) break; // Last comment processed - we can exit

            JsonObject commentDataObject = commentObject.get("data").getAsJsonObject();
            int score = commentDataObject.get("score").getAsInt();
            int created = commentDataObject.get("created").getAsInt();
            String author = checkNull(commentDataObject, "author");
            String body = checkNull(commentDataObject, "body");

            Comment comment = new Comment(score, created, author, body, mCategory);
            commentList.add(comment);
        }

        return commentList;
    }

    private List<Post> deserializePosts(JsonObject postsDataObject) {
        JsonArray postsChildrenArray = postsDataObject.get("children").getAsJsonArray();

        // Iterate through all posts retrieving relevant information and populating POJOs
        List<Post> postList = new ArrayList<>();
        for (JsonElement postElement : postsChildrenArray) {
            JsonObject postObject = postElement.getAsJsonObject();

            JsonObject postDataObject = postObject.get("data").getAsJsonObject();
            boolean isOver18 = postDataObject.get("over_18").getAsBoolean();
            if (isOver18) continue; // Do not show adult content

            String subreddit = checkNull(postDataObject, "subreddit");
            String title = checkNull(postDataObject, "title");
            int score = postDataObject.get("score").getAsInt();
            String thumbnail = checkNull(postDataObject, "thumbnail");
            int created = postDataObject.get("created").getAsInt();
            String author = checkNull(postDataObject, "author");
            String permalink = checkNull(postDataObject, "permalink");
            String sourceUrl = checkNull(postDataObject, "url");

            // Url for the post image is nested a few levels down
            String imageUrl;
            if (postDataObject.get("preview") == null) {
                imageUrl = null;
            } else {
                JsonObject postPreviewObject = postDataObject.get("preview").getAsJsonObject();
                JsonArray postImagesArray = postPreviewObject.get("images").getAsJsonArray();
                JsonObject postImagesFirstObject = postImagesArray.get(0).getAsJsonObject();
                JsonObject postSourceObject = postImagesFirstObject.get("source").getAsJsonObject();
                imageUrl = checkNull(postSourceObject, "url");
            }

            Post post = new Post(subreddit, title, score, thumbnail, created, author, permalink,
                    sourceUrl, imageUrl, mCategory);
            postList.add(post);
        }
        return postList;
    }

    private String checkNull(JsonObject jsonObject, String key) {
        return jsonObject.get(key).isJsonNull() ? null : jsonObject.get(key).getAsString();
    }
}