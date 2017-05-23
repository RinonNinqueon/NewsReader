package rinon.ninqueon.rssreader.view.channelFeedScreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.database.DataBaseHelper;
import rinon.ninqueon.rssreader.services.BindedService;
import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.xmlfeed.Feed;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;
import rinon.ninqueon.rssreader.utils.BundleConverter;
import rinon.ninqueon.rssreader.view.ToolbarActivity;
import rinon.ninqueon.rssreader.view.feedEntryScreen.FeedEntryActivity;
import rinon.ninqueon.rssreader.view.settingsScreen.SettingsActivity;

/**
 * Created by Rinon Ninqueon on 23.03.2017.
 */

final class ChannelFeedController
{
    private final Context context;
    private BindedService bindedService;
    private final ServiceConnection serviceConnection;
    private boolean serviceIsBinded;

    private final static String LOGGER_TAG                  = ChannelFeedController.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    private final ChannelFeedModel channelFeedModel;
    private final ChannelFeedView channelFeedView;

    private boolean updating;
    private boolean noMoreEntries;
    private int scrollToPosition;
    private int loadItemCount;
    private boolean rawXml;
    private String xmlUrl;

    private final static String ARG_TOTAL_COUNT     = "totalCount";
    private final static String ARG_SCROLL_POSITION = "scrollToPosition";

    private final static int SCROLL_THRESHOLD       = 2;
    private final static int NO_SETTINGS_VALUE      = -1;

    ChannelFeedController(final Context context,
                          final View rootView,
                          final Intent intent)
    {
        this.context = context;
        rawXml = false;
        xmlUrl = null;
        serviceIsBinded = false;
        serviceConnection = new ServiceConnection()
        {

            @Override
            public void onServiceConnected(final ComponentName className, final IBinder service)
            {
                logger.info("onServiceConnected");
                final BindedService.LocalBinder binder = (BindedService.LocalBinder) service;
                bindedService = binder.getService();
                serviceIsBinded = true;
            }

            @Override
            public void onServiceDisconnected(final ComponentName arg0)
            {
                bindedService = null;
                serviceIsBinded = false;
            }
        };

        final FeedEntry channel = getFeedEntry(intent);
        if (channel != null)
        {
            channelFeedModel = new ChannelFeedModel(channel.getId(), channel.getTitle());
        }
        else
        {
            channelFeedModel = new ChannelFeedModel(FeedEntry.DEFAULT_ID, null);
        }
        channelFeedView = new ChannelFeedView(context, rootView, channelFeedModel.getFeedEntries(), this);
    }

    final void onCreate(@Nullable final Bundle savedInstanceState)
    {
        scrollToPosition = NO_SETTINGS_VALUE;
        loadItemCount = NO_SETTINGS_VALUE;

        if (savedInstanceState != null)
        {
            loadState(savedInstanceState);
        }

        final String title = channelFeedModel.getTitle();
        if (title != null)
        {
            channelFeedView.setToolbarTitle(title);
        }
        else
        {
            channelFeedView.setToolbarTitle(context.getString(R.string.menu_channels_title));
        }
    }

    private void updateList(final FeedEntry params[])
    {
        if (params == null)
        {
            return;
        }

        if (!isUpdating())
        {
            clearList();
        }
        else
        {
            setUpdating(false);
        }

        addItems(params);

        if (params.length == 0)
        {
            setNoMoreEntries(true);
            return;
        }

        if (scrollToPosition > NO_SETTINGS_VALUE)
        {
            final long currentItemCount = channelFeedModel.getListSize();

            if (currentItemCount >= loadItemCount)
            {
                channelFeedView.setSelection(scrollToPosition);
                scrollToPosition = NO_SETTINGS_VALUE;
                loadItemCount = NO_SETTINGS_VALUE;
            }
        }
    }

    private void clearList()
    {
        channelFeedModel.clearItems();
        channelFeedView.notifyDataSetChanged();
        setNoMoreEntries(false);
    }

    private void addItems(final FeedEntry feedEntries[])
    {
        channelFeedModel.addItems(feedEntries);
        channelFeedView.notifyDataSetChanged();
    }

    final void onScroll(final int firstVisibleItem, final int visibleItemCount, final int totalItemCount)
    {
        if (((visibleItemCount + firstVisibleItem) >= totalItemCount - SCROLL_THRESHOLD) && (visibleItemCount < totalItemCount))
        {
            if (!isUpdating() && !isNoMoreEntries())
            {
                setUpdating(true);
                final long channelId = channelFeedModel.getChannelId();
                final Intent intent = BindedService.getChannelItemsReadIntent(context, channelId, totalItemCount, DataBaseHelper.DEFAULT_LIMIT);
                bindService(intent);
            }
        }
    }

    final void onItemClick(final int position)
    {
        final FeedEntry feedEntry = channelFeedModel.getItem(position);
        FeedEntryActivity.openActivity(context, feedEntry);
    }

    private boolean isUpdating()
    {
        return updating;
    }

    private void setUpdating(final boolean updating)
    {
        this.updating = updating;
    }

    private boolean isNoMoreEntries()
    {
        return noMoreEntries;
    }

    private void setNoMoreEntries(boolean noMoreEntries)
    {
        this.noMoreEntries = noMoreEntries;
    }

