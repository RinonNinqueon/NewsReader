package rinon.ninqueon.rssreader.view.channelFeedScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import rinon.ninqueon.rssreader.utils.BundleConverter;
import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

public final class ChannelFeedActivity extends ToolbarActivity
{
    private ChannelFeedBroadcastReceiver channelFeedBroadcastReceiver;
    private ChannelFeedController channelFeedController;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_feed_activity);

        channelFeedController = new ChannelFeedController(this, findViewById(android.R.id.content), getIntent());
        channelFeedBroadcastReceiver = new ChannelFeedBroadcastReceiver(this, channelFeedController);

        channelFeedController.onCreate(savedInstanceState);
    }

    private static Intent getInstance(final Context context, final FeedEntry feedEntry)
    {
        final Bundle args = BundleConverter.feedEntryToBundle(feedEntry);
        final Intent intent = new Intent(context, ChannelFeedActivity.class);
        intent.putExtras(args);

        return intent;
    }

    public static void openChannelFeed(final Context context, final FeedEntry feedEntry)
    {
        final Intent intent = getInstance(context, feedEntry);
        context.startActivity(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        channelFeedBroadcastReceiver.registerBroadcastReceiver();
        channelFeedController.checkAndLoadDataFromService();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        channelFeedBroadcastReceiver.unregisterBroadcastReceiver();
        channelFeedController.unBindService();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState)
    {
        super.onSaveInstanceState(outState);
        channelFeedController.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getMenuInflater().inflate(R.menu.channel_feed_menu, menu);
        channelFeedController.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        channelFeedController.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }
}
