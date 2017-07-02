package brmnt.twiterpi.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import brmnt.twiterpi.*;
import brmnt.twiterpi.instance.Tweets;
import twitter4j.Status;
import twitter4j.Twitter;

import java.util.List;

/**
 * @author Bramengton on 01/07/2017.
 */
public class Favorites extends Fragment {
    static private Favorites instance;

    public Favorites() {
        super();
    }

    public static Favorites getInstance() {
        if (instance == null)
            instance = new Favorites();
        return instance;
    }

    private Twitter twitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.Favorites);
        twitter = Utility.getTwitterInstance();
        ListView tweets = (ListView) view.findViewById(R.id.twitts);
        tweets.setEmptyView(view.findViewById(R.id.empty));

        final AdapterTweets adapter = new AdapterTweets(this.getContext());
        tweets.setAdapter(adapter);

        final JSONSharedPreferences preferences = new JSONSharedPreferences(this.getContext());
        adapter.setFavoriteListener(new AdapterTweets.OnFavoriteListener() {
            @Override
            public void onFavoriteClick(Tweets item, int pos) {
                preferences.saveJSONArray(item);
                adapter.removeItem(pos);
                Log.e("Return",
                        String.format("Id=%s @%s", item.getTweet().getId(), item.getTweet().getUser().getScreenName()));
            }
        });
        SearchTweets searchTweets = new SearchTweets(twitter, SearchTweets.Job.FAVORITE, new SearchTweets.OnSearchListener() {
            @Override
            public void onFound(List<Status> result) {
                if (result != null) {
                    adapter.addAll(result);
                    adapter.notifyDataSetChanged();
                } else {
                    showToast("Не удалось получить список");
                }
            }
        });

        searchTweets.getTrySearch(preferences.getLongArray());
    }

    private void showToast(String text) {
        View view = this.getView();
        if(view!=null)
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        else
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }
}
