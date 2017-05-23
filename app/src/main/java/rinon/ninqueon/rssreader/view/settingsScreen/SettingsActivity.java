package rinon.ninqueon.rssreader.view.settingsScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

/**
 * Created by Rinon Ninqueon on 05.03.2017.
 */

public final class SettingsActivity extends ToolbarActivity
{
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        super.initToolBar(R.id.toolbar);

        setTitle(R.string.settings_title);
        SettingsFragment.loadSettingsFragment(this, R.id.content_frame, false);
    }

    public static Intent getInstance(final Context host)
    {
        return new Intent(host, SettingsActivity.class);
    }

    public static void openSettings(final Context context)
    {
        Intent intent = SettingsActivity.getInstance(context);
        context.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return (true);
        }

        return super.onOptionsItemSelected(item);
    }
}
