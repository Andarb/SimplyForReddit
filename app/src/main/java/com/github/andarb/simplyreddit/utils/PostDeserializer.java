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
import java.util.TimeZone;

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
            long score = commentDataObject.get("score").getAsLong();
            // convert `created` time from seconds to milliseconds
            long created = getLocalMillis(commentDataObject);
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
            long score = postDataObject.get("score").getAsLong();
            String thumbnail = checkNull(postDataObject, "thumbnail");
            long created = getLocalMillis(postDataObject);
            String author = checkNull(postDataObject, "author");
            String permalink = checkNull(postDataObject, "permalink");
            String sourceUrl = checkNull(postDataObject, "url");
            String imageUrl = getPostType(sourceUrl, postDataObject);

            Post post = new Post(subreddit, title, score, thumbnail, created, author, permalink,
                    sourceUrl, imageUrl, mCategory);
            postList.add(post);
        }
        return postList;
    }

    // Checks if the given String is empty
    private String checkNull(JsonObject jsonObject, String key) {
        return jsonObject.get(key).isJsonNull() ? null : jsonObject.get(key).getAsString();
    }

    // Converts UTC seconds into local time zone millis
    private long getLocalMillis(JsonObject jsonObject) {
        return jsonObject.get("created_utc").getAsLong() * 1000
                + TimeZone.getDefault().getRawOffset();
    }

    // Determine if the post is an image, gif or a video
    private String getPostType(String url, JsonObject dataObject) {
        if (url == null) {
            return null;
        } else if (url.contains(".gifv")) {
            // Imgur gifv links can be renamed to be gif instead
            return url.substring(0, url.length() - 1);
        } else if (url.contains(".gif")) {
            // Reddit hosted gifs will normally have a direct url
            return url;
        } else if (url.contains("gfycat.com")) {
            // Gfycat gifs have a smaller version of the gif hosted on Reddit
            if (dataObject.get("media") != null && dataObject.get("media").isJsonObject()) {
                JsonObject mediaObject = dataObject.get("media").getAsJsonObject();
                JsonObject oembedObject = mediaObject.get("oembed").getAsJsonObject();
                return checkNull(oembedObject, "thumbnail_url");
            }
        } else {
            // Regular image can be retrieved a few levels down
            if (dataObject.get("preview") == null) {
                return null;
            } else {
                JsonObject previewObject = dataObject.get("preview").getAsJsonObject();
                JsonArray imagesArray = previewObject.get("images").getAsJsonArray();
                JsonObject imagesFirstObject = imagesArray.get(0).getAsJsonObject();
                JsonObject sourceObject = imagesFirstObject.get("source").getAsJsonObject();
                return checkNull(sourceObject, "url");
            }
        }
        return null;
    }
}