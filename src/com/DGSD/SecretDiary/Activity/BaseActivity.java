package com.DGSD.SecretDiary.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.cyrilmottier.android.greendroid.R;
import greendroid.app.ActionBarActivity;
import greendroid.app.GDApplication;
import greendroid.util.Config;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBarHost;
import greendroid.widget.ActionBarItem;

/**
 * Created By: Daniel Grech
 * Date: 1/11/11
 * Description: An activity whose contents is completely based on the Greendroid library's 'GDActivity',
 * with the support of the Android Fragment Library
 */
public class BaseActivity extends FragmentActivity implements ActionBarActivity {
    private static final String LOG_TAG = BaseActivity.class.getSimpleName();

    private boolean mDefaultConstructorUsed = false;

    private ActionBar.Type mActionBarType;
    private ActionBarHost mActionBarHost;

    /**
     * <p>
     * Default constructor.
     * </p>
     * <p>
     * <em><strong>Note</strong>: This constructor should never be used manually.
     * In order to instantiate an Activity you should let the Android system do
     * it for you by calling startActivity(Intent)</em>
     * </p>
     */
    public BaseActivity() {
        this(ActionBar.Type.Normal);
        mDefaultConstructorUsed = true;
    }

    /**
     * <p>
     * Create a new Activity with an {@link ActionBar} of the given type.
     * </p>
     * <p>
     * <em><strong>Note</strong>: This constructor should never be used manually.
     * In order to instantiate an Activity you should let the Android system do
     * it for you by calling startActivity(Intent)</em>
     * </p>
     *
     * @param actionBarType The {@link ActionBar.Type} for this Activity
     */
    public BaseActivity(ActionBar.Type actionBarType) {
        super();
        mActionBarType = actionBarType;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        ensureLayout();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mDefaultConstructorUsed) {
            // HACK cyril: This should have been done in the default
            // constructor. Unfortunately, the getApplication() method returns
            // null there. Hence, this has to be done here.
            if (getClass().equals(getGDApplication().getHomeActivityClass())) {
                mActionBarType = ActionBar.Type.Dashboard;
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ensureLayout();
    }

    /**
     * The current {@link ActionBar.Type} of the hosted {@link ActionBar}
     *
     * @return The current {@link ActionBar.Type} of the hosted
     *         {@link ActionBar}
     */
    public ActionBar.Type getActionBarType() {
        return mActionBarType;
    }

    public int createLayout() {
        switch (mActionBarType) {
            case Dashboard:
                return R.layout.gd_content_dashboard;
            case Empty:
                return R.layout.gd_content_empty;
            case Normal:
            default:
                return R.layout.gd_content_normal;
        }
    }

    /**
     * Call this method to ensure a layout has already been inflated and
     * attached to the top-level View of this Activity.
     */
    protected void ensureLayout() {
        if (!verifyLayout()) {
            setContentView(createLayout());
        }
    }

    /**
     * Verify the given layout contains everything needed by this Activity. A
     * GDActivity, for instance, manages an {@link ActionBarHost}. As a result
     * this method will return true of the current layout contains such a
     * widget.
     *
     * @return true if the current layout fits to the current Activity widgets
     *         requirements
     */
    protected boolean verifyLayout() {
        return mActionBarHost != null;
    }

    public GDApplication getGDApplication() {
        return (GDApplication) getApplication();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        onPreContentChanged();
        onPostContentChanged();
    }

    public void onPreContentChanged() {
        mActionBarHost = (ActionBarHost) findViewById(R.id.gd_action_bar_host);
        if (mActionBarHost == null) {
            throw new RuntimeException("Your content must have an ActionBarHost whose id attribute is R.id.gd_action_bar_host");
        }
        mActionBarHost.getActionBar().setOnActionBarListener(mActionBarListener);
    }

    public void onPostContentChanged() {

        boolean titleSet = false;

        final Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra(ActionBarActivity.GD_ACTION_BAR_TITLE);
            if (title != null) {
                titleSet = true;
                setTitle(title);
            }
        }

        if (!titleSet) {
            // No title has been set via the Intent. Let's look in the
            // ActivityInfo
            try {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 0);
                if (activityInfo.labelRes != 0) {
                    setTitle(activityInfo.labelRes);
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Do nothing
            }
        }

        final int visibility = intent.getIntExtra(ActionBarActivity.GD_ACTION_BAR_VISIBILITY, View.VISIBLE);
        getGdActionBar().setVisibility(visibility);
    }

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    public ActionBar getGdActionBar() {
        ensureLayout();
        return mActionBarHost.getActionBar();
    }

