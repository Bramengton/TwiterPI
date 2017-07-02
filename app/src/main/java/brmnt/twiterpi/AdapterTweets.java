package brmnt.twiterpi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;
import brmnt.twiterpi.instance.Tweets;
import brmnt.twiterpi.views.MessageView;
import brmnt.twiterpi.views.PatternEditableBuilder;
import com.loopj.android.image.SmartImageView;
import twitter4j.Status;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Bramengton on 01/07/2017.
 */
public class AdapterTweets extends BaseAdapter {

    private PatternEditableBuilder.SpannableClickedListener mLinkListener;
    private OnFavoriteListener mFavoriteListener;

    public interface OnFavoriteListener{
        void onFavoriteClick(Tweets item, int position);
    }

    private List<Tweets> tweets;
    private LayoutInflater mInflater;
    private SimpleDateFormat mDateFormat;
    private JSONSharedPreferences mPreferences;

    public AdapterTweets(Context context){
        super();
        mPreferences = new JSONSharedPreferences(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        tweets = new ArrayList<>();
    }

    public void setFavoriteListener(OnFavoriteListener listener){
        this.mFavoriteListener = listener;
    }

    public void setSpannableClickedListener(PatternEditableBuilder.SpannableClickedListener listener){
        this.mLinkListener= listener;
    }

    public void setDataChanged(List<Tweets> test) {
        tweets =test;
    }

    public List<Tweets> getData() {
        return tweets;
    }

    public void removeItem(int position) {
        tweets.remove(position);
        notifyDataSetChanged();
    }

    public void cleanData() {
        tweets.clear();
        //notifyDataSetChanged();
    }

    public List<Tweets> addAll(List<Status> list) {
        for (Status s : list){
            tweets.add(new Tweets(s, mPreferences));
        }
        return tweets;
    }

    public boolean isNull() {
        return (tweets==null || tweets.isEmpty());
    }

    @Override
    public int getCount() {
        if(tweets!=null && !tweets.isEmpty()) return tweets.size();
        return 0;
    }

    @Override
    public Tweets getItem(int position) {
        int size = getCount();
        if(size > 0 && !(position >= size)) return tweets.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder;
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.twit_item, parent, false);
            mHolder = new ViewHolder(view);
            view.setTag(mHolder);
        }else
            mHolder = (ViewHolder) view.getTag();

        final Tweets item = tweets.get(position);
        if(mHolder!=null && item!=null) {
            final Status tweet = item.getTweet();
            mHolder.mAvatar.setImageUrl(tweet.getUser().getProfileImageURL());
            mHolder.mUser.setText(String.format("%s @%s", tweet.getUser().getName(), tweet.getUser().getScreenName()));
            mHolder.mMessage.setText(tweet.getText());
            mHolder.mMessage.showLinks(mLinkListener);
            mHolder.mTime.setText(mDateFormat.format(tweet.getCreatedAt()));

            mHolder.mFavorite.setChecked(item.isFavorite());
            mHolder.mFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mFavoriteListener!=null){
                        mFavoriteListener.onFavoriteClick(item, position);
                        item.setFavorite(!item.isFavorite());
                        notifyDataSetChanged();
                    }
                }
            });
        }
        return view;
    }

    public final class ViewHolder {
        SmartImageView mAvatar;
        TextView mUser;
        MessageView mMessage;
        TextView mTime;
        ToggleButton mFavorite;

        ViewHolder(final View v){
            mAvatar = (SmartImageView) v.findViewById(R.id.avatar);
            mUser = (TextView) v.findViewById(R.id.user);
            mTime = (TextView) v.findViewById(R.id.time);
            mMessage = (MessageView) v.findViewById(R.id.message);
            mFavorite = (ToggleButton) v.findViewById(R.id.buttonFav);
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

