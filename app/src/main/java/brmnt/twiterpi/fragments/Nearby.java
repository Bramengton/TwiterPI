package brmnt.twiterpi.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import brmnt.twiterpi.*;
import brmnt.twiterpi.instance.Tweets;
import brmnt.twiterpi.views.PatternEditableBuilder;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;

import java.util.List;

/**
 * @author Bramengton on 01/07/2017.
 */
public class Nearby extends SearchFragment {
    static private Nearby instance;

    public Nearby() {
        super();
    }

    public static Nearby getInstance() {
        if (instance == null)
            instance = new Nearby();
        return instance;
    }

    private ListView mTweets;
    private Query mQuery;
    private SearchTweets mSearchTweets;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.Nearby);
        final SwipyRefreshLayout mSwipyRefreshLayout = (SwipyRefreshLayout) view.findViewById(R.id.swipe_container);
        Twitter twitter = TwiterApplication.getTwitterInstance();
        mTweets = (ListView) view.findViewById(R.id.twitts);
        mTweets.setEmptyView(view.findViewById(R.id.empty));

        //Николаев
        double latitude = 46.975033;
        double longitude = 31.994583;
        final GeoLocation location = new GeoLocation(latitude, longitude);
        mQuery = new Query();
        mQuery.geoCode(location, 10, String.valueOf(Query.KILOMETERS));

        final AdapterTweets adapter = new AdapterTweets(this.getContext());
        mTweets.setAdapter(adapter);

        mSearchTweets = new SearchTweets(twitter, SearchTweets.Job.SEARCH, new SearchTweets.OnSearchListener() {
            @Override
            public void onFound(List<Status> result) {
                if (result != null) {
                    adapter.addAll(result);
                    adapter.notifyDataSetChanged();
                } else {
                    showToast(R.string.FailedTweets);
                }
                mSwipyRefreshLayout.setRefreshing(false);
            }
        });


        final JSONSharedPreferences preferences = new JSONSharedPreferences(this.getContext());
        adapter.setFavoriteListener(new AdapterTweets.OnFavoriteListener() {
            @Override
            public void onFavoriteClick(Tweets item, int pos) {
                preferences.saveJSONArray(item);
                Log.e("Return",
                        String.format("Id=%s @%s", item.getTweet().getId(), item.getTweet().getUser().getScreenName()));
            }
        });

        adapter.setSpannableClickedListener(new PatternEditableBuilder.SpannableClickedListener() {
            @Override
            public void onSpanClicked(String text) {
                mSearchTweets.cleanOldResult();
                adapter.cleanData();
                mQuery.setQuery(text);
                mSearchTweets.getTrySearch(mQuery);
            }
        });


        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if(direction==SwipyRefreshLayoutDirection.TOP){
                    mQuery.query(null);
                    mSearchTweets.cleanOldResult();
                    adapter.cleanData();
                }
                mSearchTweets.getTrySearch(mQuery);
            }
        });

        mSearchTweets.getTrySearch(mQuery);
    }

    @Override
    public SearchListener setSearchListener() {
        return new SearchListener() {
            @Override
            void startSearch(String search) {
                if(!search.isEmpty()){
                    ((AdapterTweets) mTweets.getAdapter()).cleanData();
                    mSearchTweets.cleanOldResult();
                    mQuery.query(search);
                    mSearchTweets.getTrySearch(mQuery);
                }
            }
        };
    }

    @Override
    public boolean setMenuVisible() {
        return true;
    }

    @Override
    public int setFragmentLayout() {
        return R.layout.fragment_twits;
    }
}