    final void saveState(final Bundle outState)
    {
        final int count = channelFeedModel.getListSize();
        final int position = channelFeedView.getFirstVisiblePosition();

        outState.putInt(ARG_SCROLL_POSITION, position);
        outState.putInt(ARG_TOTAL_COUNT, count);
    }

    private void loadState(final Bundle savedInstanceState)
    {
        scrollToPosition = savedInstanceState.getInt(ARG_SCROLL_POSITION);
        loadItemCount = savedInstanceState.getInt(ARG_TOTAL_COUNT);
    }

    private FeedEntry getFeedEntry(final Intent intent)
    {
        logger.info("getFeedEntry");
        if (intent == null)
        {
            return null;
        }

        final String action = intent.getAction();

        if (action != null && action.equals(Intent.ACTION_VIEW))
        {
            final Uri uri = intent.getData();
            logger.info("Uri = " + uri);
            if (uri != null)
            {
                xmlUrl = uri.toString();
                final Intent loadURLIntent = BindedService.getLoadURLIntent(context, xmlUrl);
                bindService(loadURLIntent);
            }
            rawXml = true;
        }
        else
        {
            rawXml = false;
        }

        final Bundle args = intent.getExtras();

        if (args == null)
        {
            return null;
        }

        return BundleConverter.bundleToFeedEntries(args);
    }

    final void addItemsFromService()
    {
        logger.info("addItemsFromService");
        if (bindedService == null)
        {
            return;
        }

        final FeedEntry[] feedEntries = bindedService.getFeedEntriesResult();

        if (feedEntries != null)
        {
            final boolean isUpdating = isUpdating();
            updateList(feedEntries);        //Тут меняется isUpdating
            if (!isUpdating && isNoMoreEntries())
            {
                channelFeedView.showErrorToast(R.string.no_entries);
            }
        }

        unBindService();
    }

    final void addFeedFromService()
    {
        logger.info("addFeedFromService");
        if (bindedService == null)
        {
            return;
        }

        final Feed feed = bindedService.getFeedResult();

        if (feed != null)
        {
            final ArrayList<FeedEntry> result = feed.getItems();

            FeedEntry resultArray[] = new FeedEntry[result.size()];
            resultArray = result.toArray(resultArray);

            final String title = feed.getTitle();
            if (title != null)
            {
                channelFeedView.setToolbarTitle(title);
            }
            else
            {
                channelFeedView.setToolbarTitle(context.getString(R.string.menu_channels_title));
            }

            updateList(resultArray);
            setUpdating(false);
            setNoMoreEntries(true);
        }

        unBindService();
    }

    private void startReadItems()
    {
        logger.info("startReadItems");
        setUpdating(false);
        final long channelId = channelFeedModel.getChannelId();
        if (channelId != FeedEntry.DEFAULT_ID)
        {
            if (loadItemCount == NO_SETTINGS_VALUE)
            {
                final Intent intent = BindedService.getChannelItemsReadIntent(context, channelId, DataBaseHelper.DEFAULT_OFFSET, DataBaseHelper.DEFAULT_LIMIT);
                bindService(intent);
            }
            else
            {
                final Intent intent = BindedService.getChannelItemsReadIntent(context, channelId, DataBaseHelper.DEFAULT_OFFSET, loadItemCount);
                bindService(intent);
            }
        }
    }

    final void onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                ((ToolbarActivity)context).onBackPressed();
                break;
            case R.id.menu_channel_entries_settings:
                SettingsActivity.openSettings(context);
                break;
            case R.id.menu_channel_add:
                final Intent intent = BindedService.getChannelAddIntent(context, xmlUrl);
                bindService(intent);
                break;
        }
    }

    void displayErrorToast(final int errorCode)
    {
        channelFeedView.showErrorToast(ErrorCodes.getErrorMessageId(errorCode));
    }

    final void checkAndLoadDataFromService()
    {
        logger.info("checkAndLoadDataFromService");
        if (bindedService != null)
        {
            if (bindedService.dataAvailable())
            {
                if (rawXml)
                {
                    addFeedFromService();
                }
                else
                {
                    addItemsFromService();
                }
            }
        }
        else
        {
            if (!isNoMoreEntries() && channelFeedModel.isEmpty())
            {
                startReadItems();
            }
        }
    }

    final void onCreateOptionsMenu(final Menu menu)
    {
        final MenuItem menuItem = menu.findItem(R.id.menu_channel_add);
        if (menuItem == null)
        {
            return;
        }
        if (rawXml)
        {
            menuItem.setVisible(true);
        }
        else
        {
            menuItem.setVisible(false);
        }
    }

    final void onChannelAdd()
    {
        showDialog(R.string.dialog_information_title, R.string.dialog_add_success);
        unBindService();
    }

    final void showErrorDialog(final int errorCode)
    {
        channelFeedView.showErrorDialog(ErrorCodes.getErrorMessageId(errorCode));
        unBindService();
    }

    private void showDialog(final int titleId, final int messageId)
    {
        channelFeedView.showDialog(titleId, messageId);
    }

    private void bindService(final Intent intent)
    {
        logger.info("bindService " + intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    final void unBindService()
    {
        logger.info("unBindService");
        if (serviceIsBinded || bindedService != null)
        {
            logger.info("Call unbindService");
            context.unbindService(serviceConnection);
            bindedService = null;
            serviceIsBinded = false;
        }
    }
}
