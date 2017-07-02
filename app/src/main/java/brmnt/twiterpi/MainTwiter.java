package brmnt.twiterpi;

import android.os.Bundle;
import brmnt.twiterpi.fragments.Search;

public class MainTwiter extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_drawer);
        getActionBarToolbar();

        setFragment(R.id.navSearch, Search.getInstance()).commit();
    }
}
