package brmnt.twiterpi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import brmnt.twiterpi.instance.Tweets;
import org.json.JSONArray;
import org.json.JSONException;
import twitter4j.Status;

/**
 * @author by Bramengton
 * @date 02.07.17.
 */
public final class JSONSharedPreferences {
    private static final String NAME = "favorites";
    private static final String KEY = "favor";

    private SharedPreferences mPreferences;
    public JSONSharedPreferences(Context context){
        this.mPreferences = context.getSharedPreferences(JSONSharedPreferences.NAME, 0);
    }

    @SuppressLint("CommitPrefEdits")
    public void saveJSONArray(Tweets item) {

        SharedPreferences.Editor editor = this.mPreferences.edit();

        long twitId= item.getTweet().getId();
        JSONArray jsonArray = null;
        try {
            jsonArray = loadJSONArray(this.mPreferences);
            if(searchInArray(jsonArray, twitId))
                jsonArray = removeFromArray(jsonArray, twitId);
            else
                jsonArray.put(item.getTweet().getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonArray==null)return;

        editor.putString(JSONSharedPreferences.KEY, jsonArray.toString());
        editor.commit();
    }

    public long[] getLongArray(){
        JSONArray jsonArray = null;
        try {
            jsonArray = loadJSONArray(this.mPreferences);
            if (jsonArray.length()==0) return new long[0];
            long[] ids = new long[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); ++i) {
                ids[i] = Long.parseLong(jsonArray.get(i).toString());
            }
            return ids;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new long[0];
    }


    public boolean searchInJSONArray(Status status){
        long twitId= status.getId();
        JSONArray jsonArray = null;
        boolean result = false;
        try {
            jsonArray = loadJSONArray(this.mPreferences);
            if(jsonArray.length()>0) result = searchInArray(jsonArray, twitId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JSONArray loadJSONArray(SharedPreferences settings) throws JSONException {
        return new JSONArray(settings.getString(JSONSharedPreferences.KEY, "[]"));
    }

    private boolean searchInArray(JSONArray jsonArray, long element)throws JSONException{
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.get(i).toString().equals(String.valueOf(element))) return true;
        }
        return false;
    }

    private JSONArray removeFromArray(JSONArray jsonArray, long element) {
        JSONArray output = new JSONArray();
        int len = jsonArray.length();
        for (int i = 0; i < len; i++)   {
            try {
                if (!jsonArray.get(i).toString().equals(String.valueOf(element))) {
                        output.put(jsonArray.get(i));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return output;
    }
}
