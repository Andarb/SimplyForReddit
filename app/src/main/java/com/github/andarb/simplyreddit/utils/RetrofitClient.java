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

/**
 * This class setups `Retrofit` to communicate with `Reddit` API.
 */
public final class RetrofitClient {

    // URL details of the API
    private static final String BASE_URL = "https://www.reddit.com/r/";
    private static final String RETURN_FORMAT = ".json";
    private static final String NEW_POSTS_PATH = "all/new/" + RETURN_FORMAT;
    private static final String HOT_POSTS_PATH = "all/hot/" + RETURN_FORMAT;
    private static final String TOP_POSTS_PATH = "all/top/" + RETURN_FORMAT;

    /* Retrofit interface for retrieving posts */
    private interface MovieApi {
        @GET(NEW_POSTS_PATH)
        Call<RedditPosts> getNewPosts();

        @GET(HOT_POSTS_PATH)
        Call<RedditPosts> getHotPosts();

        @GET(TOP_POSTS_PATH)
        Call<RedditPosts> getTopPosts();
    }

    /* Set up retrofit and its service */
    private static MovieApi setupRetrofit() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RedditPosts.class, new RedditPostDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(MovieApi.class);
    }

    /* Retrieve latest posts from all subreddits */
    public static Call<RedditPosts> getNew() {
        MovieApi apiService = setupRetrofit();

        return apiService.getNewPosts();
    }

    /* Retrieve hottest posts from all subreddits */
    public static Call<RedditPosts> getHot() {
        MovieApi apiService = setupRetrofit();

        return apiService.getHotPosts();
    }

    /* Retrieve top posts from all subreddits */
    public static Call<RedditPosts> getTop() {
        MovieApi apiService = setupRetrofit();

        return apiService.getTopPosts();
    }

    /* Unwrap JSON and deserialize from top level `data` property down.
     * This helps prevent a conflict with another, different property that has an identical name. */
    private static class RedditPostDeserializer implements JsonDeserializer<RedditPosts> {
        @Override
        public RedditPosts deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject rootObject = json.getAsJsonObject();
            String kind = rootObject.get("kind").getAsString();

            RedditPosts posts = null;
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


