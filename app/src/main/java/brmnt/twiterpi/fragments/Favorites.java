package brmnt.twiterpi.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import brmnt.twiterpi.*;
import brmnt.twiterpi.instance.Tweets;
import twitter4j.Status;
import twitter4j.Twitter;

import java.util.List;

/**
 * @author Bramengton on 01/07/2017.
 */
public class Favorites extends BaseFragment {
    static private Favorites instance;

    public Favorites() {
        super();
    }

    public static Favorites getInstance() {
        if (instance == null)
            instance = new Favorites();
        return instance;
    }

    @Override
    public boolean setMenuVisible() {
        return false;
    }

    @Override
    public int setFragmentLayout() {
        return R.layout.fragment_favorite;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.Favorites);
        Twitter mTwitter = TwiterApplication.getTwitterInstance();//.getTwitterInstance();
        ListView mTweets = (ListView) view.findViewById(R.id.twitts);
        mTweets.setEmptyView(view.findViewById(R.id.empty));

        final AdapterTweets mAdapter = new AdapterTweets(this.getContext());
        mTweets.setAdapter(mAdapter);

        final JSONSharedPreferences mPreferences = new JSONSharedPreferences(this.getContext());
        mAdapter.setFavoriteListener(new AdapterTweets.OnFavoriteListener() {
            @Override
            public void onFavoriteClick(Tweets item, int pos) {
                mPreferences.saveJSONArray(item);
                mAdapter.removeItem(pos);
                Log.e("Return",
                        String.format("Id=%s @%s", item.getTweet().getId(), item.getTweet().getUser().getScreenName()));
            }
        });

        SearchTweets mSearchTweets = new SearchTweets(mTwitter, SearchTweets.Job.FAVORITE, new SearchTweets.OnSearchListener() {
            @Override
            public void onFound(List<Status> result) {
                if (result != null) {
                    mAdapter.addAll(result);
                    mAdapter.notifyDataSetChanged();
                } else {
                    showToast(R.string.FailedTweets);
                }
            }
        });

        mSearchTweets.getTrySearch(mPreferences.getLongArray());
    }
}
