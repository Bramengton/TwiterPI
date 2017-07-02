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
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;

import java.util.List;

/**
 * @author Bramengton on 01/07/2017.
 */
public class Search extends Fragment {
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
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.Search);

        final SwipyRefreshLayout mSwipyRefreshLayout = (SwipyRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipyRefreshLayout.setRefreshing(false);
        Twitter twitter = Utility.getTwitterInstance();
        mTweets = (ListView) view.findViewById(R.id.twitts);
        mTweets.setEmptyView(view.findViewById(R.id.empty));

        final Query query = new Query("@Twitter");
        final AdapterTweets adapter = new AdapterTweets(this.getContext());
        mTweets.setAdapter(adapter);

        searchTweets = new SearchTweets(twitter, SearchTweets.Job.SEARCH, new SearchTweets.OnSearchListener() {
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
                    query.setQuery("@Twitter");
                    searchTweets.cleanOldResult();
                    adapter.cleanData();
                }
                searchTweets.getTrySearch(query);
            }
        });

        //new TimeLineLoader(adapter, SwipyRefreshLayoutDirection.TOP).execute();
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
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
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
                searchTweets.cleanOldResult();
                ((AdapterTweets) mTweets.getAdapter()).cleanData();
                searchTweets.getTrySearch(new Query(search));
            }
        }
    }

    /*
    private class TimeLineLoader extends AsyncTask<Void, Void, List<Status>> {
        private AdapterTweets adapter;
        private SwipyRefreshLayoutDirection direction;
        private SwipyRefreshLayout mSwipyRefreshLayout;
        TimeLineLoader(final AdapterTweets adapter, SwipyRefreshLayout swipyRefreshLayout){
            this.adapter = adapter;
            this.mSwipyRefreshLayout =swipyRefreshLayout;
            this.direction = swipyRefreshLayout.getDirection();
        }

        @Override
        protected List<twitter4j.Status> doInBackground(Void... voids) {
            try {
                if (pages == null) {
                    pages = new Paging(1, 20);
                } else {
                    twitter4j.Status s = timeline.get(timeline.size()-1);
                    pages = new Paging();
                    switch (direction){
                        case TOP:
                            pages.setSinceId(s.getId());
                            break;
                        case BOTTOM:
                            pages.setMaxId(s.getId());
                            break;
                    }
                }
                timeline = twitter.getUserTimeline("Twitter",pages);
                return timeline;
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> result) {
            if (result != null) {
                switch (direction){
                    case TOP:
                        adapter.addAll(result);
                        break;
                    case BOTTOM:
                        result.remove(0);
                        //Удаляем дубль элемента.
                        // так как первый элемент предыдущей страницы Timeline
                        // начинается с последнего элемента первой страницы
                        adapter.addAll(result);
                        break;
                }
                adapter.notifyDataSetChanged();
            } else {
                showToast("Не удалось получить список");
            }
            mSwipyRefreshLayout.setRefreshing(false);
        }
    }
    */
}
