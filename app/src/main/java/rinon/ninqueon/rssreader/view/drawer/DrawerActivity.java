package rinon.ninqueon.rssreader.view.drawer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.NotificationsHelper;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

/**
 * Created by Rinon Ninqueon on 26.02.2017.
 */

public final class DrawerActivity extends ToolbarActivity
{
    private DrawerController drawerController;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        Logger.getLogger(DrawerActivity.class.getName()).info("onCreate...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_activity);

        drawerController = new DrawerController(this, findViewById(android.R.id.content));
        drawerController.onCreate();
        drawerController.loadStartScreen(this, savedInstanceState, isFromNotification());
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        drawerController.syncState();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerController.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause()
    {
        Logger.getLogger(DrawerActivity.class.getName()).info("onPause...");
        super.onPause();
        drawerController.saveState(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Logger.getLogger(DrawerActivity.class.getName()).info("onSaveInstanceState...");
        super.onSaveInstanceState(outState);
        drawerController.onSaveInstanceState(outState);
    }

    private boolean isFromNotification()
    {
        final Intent intent = getIntent();
        return NotificationsHelper.isFromNotification(intent);
    }
}
