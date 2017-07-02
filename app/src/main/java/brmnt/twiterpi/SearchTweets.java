package brmnt.twiterpi;

import android.os.AsyncTask;
import android.support.v7.widget.SearchView;
import twitter4j.*;

import java.util.List;

/**
 * @author by Bramengton
 * @date 02.07.17.
 */
public final class SearchTweets {

    private Twitter mTwitter;
    private QueryResult mResult = null;

    private OnSearchListener mSearchListener;
    public interface OnSearchListener{
        void onFound(List<Status> result);
    }

    private Job mJob;
    public enum Job{
        Search,
        Favorits;
    }

    public SearchTweets(final Twitter twitter, final Job init, OnSearchListener listener){
        this.mTwitter = twitter;
        this.mJob = init;
        this.mSearchListener=listener;
    }

    public void getTrySearch(Query query){
        switch (mJob){
            case Search:
                new SearchTweet(query).execute();
                break;
            case Favorits:
                break;
        }
    }

    public void cleanOldResult(){
        this.mResult=null;
    }


    private class SearchTweet extends AsyncTask<Void, Void, List<Status>> {
        private Query mQuery;
        SearchTweet(final Query query){
            this.mQuery = query;
        }

        @Override
        protected List<twitter4j.Status> doInBackground(Void... voids) {
            try {
                if(mResult != null) {
                    if (mResult.hasNext()) mResult = mTwitter.search(mResult.nextQuery());
                }else mResult = mTwitter.search(this.mQuery);
                return mResult.getTweets();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> result) {
            if(mSearchListener!=null) mSearchListener.onFound(result);
        }
    }

    public SearchView.OnQueryTextListener getSearchListener(final AdapterTweets adapter, final Query query){
        return new SearchListener(adapter, query);
    }

    private class SearchListener implements SearchView.OnQueryTextListener{
        private AdapterTweets mAdapter;
        private Query mQuery;
        public SearchListener(final AdapterTweets adapter, final Query query){
            this.mAdapter =adapter;
            this.mQuery =query;
        }

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
                this.mAdapter.cleanData();
                cleanOldResult();
                mQuery.query(search);
                getTrySearch(this.mQuery);
            }
        }
    }
}
