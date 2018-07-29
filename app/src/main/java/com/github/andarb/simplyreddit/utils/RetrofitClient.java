package com.github.andarb.simplyreddit.utils;

import com.github.andarb.simplyreddit.data.RedditPost;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        Call<RedditPost> getNewPosts();

        @GET(HOT_POSTS_PATH)
        Call<RedditPost> getHotPosts();

        @GET(TOP_POSTS_PATH)
        Call<RedditPost> getTopPosts();

        @GET(SUBREDDIT_PATH_MASK)
        Call<RedditPost> getSubreddit(@Path(SUBREDDIT_PATH) String subreddit);

        @GET(POST_PATH_MASK)
        Call<RedditPost> getPost(@Path(value = POST_PATH, encoded = true) String post);
    }

    /* Set up retrofit and its service */
    private static RedditApi setupRetrofit() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(RedditPost.class, new PostDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(RedditApi.class);
    }

    /* Retrieve latest posts from all subreddits */
    public static Call<RedditPost> getNew() {
        RedditApi apiService = setupRetrofit();

        return apiService.getNewPosts();
    }

    /* Retrieve hottest posts from all subreddits */
    public static Call<RedditPost> getHot() {
        RedditApi apiService = setupRetrofit();

        return apiService.getHotPosts();
    }

    /* Retrieve top posts from all subreddits */
    public static Call<RedditPost> getTop() {
        RedditApi apiService = setupRetrofit();

        return apiService.getTopPosts();
    }

    /* Retrieve posts from the chosen subreddit */
    public static Call<RedditPost> getSubreddit(String subreddit) {
        RedditApi apiService = setupRetrofit();

        return apiService.getSubreddit(subreddit);
    }

    /* Retrieve a chosen post */
    public static Call<RedditPost> getPost(String post) {
        RedditApi apiService = setupRetrofit();

        return apiService.getPost(post);
    }
}


