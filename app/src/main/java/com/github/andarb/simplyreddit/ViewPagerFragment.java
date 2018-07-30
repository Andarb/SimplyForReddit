package com.github.andarb.simplyreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.andarb.simplyreddit.adapters.PostAdapter;
import com.github.andarb.simplyreddit.data.Post;
import com.github.andarb.simplyreddit.data.RedditPost;
import com.github.andarb.simplyreddit.database.AppDatabase;
import com.github.andarb.simplyreddit.utils.AppExecutor;
import com.github.andarb.simplyreddit.utils.PostsViewModel;
import com.github.andarb.simplyreddit.utils.PostsViewModelFactory;
import com.github.andarb.simplyreddit.utils.RetrofitClient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A new instance of this fragment is used for each page of the ViewPager.
 * A list of most popular, hot or new posts from all subreddits is downloaded and displayed here.
 */
public class ViewPagerFragment extends Fragment {

    private static final String TAG = ViewPagerFragment.class.getSimpleName();
    private static final String ARG_PAGE = "com.github.andarb.simplyreddit.arg.PAGE";

    private String mPage;
    private PostAdapter mAdapter;
    private AppDatabase mDb;
    private Unbinder mButterknifeUnbinder;

    @BindView(R.id.posts_recycler_view)
    RecyclerView mRecyclerView;

    /* Required empty public constructor */
    public ViewPagerFragment() {

    }

    /* Create a new instance of this fragment with a viewpager page number argument*/
    public static ViewPagerFragment newInstance(String category) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PAGE, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_list, container, false);
        mButterknifeUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            // Set viewpager page that needs to be loaded
            mPage = getArguments().getString(ARG_PAGE);
        }
        Context context = getActivity();
        mDb = AppDatabase.getDatabase(context.getApplicationContext());

        // Setup recyclerview adapter
        mAdapter = new PostAdapter(context);
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        // Setup viewmodel for adapter data
        PostsViewModelFactory factory = new PostsViewModelFactory(mDb, mPage);
        PostsViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(PostsViewModel.class);
        viewModel.getPosts().observe(this, new Observer<List<Post>>() {
            @Override
            public void onChanged(@Nullable List<Post> posts) {
                mAdapter.setPosts(posts);
                mAdapter.notifyDataSetChanged();
            }
        });

        // Retrieve posts from ViewModel instead of making a network call on configuration change
        if (savedInstanceState == null) {
            retrievePosts();
        }
    }

    /* Download and parse Reddit posts */
    private void retrievePosts() {
        Call<RedditPost> getCall = RetrofitClient.getCategory(mPage);

        getCall.enqueue(new Callback<RedditPost>() {
            @Override
            public void onResponse(Call<RedditPost> call,
                                   Response<RedditPost> response) {
                if (response.isSuccessful()) {
                    final RedditPost redditPosts = response.body();

                    if (redditPosts == null) {
                        Log.w(TAG, "Failed deserializing JSON");
                        return;
                    }

                    AppExecutor.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.postDao().deleteCategory(mPage);
                        }
                    });

                    AppExecutor.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.postDao().insertAll(redditPosts.getPosts());
                        }
                    });
                } else {
                    Log.w(TAG, "Response not successful:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<RedditPost> call, Throwable t) {
                Log.w(TAG, "Response failed");
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mButterknifeUnbinder.unbind();
    }
}
