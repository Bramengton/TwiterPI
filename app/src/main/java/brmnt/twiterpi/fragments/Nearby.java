package brmnt.twiterpi.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.Toast;
import brmnt.twiterpi.*;
import brmnt.twiterpi.instance.Tweets;
import brmnt.twiterpi.views.PatternEditableBuilder;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import twitter4j.*;

import java.util.List;

/**
 * @author Bramengton on 01/07/2017.
 */
public class Nearby extends Fragment {
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
    private Query query;
    private SearchTweets searchTweets;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_twits, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.Nearby);
        final SwipyRefreshLayout mSwipyRefreshLayout = (SwipyRefreshLayout) view.findViewById(R.id.swipe_container);
        Twitter twitter = Utility.getTwitterInstance();
        mTweets = (ListView) view.findViewById(R.id.twitts);
        mTweets.setEmptyView(view.findViewById(R.id.empty));

        //Николаев
        double latitude = 46.975033;
        double longitude = 31.994583;
        final GeoLocation location = new GeoLocation(latitude, longitude);
        query = new Query();
        query.geoCode(location, 10, String.valueOf(Query.KILOMETERS));

        final AdapterTweets adapter = new AdapterTweets(this.getContext());
        mTweets.setAdapter(adapter);

        searchTweets = new SearchTweets(twitter, SearchTweets.Job.Search, new SearchTweets.OnSearchListener() {
            @Override
            public void onFound(List<Status> result) {
                if (result != null) {
                    adapter.addAll(result);
                    adapter.notifyDataSetChanged();
                } else {
                    showToast("Не удалось получить список");
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
                searchTweets.cleanOldResult();
                adapter.cleanData();
                query.setQuery(text);
                searchTweets.getTrySearch(query);
            }
        });


        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if(direction==SwipyRefreshLayoutDirection.TOP){
                    query.query(null);
                    searchTweets.cleanOldResult();
                    adapter.cleanData();
                }
                searchTweets.getTrySearch(query);
            }
        });

        searchTweets.getTrySearch(query);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_twiter, menu);
        UIListSearch(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showToast(String text) {
        View view = this.getView();
        if(view!=null)
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        else
            Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void UIListSearch(Menu menu){
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null) return;
        searchView.setIconifiedByDefault(true);
        searchView.setQueryHint(getString(android.R.string.search_go));
        searchView.setOnQueryTextListener(new SearchListener());
    }

    private class SearchListener implements SearchView.OnQueryTextListener{
        @Override
        public boolean onQueryTextSubmit(String newText){
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText){
            startSearch(newText);
            return true;
        }

        private void startSearch(String search){
            if(!search.isEmpty()){
                ((AdapterTweets) mTweets.getAdapter()).cleanData();
                searchTweets.cleanOldResult();
                query.query(search);
                searchTweets.getTrySearch(query);
            }
        }
    }
}
