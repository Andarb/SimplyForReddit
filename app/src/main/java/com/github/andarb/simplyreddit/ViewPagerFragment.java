package com.github.andarb.simplyreddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.andarb.simplyreddit.adapters.PostAdapter;
import com.github.andarb.simplyreddit.data.RedditPosts;
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

    private int mPage;
    private Unbinder mButterknifeUnbinder;

    @BindView(R.id.posts_recycler_view)
    RecyclerView mPostsRV;

    /* Required empty public constructor */
    public ViewPagerFragment() {

    }

    /* Create a new instance of this fragment with a viewpager page number argument*/
    public static ViewPagerFragment newInstance(int page) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.post_list, container, false);
        mButterknifeUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
            retrievePosts();
        }
    }

    /* Download and parse Reddit posts */
    private void retrievePosts() {
        Call<List<RedditPosts>> getCall;
        switch (mPage) {
            case 0:
                getCall = RetrofitClient.getHot();
                break;
            case 1:
                getCall = RetrofitClient.getTop();
                break;
            case 2:
                getCall = RetrofitClient.getNew();
                break;
            default:
                getCall = RetrofitClient.getHot();
        }

        getCall.enqueue(new Callback<List<RedditPosts>>() {
            @Override
            public void onResponse(Call<List<RedditPosts>> call,
                                   Response<List<RedditPosts>> response) {
                if (response.isSuccessful()) {
                    List<RedditPosts> redditPosts = response.body();

                    if (redditPosts == null) {
                        Log.w(TAG, "Failed deserializing JSON");
                        return;
                    }

                    PostAdapter postAdapter = new PostAdapter(getActivity(), redditPosts.get(0));
                    mPostsRV.setLayoutManager(new LinearLayoutManager(getActivity(),
                            LinearLayoutManager.VERTICAL, false));
                    mPostsRV.setHasFixedSize(true);
                    mPostsRV.setAdapter(postAdapter);
                } else {
                    Log.w(TAG, "Response not successful:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<RedditPosts>> call, Throwable t) {
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