    public ActionBarItem addActionBarItem(ActionBarItem item) {
        return getGdActionBar().addItem(item);
    }

    public ActionBarItem addActionBarItem(ActionBarItem item, int itemId) {
        return getGdActionBar().addItem(item, itemId);
    }

    public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType) {
        return getGdActionBar().addItem(actionBarItemType);
    }

    public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType, int itemId) {
        return getGdActionBar().addItem(actionBarItemType, itemId);
    }

    public FrameLayout getContentView() {
        ensureLayout();
        return mActionBarHost.getContentView();
    }

    /**
     * <p>
     * Set the activity content from a layout resource. The resource will be
     * inflated, adding all top-level views to the activity.
     * </p>
     * <p>
     * This method is an equivalent to setContentView(int) that automatically
     * wraps the given layout in an {@link ActionBarHost} if needed..
     * </p>
     *
     * @param resID Resource ID to be inflated.
     * @see #setActionBarContentView(View)
     * @see #setActionBarContentView(View, android.view.ViewGroup.LayoutParams)
     */
    public void setActionBarContentView(int resID) {
        final FrameLayout contentView = getContentView();
        contentView.removeAllViews();
        LayoutInflater.from(this).inflate(resID, contentView);
    }

    /**
     * <p>
     * Set the activity content to an explicit view. This view is placed
     * directly into the activity's view hierarchy. It can itself be a complex
     * view hierarchy.
     * </p>
     * <p>
     * This method is an equivalent to setContentView(View, LayoutParams) that
     * automatically wraps the given layout in an {@link ActionBarHost} if
     * needed.
     * </p>
     *
     * @param view The desired content to display.
     * @param params Layout parameters for the view.
     * @see #setActionBarContentView(View)
     * @see #setActionBarContentView(int)
     */
    public void setActionBarContentView(View view, ViewGroup.LayoutParams params) {
        final FrameLayout contentView = getContentView();
        contentView.removeAllViews();
        contentView.addView(view, params);
    }

    /**
     * <p>
     * Set the activity content to an explicit view. This view is placed
     * directly into the activity's view hierarchy. It can itself be a complex
     * view hierarchy.
     * </p>
     * <p>
     * This method is an equivalent to setContentView(View) that automatically
     * wraps the given layout in an {@link ActionBarHost} if needed.
     * </p>
     *
     * @param view The desired content to display.
     * @see #setActionBarContentView(int)
     * @see #setActionBarContentView(View, android.view.ViewGroup.LayoutParams)
     */
    public void setActionBarContentView(View view) {
        final FrameLayout contentView = getContentView();
        contentView.removeAllViews();
        contentView.addView(view);
    }

    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
        return false;
    }

    private ActionBar.OnActionBarListener mActionBarListener = new ActionBar.OnActionBarListener() {
        public void onActionBarItemClicked(int position) {
            if (position == ActionBar.OnActionBarListener.HOME_ITEM) {

                final GDApplication app = getGDApplication();
                switch (mActionBarType) {
                    case Normal:
                        final Class<?> klass = app.getHomeActivityClass();
                        if (klass != null && !klass.equals(BaseActivity.this.getClass())) {
                            if (Config.GD_INFO_LOGS_ENABLED) {
                                Log.i(LOG_TAG, "Going back to the home activity");
                            }
                            Intent homeIntent = new Intent(BaseActivity.this, klass);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeIntent);
                        }
                        break;
                    case Dashboard:
                        final Intent appIntent = app.getMainApplicationIntent();
                        if (appIntent != null) {
                            if (Config.GD_INFO_LOGS_ENABLED) {
                                Log.i(LOG_TAG, "Launching the main application Intent");
                            }
                            startActivity(appIntent);
                        }
                        break;
                }

            } else {
                if (!onHandleActionBarItemClick(getGdActionBar().getItem(position), position)) {
                    if (Config.GD_WARNING_LOGS_ENABLED) {
                        Log.w(LOG_TAG, "Click on item at position " + position + " dropped down to the floor");
                    }
                }
            }
        }
    };
}
