package brmnt.twiterpi;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Bramengton on 01/07/2017.
 */
public final class Utility {
    private final static String CONSUMER_KEY = "6Lreopq0QvN2Oqo6VAvfxZV4Y";
    private final static String CONSUMER_KEY_SECRET = "O35I8FRYLdFeD5j9e2ZtgX00pHH7K9W19ZCTck6zouqTBSSeiB";

    public static Twitter getTwitterInstance() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setDebugEnabled(true)
                .setApplicationOnlyAuthEnabled(true)
                .setOAuthConsumerKey(Utility.CONSUMER_KEY)
                .setOAuthConsumerSecret(Utility.CONSUMER_KEY_SECRET);

        final Twitter twitter = new TwitterFactory(builder.build()).getInstance();
        Thread thread =new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    twitter.getOAuth2Token();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return twitter;
    }
}
