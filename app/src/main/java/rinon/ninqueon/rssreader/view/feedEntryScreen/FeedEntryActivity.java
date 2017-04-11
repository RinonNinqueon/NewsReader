package rinon.ninqueon.rssreader.view.feedEntryScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;
import rinon.ninqueon.rssreader.services.NotificationsHelper;
import rinon.ninqueon.rssreader.utils.BundleConverter;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

public final class FeedEntryActivity extends ToolbarActivity
{
    private FeedEntryController feedEntryController;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_entry_activity);

        feedEntryController = new FeedEntryController(this, findViewById(android.R.id.content), getIntent());

        if (isFromNotification())
        {
            NotificationsHelper.clearNotification(this);
        }

        feedEntryController.onCreate();
    }

    public static Intent getStartIntent(final Context context, final FeedEntry feedEntry)
    {
        final Bundle args = BundleConverter.feedEntryToBundle(feedEntry);
        final Intent intent = new Intent(context, FeedEntryActivity.class);
        intent.putExtras(args);

        return intent;
    }

    public static void openActivity(final Context context, final FeedEntry feedEntry)
    {
        final Intent intent = getStartIntent(context, feedEntry);
        context.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        feedEntryController.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getMenuInflater().inflate(R.menu.feed_entry_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isFromNotification()
    {
        final Intent intent = getIntent();
        return NotificationsHelper.isFromNotification(intent);
    }
}
