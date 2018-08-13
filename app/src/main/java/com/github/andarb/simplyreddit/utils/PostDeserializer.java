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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Custom JSON deserializer due to complex nested structure of the JSON returned by Reddit API.
 * This class will retrieve a list of posts, post details and comments when applicable.
 */
public class PostDeserializer implements JsonDeserializer<RedditPosts> {
    private String mCategory;
    private boolean mIsVideo = false;

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

            // Retrieve posts
            List<Post> posts = deserializePosts(postsRootObject);
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

    private List<Post> deserializePosts(JsonObject postsRootObject) {

        // Retrieve `after` for pagination
        JsonObject postsDataObject = postsRootObject.get("data").getAsJsonObject();
        String after = checkNull(postsDataObject, "after");

        JsonArray postsChildrenArray = postsDataObject.get("children").getAsJsonArray();

        // Iterate through all posts retrieving relevant information and populating POJOs
        List<Post> postList = new ArrayList<>();
        for (JsonElement postElement : postsChildrenArray) {
            JsonObject postObject = postElement.getAsJsonObject();

            JsonObject postDataObject = postObject.get("data").getAsJsonObject();
            String title = checkNull(postDataObject, "title");
            boolean isOver18 = postDataObject.get("over_18").getAsBoolean();

            // Skip NSFW content
            if (isOver18 || (title != null && title.contains("NSFW"))) continue;

            String body = checkNull(postDataObject, "selftext");
            String subreddit = checkNull(postDataObject, "subreddit");
            String thumbnail = checkNull(postDataObject, "thumbnail");
            String author = checkNull(postDataObject, "author");
            String permalink = checkNull(postDataObject, "permalink");
            String sourceUrl = checkNull(postDataObject, "url");

            String score = getShortenedScore(postDataObject);
            long created = getLocalMillis(postDataObject);
            String mediaUrl = getPostType(sourceUrl, postDataObject);

            Post post = new Post(subreddit, title, score, thumbnail, created, author, permalink,
                    sourceUrl, mediaUrl, mCategory, mIsVideo, body);
            postList.add(post);
        }
        // Set key (for retrieving next batch of posts) on the last post in the list
        postList.get(postList.size() - 1).setAfter(after);

        return postList;
    }

    // Add SI prefixes to the score
    private String getShortenedScore(JsonObject dataObject) {
        long score = dataObject.get("score").getAsLong();

        DecimalFormat df = new DecimalFormat("#.#");

        if (score / 1000000 > 1) {
            return df.format(score / 1000000.0) + "M";
        } else if (score / 1000 > 1) {
            return df.format(score / 1000.0) + "k";
        }

        return String.valueOf(score);
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

            // Imgur provides gifv links that can be renamed to be gif that we can use
        } else if (url.contains(".gifv")) {
            return url.substring(0, url.length() - 1);

            // Reddit hosted gifs will have a direct url
        } else if (url.contains(".gif")) {
            return url;

            // Gfycat gifs normally have a lighter version of the animation hosted on Reddit
        } else if (url.contains("gfycat.com")) {
            if (dataObject.get("media") != null && dataObject.get("media").isJsonObject()) {
                JsonObject mediaObject = dataObject.get("media").getAsJsonObject();
                JsonObject oembedObject = mediaObject.get("oembed").getAsJsonObject();
                return checkNull(oembedObject, "thumbnail_url");
            } else {
                mIsVideo = true;
                return url;
            }

            // If nothing else, this will either be a regular image or a video
        } else {
            if (dataObject.get("post_hint") != null) {
                if (dataObject.get("post_hint").getAsString().contains("video")) mIsVideo = true;
            }

            // Retrieve a preview image, which can serve as a thumbnail for a video,
            // or be an actual post image
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
    }
}