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
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;

import java.util.List;

/**
 * @author Bramengton on 01/07/2017.
 */
public class Search extends SearchFragment {
    static private Search instance;

    public Search() {
        super();
    }

    public static Search getInstance() {
        if (instance == null)
            instance = new Search();
        return instance;
    }

    private ListView mTweets;
    private SearchTweets mSearchTweets;

    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.Search);

        final SwipyRefreshLayout mSwipyRefreshLayout = (SwipyRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipyRefreshLayout.setRefreshing(false);
        Twitter twitter = TwiterApplication.getTwitterInstance();
        mTweets = (ListView) view.findViewById(R.id.twitts);
        mTweets.setEmptyView(view.findViewById(R.id.empty));

        final Query query = new Query("@Twitter");
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
                query.setQuery(text);

                mSearchTweets.getTrySearch(query);
            }
        });

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if(direction==SwipyRefreshLayoutDirection.TOP){
                    query.setQuery("@Twitter");
                    mSearchTweets.cleanOldResult();
                    adapter.cleanData();
                }
                mSearchTweets.getTrySearch(query);
            }
        });

        mSearchTweets.getTrySearch(query);
    }

    @Override
    public SearchListener setSearchListener() {
        return new SearchListener() {
            @Override
            void startSearch(String search) {
                if(!search.isEmpty()){
                    mSearchTweets.cleanOldResult();
                    ((AdapterTweets) mTweets.getAdapter()).cleanData();
                    mSearchTweets.getTrySearch(new Query(search));
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
