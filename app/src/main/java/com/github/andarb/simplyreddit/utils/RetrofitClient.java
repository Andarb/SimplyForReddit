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
        Call<RedditPosts> getNew();

        @GET(HOT_POSTS_PATH)
        Call<RedditPosts> getHot();

        @GET(TOP_POSTS_PATH)
        Call<RedditPosts> getTop();

        @GET(SUBREDDIT_PATH_MASK)
        Call<RedditPosts> getSubreddit(@Path(SUBREDDIT_PATH) String subreddit);

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
    public static Call<RedditPosts> getCategory(String category) {
        RedditApi apiService = setupRetrofit(category);

        switch (Arrays.asList(MainActivity.PAGES).indexOf(category)) {
            case 0:
                return apiService.getHot();
            case 1:
                return apiService.getTop();
            case 2:
                return apiService.getNew();
            default:
                if (category.contains("/r")) { // then it's a url for post details
                    return apiService.getPostDetails(category);
                } else { // otherwise it's just a name for subreddit
                    return apiService.getSubreddit(category);
                }

        }
    }


}


