package brmnt.twiterpi;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import brmnt.twiterpi.fragments.Favorites;
import brmnt.twiterpi.fragments.Nearby;
import brmnt.twiterpi.fragments.Search;


/**
 * @author by Bramengton on 12.06.16.
 */
public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final int FRAME_CONTENT_ID = R.id.fragment_content;
    private static final int TOOLBAR_ID = R.id.toolbar;
    private static final int DRAWER_ID =R.id.drawer_layout;
    private static final int NAVIGATION_ID = R.id.nav_view;

    private Toolbar mActionBarToolbar;
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Menu mNavigationMenu;

    private static final long DRAWER_CLOSE_DELAY_MS = 350;

    private static int sNavDrawerItem = -1;

    //========================== Drawer Listener
    private OnDrawerActionListener mDrawerOpenListener;

    public interface OnDrawerActionListener{
        void onDrawerOpen();
        void onDrawerClose();
    }

    protected void setDrawerActionListener(OnDrawerActionListener listener) {
        mDrawerOpenListener = listener;
    }
    //==========================================

    /**
     * Provides the action bar instance.
     * @return the action bar.
     */
    public ActionBar getActionBarToolbar() {

        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(TOOLBAR_ID);
            setSupportActionBar(mActionBarToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(12.0f);
                //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_SHOW_TITLE);
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE
                        | ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        return getSupportActionBar();
    }

    public Toolbar getToolbar() {
        return mActionBarToolbar;
    }

    public void setDrawerEnabled(boolean enable) {
        getActionBarToolbar().setDisplayHomeAsUpEnabled(enable);
        int mLockMode = enable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        mDrawerLayout.setDrawerLockMode(mLockMode);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mDrawerLayout = (DrawerLayout) findViewById(DRAWER_ID);
        mNavigationView = (NavigationView) findViewById(NAVIGATION_ID);

        if(mNavigationView !=null) {
            mNavigationMenu = mNavigationView.getMenu();
            mNavigationView.setNavigationItemSelectedListener(this);

            if (mDrawerLayout != null) {
                mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mActionBarToolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
                    @Override
                    public void onDrawerStateChanged(int newState) {
                        super.onDrawerStateChanged(newState);
                    }

                    @Override
                    public void onDrawerClosed(View view) {
                        invalidateOptionsMenu();
                        if(mDrawerOpenListener!=null) mDrawerOpenListener.onDrawerClose();
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        if (mActionBarToolbar != null) {
                            mActionBarToolbar.collapseActionView();
                            if(mDrawerOpenListener!=null) mDrawerOpenListener.onDrawerOpen();
                        }
                    }
                };
                mToggle.setDrawerIndicatorEnabled(true);
                mDrawerLayout.addDrawerListener(mToggle);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDrawerLayout!=null && mNavigationView!=null) {
            mDrawerLayout.removeDrawerListener(mToggle);
            mNavigationView.setNavigationItemSelectedListener(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle !=null) return mToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
        if(item.getItemId() == android.R.id.home){
            if (getFragmentManager().getBackStackEntryCount() > 0 ){
                getFragmentManager().popBackStack();
            } else finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSelectedItem(sNavDrawerItem);
        if(mToggle !=null) mToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(mToggle !=null) mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        doBackPressedAction();
        onNavigationItemClicked(menuItem.getItemId());
        return true;
    }

    @Override
    public void onBackPressed() {
        if(doBackPressedAction()) return;
        super.onBackPressed();
    }

    private void setSelectedItem(@IdRes int item) {
        if(mNavigationMenu!=null && sNavDrawerItem >0)
            mNavigationMenu.findItem(item).setChecked(true);
    }

    private void onNavigationItemClicked(final int itemId) {
        if(itemId == sNavDrawerItem){
            // If already selected then close drawer and do nothing
            doBackPressedAction();
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setCustomNavigate(itemId);
            }
        }, DRAWER_CLOSE_DELAY_MS);
    }

    private boolean doBackPressedAction(){
        if(mDrawerLayout !=null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    private void setCustomNavigate(final int id) {
        switch (id){
            case R.id.navSearch:
                setFragment(id, Search.getInstance()).commit();
                break;

            case R.id.navNearby:
                setFragment(id, Nearby.getInstance()).commit();
                break;

            case R.id.navFavorites:
                setFragment(id, Favorites.getInstance()).commit();
                break;
        }
    }

    @SuppressLint("CommitTransaction")
    FragmentTransaction setFragment(@IdRes int item, @NonNull Fragment fragment){
        sNavDrawerItem = item;
        setSelectedItem(item);
        return getSupportFragmentManager().beginTransaction().replace(FRAME_CONTENT_ID, fragment);
    }
}
