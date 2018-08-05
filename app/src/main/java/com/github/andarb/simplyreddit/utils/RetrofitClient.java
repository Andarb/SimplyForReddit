package com.github.andarb.simplyreddit.utils;

import com.github.andarb.simplyreddit.MainActivity;
import com.github.andarb.simplyreddit.data.RedditPosts;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * This class setups `Retrofit` to communicate with `Reddit` API.
 */
public final class RetrofitClient {

    // URL details of the API
    private static final String BASE_URL = "https://www.reddit.com";
    private static final String RETURN_FORMAT = ".json";

    private static final String CATEGORY_PATH = "category";
    private static final String CATEGORY_PATH_MASK = "/r/all/{category}/" + RETURN_FORMAT;

    private static final String SUBREDDIT_PATH = "subreddit_name";
    private static final String SUBREDDIT_PATH_MASK = "/r/{subreddit_name}/" + RETURN_FORMAT;

    private static final String POST_PATH = "post_name";
    private static final String POST_PATH_MASK = "{post_name}" + RETURN_FORMAT;

    /* Retrofit interface for retrieving posts */
    private interface RedditApi {
        @GET(CATEGORY_PATH_MASK)
        Call<RedditPosts> getCategoryPosts(@Path(CATEGORY_PATH) String category,
                                           @Query("after") String after);

        @GET(SUBREDDIT_PATH_MASK)
        Call<RedditPosts> getSubredditPosts(@Path(SUBREDDIT_PATH) String subreddit,
                                            @Query("after") String after);

        @GET(POST_PATH_MASK)
        Call<RedditPosts> getPostDetails(@Path(value = POST_PATH, encoded = true) String post);
    }

    /* Set up retrofit and its service */
    private static RedditApi setupRetrofit(String category) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(RedditPosts.class, new PostDeserializer(category))
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(RedditApi.class);
    }

    /* Retrieve a chosen category of posts */
    public static Call<RedditPosts> getCategory(String category, String nextBatch) {
        RedditApi apiService = setupRetrofit(category);

        if (Arrays.asList(MainActivity.PAGES).indexOf(category) != -1) {
            // If it matches one of the thee categories (hot, top, new), return it
            return apiService.getCategoryPosts(category.toLowerCase(), nextBatch);
        } else if (category.contains("/r")) {
            // URL for a post starts with an "/r"
            return apiService.getPostDetails(category);
        } else {
            // Otherwise, it's just a name for a subreddit
            return apiService.getSubredditPosts(category, nextBatch);
        }
    }
}


