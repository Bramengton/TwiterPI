package brmnt.twiterpi;

import android.os.AsyncTask;
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
        SEARCH,
        FAVORITE;
    }

    public SearchTweets(final Twitter twitter, final Job init, OnSearchListener listener){
        this.mTwitter = twitter;
        this.mJob = init;
        this.mSearchListener=listener;
    }

    public void getTrySearch(Query query){
        new SearchTweet(query).execute();
    }

    public void getTrySearch(long[] array){
        new SearchTweet(array).execute();
    }

    public void cleanOldResult(){
        this.mResult=null;
    }


    private class SearchTweet extends AsyncTask<Void, Void, List<Status>> {
        private Query mQuery;
        private long[] tweetsId;
        SearchTweet(Query query){
            this.mQuery = query;
        }

        SearchTweet(long[] array){
            this.tweetsId = array;
        }

        @Override
        protected List<twitter4j.Status> doInBackground(Void... voids) {
            switch (mJob){
                case SEARCH:
                    return doNearbySearch();

                case FAVORITE:
                    return doFavorite();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> result) {
            if(mSearchListener!=null) mSearchListener.onFound(result);
        }

        private List<twitter4j.Status> doNearbySearch(){
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

        private List<twitter4j.Status> doFavorite(){
            try {
                return mTwitter.lookup(this.tweetsId);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
