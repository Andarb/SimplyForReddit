package com.github.andarb.simplyreddit.utils;

import com.github.andarb.simplyreddit.data.Children;
import com.github.andarb.simplyreddit.data.Data;
import com.github.andarb.simplyreddit.data.RedditPosts;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * This class setups `Retrofit` to communicate with `Reddit` API.
 */
public final class RetrofitClient {

    // URL details of the API
    private static final String BASE_URL = "https://www.reddit.com";
    private static final String RETURN_FORMAT = ".json";

    private static final String NEW_POSTS_PATH = "/r/all/new/" + RETURN_FORMAT;
    private static final String HOT_POSTS_PATH = "/r/all/hot/" + RETURN_FORMAT;
    private static final String TOP_POSTS_PATH = "/r/all/top/" + RETURN_FORMAT;

    private static final String SUBREDDIT_PATH = "subreddit_name";
    private static final String SUBREDDIT_PATH_MASK = "/r/{subreddit_name}" + RETURN_FORMAT;

    private static final String POST_PATH = "post_name";
    private static final String POST_PATH_MASK = "{post_name}" + RETURN_FORMAT;

    /* Retrofit interface for retrieving posts */
    private interface RedditApi {
        @GET(NEW_POSTS_PATH)
        Call<List<RedditPosts>> getNewPosts();

        @GET(HOT_POSTS_PATH)
        Call<List<RedditPosts>> getHotPosts();

        @GET(TOP_POSTS_PATH)
        Call<List<RedditPosts>> getTopPosts();

        @GET(SUBREDDIT_PATH_MASK)
        Call<List<RedditPosts>> getSubreddit(@Path(SUBREDDIT_PATH) String subreddit);

        @GET(POST_PATH_MASK)
        Call<List<RedditPosts>> getPost(@Path(value = POST_PATH, encoded = true) String post);
    }

    /* Set up retrofit and its service */
    private static RedditApi setupRetrofit() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<List<RedditPosts>>() {
                }.getType(), new RedditPostDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(RedditApi.class);
    }

    /* Retrieve latest posts from all subreddits */
    public static Call<List<RedditPosts>> getNew() {
        RedditApi apiService = setupRetrofit();

        return apiService.getNewPosts();
    }

    /* Retrieve hottest posts from all subreddits */
    public static Call<List<RedditPosts>> getHot() {
        RedditApi apiService = setupRetrofit();

        return apiService.getHotPosts();
    }

    /* Retrieve top posts from all subreddits */
    public static Call<List<RedditPosts>> getTop() {
        RedditApi apiService = setupRetrofit();

        return apiService.getTopPosts();
    }

    /* Retrieve posts from the chosen subreddit */
    public static Call<List<RedditPosts>> getSubreddit(String subreddit) {
        RedditApi apiService = setupRetrofit();

        return apiService.getSubreddit(subreddit);
    }

    /* Retrieve a chosen post */
    public static Call<List<RedditPosts>> getPost(String post) {
        RedditApi apiService = setupRetrofit();

        return apiService.getPost(post);
    }

    /* Unwrap JSON and deserialize from top level `data` property down.
     * This helps prevent a conflict with another, different property that has an identical name. */
    private static class RedditPostDeserializer implements JsonDeserializer<List<RedditPosts>> {
        @Override
        public List<RedditPosts> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            RedditPosts posts = null;
            RedditPosts comments = new RedditPosts();
            List<RedditPosts> postsAndComments = new ArrayList<>();

            // If root JSON is an object, it will contain a list of posts. Otherwise it's an array
            // which will contain post details and a list of comments.
            JsonObject postsRootObject;
            if (json.isJsonObject()) {
                postsRootObject = json.getAsJsonObject();
            } else {
                // Get post details
                postsRootObject = json.getAsJsonArray().get(0).getAsJsonObject();
                // Get comments
                JsonObject commentsRootObject = json.getAsJsonArray().get(1).getAsJsonObject();

                // Move down through JSON nested properties
                JsonObject commentsDataObject = commentsRootObject.get("data").getAsJsonObject();
                JsonArray commentsChildrenArray = commentsDataObject.get("children").getAsJsonArray();

                // Iterate through all comments retrieving relevant information and populating POJOs
                List<Children> children = new ArrayList<>();
                for (JsonElement comment : commentsChildrenArray) {
                    JsonObject commentObject = comment.getAsJsonObject();

                    String kind = commentObject.get("kind").getAsString();
                    if (kind.equals("more")) break; // Last comment processed - we can exit

                    JsonObject commentDataObject = commentObject.get("data").getAsJsonObject();

                    String author = commentDataObject.get("author").getAsString();
                    String body = commentDataObject.get("body").getAsString();
                    int score = commentDataObject.get("score").getAsInt();
                    int created = commentDataObject.get("created").getAsInt();

                    Data data = new Data();
                    data.setAuthor(author);
                    data.setBody(body);
                    data.setScore(score);
                    data.setCreated(created);

                    Children child = new Children();
                    child.setData(data);

                    children.add(child);
                }

                // Recreate nested POJOs, and add the final result to the List
                Data rootData = new Data();
                rootData.setChildren(children);
                comments.setData(rootData);
                postsAndComments.add(comments);
            }

            // Retrieve posts or post details, and them to the List
            try {
                posts = new Gson().fromJson(postsRootObject.toString(), RedditPosts.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
            postsAndComments.add(0, posts);

            return postsAndComments;
        }
    }
}


