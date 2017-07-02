package brmnt.twiterpi.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import brmnt.twiterpi.AdapterTweets;
import brmnt.twiterpi.JSONSharedPreferences;
import brmnt.twiterpi.R;
import brmnt.twiterpi.Utility;
import brmnt.twiterpi.instance.Tweets;
import twitter4j.*;

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

    public int inDrawerItem() {
        return 0;
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
        getActivity().setTitle(R.string.Nearby);
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

        SearchTweets(preferences.getLongArray(), adapter);
    }

    private void SearchTweets(final long[] array, final AdapterTweets adapter) {
        //query.count(20);
        AsyncTask<Void, Void, List<Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
            //Этим движением я подгружаю НИЗ то что было до
            @Override
            protected List<twitter4j.Status> doInBackground(Void... params) {
                try {
                    return twitter.lookup(array);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<twitter4j.Status> result) {
                if (result != null) {
                    adapter.addAll(result);
                    adapter.notifyDataSetChanged();
                } else {
                    showToast("Не удалось получить список");
                }
            }
        };
        task.execute();
    }

    private void showToast(String text) {
        Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
    }
}
