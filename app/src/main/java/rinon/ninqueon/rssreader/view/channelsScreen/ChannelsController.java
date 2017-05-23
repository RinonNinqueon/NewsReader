package rinon.ninqueon.rssreader.view.channelsScreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.BindedService;
import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.utils.ProgressCodes;
import rinon.ninqueon.rssreader.view.channelFeedScreen.ChannelFeedActivity;
import rinon.ninqueon.rssreader.view.settingsScreen.SettingsActivity;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 23.03.2017.
 */

final class ChannelsController
{
    private final Context context;
    private BindedService bindedService;
    private final ServiceConnection serviceConnection;
    private boolean serviceIsBinded;

    private final static String LOGGER_TAG                  = ChannelsController.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    private final ChannelsModel channelsModel;
    private final ChannelsView channelsView;

    private int scrollToPosition;

    private final static String ARG_SCROLL_POSITION = "scrollToPosition";

    private final static int NO_SETTINGS_VALUE      = -1;

    ChannelsController(final Context context,
                   final View rootView)
    {
        this.context = context;

        serviceIsBinded = false;

        channelsModel = new ChannelsModel();
        channelsView = new ChannelsView(context, rootView, channelsModel.getFeedEntries(), this);

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
                logger.info("onServiceDisconnected");
                bindedService = null;
                serviceIsBinded = false;
            }
        };
    }

    final void onCreate(@Nullable final Bundle savedInstanceState)
    {
        scrollToPosition = NO_SETTINGS_VALUE;

        if (savedInstanceState != null)
        {
            loadState(savedInstanceState);
        }
    }

    private void updateList(final FeedEntry params[])
    {
        if (params == null)
        {
            return;
        }

        clearList();

        addItems(params);

        if (scrollToPosition > NO_SETTINGS_VALUE)
        {
            channelsView.setSelection(scrollToPosition);
            scrollToPosition = NO_SETTINGS_VALUE;
        }
    }

    private void clearList()
    {
        channelsModel.clearItems();
        channelsView.notifyDataSetChanged();
    }

    private void addItems(final FeedEntry feedEntries[])
    {
        channelsModel.addItems(feedEntries);
        channelsView.notifyDataSetChanged();
    }

    final void onContextItemSelected(final MenuItem item)
    {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final int currentSelectedIndex = info.position;

        if (currentSelectedIndex == ListView.INVALID_POSITION)
        {
            return;
        }

        final FeedEntry feedEntry = channelsModel.getItem(currentSelectedIndex);

        if (feedEntry == null)
        {
            return;
        }

        switch (item.getItemId())
        {
            case R.id.popup_delete:
                final Intent intent = BindedService.getChannelDeleteIntent(context, feedEntry.getId());
                bindService(intent);
                break;
            case R.id.popup_modify:
                channelsView.openChannelModifyDialog(feedEntry);
                break;
        }
    }

    final void saveState(final Bundle outState)
    {
        final int position = channelsView.getFirstVisiblePosition();

        outState.putInt(ARG_SCROLL_POSITION, position);
    }

    private void loadState(final Bundle savedInstanceState)
    {
        scrollToPosition = savedInstanceState.getInt(ARG_SCROLL_POSITION);
    }

    final void onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_news_refresh:
                channelsView.swipeStartRefreshingIcon();
                onRefresh();
                break;
            case R.id.menu_channel_add:
                channelsView.openChannelAddDialog();
                break;
            case R.id.menu_channels_settings:
                SettingsActivity.openSettings(context);
                break;
            case R.id.menu_channels_import:
                final Intent importIntent = BindedService.getChannelsImportIntent(context);
                bindService(importIntent);
                break;
            case R.id.menu_channels_export:
                final Intent exportIntent = BindedService.getChannelsExportIntent(context);
                bindService(exportIntent);
                break;
        }
    }

    //-------------------------

    final void onRefresh()
    {
        startUpdateChannels();
    }

    final void onItemClick(final int position)
    {
        final FeedEntry feedEntry = channelsModel.getItem(position);
        ChannelFeedActivity.openChannelFeed(context, feedEntry);
    }

    final void addChannelsFromService()
    {
        logger.info("addChannelsFromService");
        if (bindedService == null)
        {
            return;
        }

        final FeedEntry[] feedEntries = bindedService.getFeedEntriesResult();

        if (feedEntries != null)
        {
            updateList(feedEntries);
            if (channelsModel.isEmpty())
            {
                channelsView.showOnlyText(R.string.no_entries);
            }
            channelsView.swipeStopRefreshing();
            channelsView.swipeSetEnabled(true);
        }

        unBindService();
    }

    void updateProgressBar(final int progress, final int progressPercent)
    {
        final int textId = ProgressCodes.getProgressMessageId(progress);

        channelsView.showProgressBarLayout();
        channelsView.setProgressBar(textId, progressPercent);
    }

    final void showDialog(final int titleId, final int messageId)
    {
        channelsView.showDialog(titleId, messageId);
    }

    final void showErrorDialog(final int errorCode)
    {
        channelsView.showErrorDialog(ErrorCodes.getErrorMessageId(errorCode));
        channelsView.swipeStopRefreshing();
        channelsView.swipeSetEnabled(true);
        channelsView.hideProgressBarLayout();

        unBindService();
    }

    final void startReadChannels()
    {
        unBindService();

        channelsView.swipeStartRefreshingIcon();
        channelsView.swipeSetEnabled(false);
        final Intent intent = BindedService.getChannelsReadIntent(context);
        bindService(intent);
    }

    private void startUpdateChannels()
    {
        channelsView.swipeStartRefreshingIcon();
        channelsView.swipeSetEnabled(false);
        final Intent intent = BindedService.getChannelsUpdateIntent(context);
        bindService(intent);
    }

    final void checkAndLoadDataFromService()
    {
        logger.info("checkAndLoadDataFromService");
        if (bindedService != null)
        {
            if (bindedService.dataAvailable())
            {
                addChannelsFromService();
            }
        }
        else
        {
            if (channelsModel.isEmpty())
            {
                startReadChannels();
            }
        }
    }

    final void showErrorToast(final int errorCode)
    {
        channelsView.showErrorToast(ErrorCodes.getErrorMessageId(errorCode));
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
