package rinon.ninqueon.rssreader.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.SharedPreferencesHelper;

/**
 * Created by Rinon Ninqueon on 10.03.2017.
 */

public class ToolbarActivity extends AppCompatActivity
{
    private final RecreateHandler restartHandler;
    private final static int MESSAGE_RECREATE   = 134;
    private final static String LOGGER_TAG      = ToolbarActivity.class.getName();
    private final static Logger logger          = Logger.getLogger(LOGGER_TAG);

    public ToolbarActivity()
    {
        restartHandler = new RecreateHandler(this);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState)
    {
        applyThemeFromSettings();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        reloadThemeFromSettings();
    }

    public final Toolbar initToolBar(final int toolBarId)
    {
        final Toolbar toolbar = (Toolbar) findViewById(toolBarId);

        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        return toolbar;
    }

    private void reloadThemeFromSettings()
    {
        logger.info("reloadThemeFromSettings");
        final String settings_application_theme = SharedPreferencesHelper.getThemeFromSettings(this);
        final TypedValue outValue = new TypedValue();

        getTheme().resolveAttribute(R.attr.themeName, outValue, true);
        logger.info("Current theme: " + outValue.string);
        logger.info("Applying theme: " + settings_application_theme);

        if (settings_application_theme.equals(outValue.string))
        {
            logger.info("Do nothing...");
            return;
        }

        logger.info("Recreating...");

        Message msg = restartHandler.obtainMessage();
        msg.what = MESSAGE_RECREATE;
        restartHandler.sendMessage(msg);
    }

    private void applyThemeFromSettings()
    {
        logger.info("applyThemeFromSettings");
        final String resourceKey = getResources().getString(R.string.settings_application_theme);
        final String values[] = getResources().getStringArray(R.array.settings_application_theme_list_values);
        final String settings_application_theme = SharedPreferencesHelper.getSharedString(this, resourceKey, values[0]);

        logger.info("Applying theme: " + settings_application_theme);

        if (settings_application_theme.equals(values[0]))
        {
            setTheme(R.style.AppTheme_Light);
        }
        if (settings_application_theme.equals(values[1]))
        {
            setTheme(R.style.AppTheme_Dark);
        }
    }

    private static class RecreateHandler extends Handler
    {
        private final WeakReference<ToolbarActivity> toolbarActivityWeakReference;

        RecreateHandler(final ToolbarActivity activity)
        {
            toolbarActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(final Message msg)
        {
            if (msg == null)
            {
                return;
            }

            final ToolbarActivity activity = toolbarActivityWeakReference.get();
            if (activity != null && msg.what == MESSAGE_RECREATE)
            {
                activity.recreate();
            }
        }
    }
}
