package rinon.ninqueon.rssreader.view.feedEntryScreen;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;
import rinon.ninqueon.rssreader.utils.BundleConverter;
import rinon.ninqueon.rssreader.view.ToolbarActivity;
import rinon.ninqueon.rssreader.view.settingsScreen.SettingsActivity;

/**
 * Created by Rinon Ninqueon on 22.03.2017.
 */

final class FeedEntryController
{
    private final Context context;
    private final FeedEntry feedEntryModel;
    private final FeedEntryView feedEntryView;

    FeedEntryController(final Context context,
                        final View rootView,
                        final Intent intent)
    {
        this.context = context;
        this.feedEntryModel = getFeedEntry(intent);
        this.feedEntryView = new FeedEntryView(context, rootView);
    }

    final void onCreate()
    {
        if (feedEntryModel == null)
        {
            return;
        }

        final String channelTitle = feedEntryModel.getChannelTitle();
        final String title = feedEntryModel.getTitle();
        final String description = feedEntryModel.getDescription();
        final String link = feedEntryModel.getLink();
        final String date = feedEntryModel.getStringDate();

        feedEntryView.setTitle(title);
        feedEntryView.setDescription(description);
        feedEntryView.setLink(link);
        feedEntryView.setDate(date);

        if (title != null && channelTitle != null)
        {
            feedEntryView.setToolbarTitle(channelTitle + ": " + title);
        }
        else if (title != null)
        {
            feedEntryView.setToolbarTitle(title);
        }
        else if (channelTitle != null)
        {
            feedEntryView.setToolbarTitle(channelTitle);
        }
    }

    private FeedEntry getFeedEntry(final Intent intent)
    {
        if (intent == null)
        {
            return null;
        }

        final Bundle args = intent.getExtras();

        if (args == null)
        {
            return null;
        }

        final FeedEntry[] entries = BundleConverter.bundleToFeedEntries(args);

        if (entries != null && entries.length == 1)
        {
            return  entries[0];
        }

        return null;
    }

    final void onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                ((ToolbarActivity)context).onBackPressed();
                break;
            case R.id.menu_feed_entry_open_in_browser:
                openInBrowser();
                break;
            case R.id.menu_feed_entry_share:
                shareFeedEntry();
                break;
            case R.id.menu_feed_entry_settings:
                final Intent intent = SettingsActivity.getInstance(context);
                context.startActivity(intent);
                break;
        }
    }

    private Intent createShareIntent()
    {
        String message;

        final String channelTitle = feedEntryModel.getChannelTitle();
        final String title = feedEntryModel.getTitle();
        final String link = feedEntryModel.getLink();

        if (title != null)
        {
            message = title + ": ";
        }
        else if (channelTitle != null)
        {
            message = channelTitle + ": ";
        }
        else
        {
            message = "";
        }

        if (link != null)
        {
            message += " " + link;
        }

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        return intent;
    }

    private Intent createBrowserIntent()
    {
        final String link = feedEntryModel.getLink();

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        return intent;
    }

    private void shareFeedEntry()
    {
        final Intent intent = createShareIntent();
        final String shareTitle = context.getResources().getString(R.string.menu_feed_entry_share);

        final Intent chooser = Intent.createChooser(intent, shareTitle);

        if (intent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(chooser);
        }
    }

    private void openInBrowser()
    {
        final Intent intent = createBrowserIntent();
        final String openInBrowserTitle = context.getResources().getString(R.string.menu_feed_entry_open_in_browser);

        final Intent chooser = Intent.createChooser(intent, openInBrowserTitle);

        if (intent.resolveActivity(context.getPackageManager()) != null)
        {
            context.startActivity(chooser);
        }
    }
}
