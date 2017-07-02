package brmnt.twiterpi.instance;

import brmnt.twiterpi.JSONSharedPreferences;
import twitter4j.Status;

/**
 * @author by Bramengton
 * @date 02.07.17.
 */
public class Tweets {
    private Status $tweet;
    private boolean $favorite;
    public Tweets(Status twit, JSONSharedPreferences preferences){
        this.$tweet = twit;
        this.$favorite = preferences.searchInJSONArray(twit);
    }

    public Status getTweet(){
        return this.$tweet;
    }

    public void setFavorite(boolean val){
        this.$favorite = val;
    }

    public boolean isFavorite(){
        return this.$favorite;
    }
}
