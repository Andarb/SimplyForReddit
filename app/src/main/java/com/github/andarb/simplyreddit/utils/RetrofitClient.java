package com.github.andarb.simplyreddit.utils;

import com.github.andarb.simplyreddit.data.RedditPosts;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

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
        Call<RedditPosts> getNewPosts();

        @GET(HOT_POSTS_PATH)
        Call<RedditPosts> getHotPosts();

        @GET(TOP_POSTS_PATH)
        Call<RedditPosts> getTopPosts();

        @GET(SUBREDDIT_PATH_MASK)
        Call<RedditPosts> getSubreddit(@Path(SUBREDDIT_PATH) String subreddit);

        @GET(POST_PATH_MASK)
        Call<RedditPosts> getPost(@Path(value = POST_PATH, encoded = true) String post);
    }

    /* Set up retrofit and its service */
    private static RedditApi setupRetrofit() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RedditPosts.class, new RedditPostDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(RedditApi.class);
    }

    /* Retrieve latest posts from all subreddits */
    public static Call<RedditPosts> getNew() {
        RedditApi apiService = setupRetrofit();

        return apiService.getNewPosts();
    }

    /* Retrieve hottest posts from all subreddits */
    public static Call<RedditPosts> getHot() {
        RedditApi apiService = setupRetrofit();

        return apiService.getHotPosts();
    }

    /* Retrieve top posts from all subreddits */
    public static Call<RedditPosts> getTop() {
        RedditApi apiService = setupRetrofit();

        return apiService.getTopPosts();
    }

    /* Retrieve posts from the chosen subreddit */
    public static Call<RedditPosts> getSubreddit(String subreddit) {
        RedditApi apiService = setupRetrofit();

        return apiService.getSubreddit(subreddit);
    }

    /* Retrieve a chosen post */
    public static Call<RedditPosts> getPost(String post) {
        RedditApi apiService = setupRetrofit();

        return apiService.getPost(post);
    }

    /* Unwrap JSON and deserialize from top level `data` property down.
     * This helps prevent a conflict with another, different property that has an identical name. */
    private static class RedditPostDeserializer implements JsonDeserializer<RedditPosts> {
        @Override
        public RedditPosts deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            RedditPosts posts = null;
            JsonObject rootObject = null;

            // A list of posts will be an object, while specific post details will be an array
            if (json.isJsonObject()) {
                rootObject = json.getAsJsonObject();
            } else {
                rootObject = json.getAsJsonArray().get(0).getAsJsonObject();
            }

            String kind = rootObject.get("kind").getAsString();

            if (kind.equals("Listing")) { // We are in the right place, and can try to deserialize
                String dataJSON = rootObject.get("data").toString();

                try {
                    posts = new Gson().fromJson(dataJSON, RedditPosts.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return posts;
        }
    }
}


