package rinon.ninqueon.rssreader.view.drawer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.NotificationsHelper;
import rinon.ninqueon.rssreader.view.channelsScreen.ChannelsFragment;
import rinon.ninqueon.rssreader.view.newsScreen.NewsFragment;
import rinon.ninqueon.rssreader.view.settingsScreen.SettingsFragment;

/**
 * Created by Rinon Ninqueon on 22.03.2017.
 */

final class DrawerController
{
    private final Context context;
    private final DrawerView drawerView;
    private int menuPosition;

    private final static String TAG                     = "DrawerActivity";
    private final static String APP_PREFERENCES         = TAG + ".State";
    private final static String ARG_SCREEN              = "screen";

    private final static String EXTRA_IS_UPDATE         = "update";
    private final static boolean TAG_IS_UPDATING        = true;
    private final static boolean TAG_IS_NOT_UPDATING    = false;

    private final static int DEFAULT_START_SCREEN   = 0;
    private final static int NO_MENU_POSITION       = -1;
    private final static int menuNews        = 0;
    private final static int menuChannels    = 1;
    private final static int menuSettings    = 2;

    DrawerController(final Context context,
                     final View rootView)
    {
        this.context = context;
        final String[] drawerModel = context.getResources().getStringArray(R.array.menu_items);
        drawerView = new DrawerView(context, rootView, drawerModel, this);
    }

    final void onCreate()
    {
        menuPosition = NO_MENU_POSITION;
    }

    private void selectItem(final Context context, final int menuPosition)
    {
        if (this.menuPosition == menuPosition)
        {
            return;
        }

        this.menuPosition = menuPosition;

        switch (menuPosition)
        {
            case menuNews:
                NewsFragment.loadNewsFragment((DrawerActivity)context, R.id.content_frame);
                break;
            case menuChannels:
                ChannelsFragment.loadChannelsFragment((DrawerActivity)context, R.id.content_frame);
                break;
            case menuSettings:
                SettingsFragment.loadSettingsFragment((DrawerActivity)context, R.id.content_frame, false);
                break;
        }

        ((DrawerActivity)context).invalidateOptionsMenu();
        saveState(context);

        drawerView.closeDrawers();
        drawerView.setItemChecked(menuPosition, true);
    }

    final void saveState(final Context context)
    {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ARG_SCREEN, menuPosition);
        editor.apply();
    }

    private void loadState(final Context context)
    {
        int menuPosition = DEFAULT_START_SCREEN;
        final SharedPreferences sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(ARG_SCREEN))
        {
            menuPosition = sharedPreferences.getInt(ARG_SCREEN, DEFAULT_START_SCREEN);
        }

        if (menuPosition == NO_MENU_POSITION)
        {
            menuPosition = DEFAULT_START_SCREEN;
        }

        selectItem(context, menuPosition);
    }

    final void onSaveInstanceState(final Bundle outState)
    {
        outState.putBoolean(EXTRA_IS_UPDATE, TAG_IS_UPDATING);
    }

    final void loadStartScreen(final Context context, final Bundle savedInstanceState, boolean isFromNotification)
    {
        if (isFromNotification)
        {
            NotificationsHelper.clearNotification(context);
            selectItem(context, DEFAULT_START_SCREEN);
        }
        else if (savedInstanceState != null)
        {
            final boolean updateState = savedInstanceState.getBoolean(EXTRA_IS_UPDATE);
            if (updateState == TAG_IS_NOT_UPDATING)
            {
                loadState(context);
            }
        }
        else
        {
            loadState(context);
        }
    }

    final void onItemClick(final int position)
    {
        selectItem(context, position);
    }

    final void syncState()
    {
        drawerView.syncState();
    }

    final void onConfigurationChanged(final Configuration newConfig)
    {
        drawerView.onConfigurationChanged(newConfig);
    }
}
