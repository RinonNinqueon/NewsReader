package rinon.ninqueon.rssreader.view.newsScreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.database.DataBaseHelper;
import rinon.ninqueon.rssreader.services.BindedService;
import rinon.ninqueon.rssreader.services.SharedPreferencesHelper;
//import rinon.ninqueon.rssreader.services.BackgroundUpdateIntentService;
import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.utils.ProgressCodes;
import rinon.ninqueon.rssreader.view.feedEntryScreen.FeedEntryActivity;
import rinon.ninqueon.rssreader.view.settingsScreen.SettingsActivity;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 21.03.2017.
 */

final class NewsController
{
    private final Context context;
    private BindedService bindedService;
    private final ServiceConnection serviceConnection;
    private boolean serviceIsBinded;

    private final static String LOGGER_TAG                  = NewsController.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    private final NewsModel newsModel;
    private final NewsView newsView;

    private boolean updating;
    private boolean noMoreEntries;
    private int scrollToPosition;
    private int loadItemCount;

    private final static String ARG_TOTAL_COUNT     = "totalCount";
    private final static String ARG_SCROLL_POSITION = "scrollToPosition";

    private final static int SCROLL_THRESHOLD       = 2;
    private final static int NO_SETTINGS_VALUE      = -1;

    NewsController(final Context context,
                   final View rootView)
    {
        this.context = context;
        serviceIsBinded = false;

        newsModel = new NewsModel();
        newsView = new NewsView(context, rootView, newsModel.getFeedEntries(), this);

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
        loadItemCount = NO_SETTINGS_VALUE;

        if (savedInstanceState != null)
        {
            loadState(savedInstanceState);
        }
    }

    private void onStartUpdate()
    {
        setUpdating(false);
        startReadItems();

        final boolean settings_update_on_start = SharedPreferencesHelper.getSharedBooleanId(context, R.string.settings_update_on_start, R.bool.settings_update_on_start_default);
        if (settings_update_on_start)
        {
            startUpdate();
        }
    }

    //---------

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
            final long currentItemCount = newsModel.getListSize();

            if (currentItemCount >= loadItemCount)
            {
                newsView.setSelection(scrollToPosition);
                scrollToPosition = NO_SETTINGS_VALUE;
                loadItemCount = NO_SETTINGS_VALUE;
            }
        }
    }

    private void clearList()
    {
        newsModel.clearItems();
        newsView.notifyDataSetChanged();
        setNoMoreEntries(false);
    }

    private void addItems(final FeedEntry feedEntries[])
    {
        newsModel.addItems(feedEntries);
        newsView.notifyDataSetChanged();
    }

    final void onRefresh()
    {
        startUpdate();
    }

    final void onScroll(final int firstVisibleItem, final int visibleItemCount, final int totalItemCount)
    {
        if (((visibleItemCount + firstVisibleItem) >= totalItemCount - SCROLL_THRESHOLD) && (visibleItemCount < totalItemCount))
        {
            if (!isUpdating() && !isNoMoreEntries())
            {
                setUpdating(true);
                final Intent intent = BindedService.getItemsReadIntent(context, totalItemCount, DataBaseHelper.DEFAULT_LIMIT);
                bindService(intent);
            }
        }
    }

    final void onItemClick(final int position)
    {
        FeedEntry feedEntry = newsModel.getItem(position);
        loadFeedEntryActivity(feedEntry);
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
        final int count = newsModel.getListSize();
        final int position = newsView.getFirstVisiblePosition();

        outState.putInt(ARG_SCROLL_POSITION, position);
        outState.putInt(ARG_TOTAL_COUNT, count);
    }

    private void loadState(final Bundle savedInstanceState)
    {
        scrollToPosition = savedInstanceState.getInt(ARG_SCROLL_POSITION);
        loadItemCount = savedInstanceState.getInt(ARG_TOTAL_COUNT);
    }

    private void loadFeedEntryActivity(final FeedEntry feedEntry)
    {
        Intent intent = FeedEntryActivity.getStartIntent(context, feedEntry);
        context.startActivity(intent);
    }

    final void onOptionsItemSelected(final int menuItemId)
    {
        switch (menuItemId)
        {
            case R.id.menu_news_refresh:
                newsView.swipeStartRefreshingIcon();
                onRefresh();
                break;
            case R.id.menu_news_settings:
                SettingsActivity.openSettings(context);
                break;
        }
    }

    //---------SERVICE---------

    private void startUpdate()
    {
        logger.info("startUpdate");
        final Intent intent = BindedService.getUpdateIntent(context);
        bindService(intent);
    }

    final void addItemsFromService()
    {
        if (bindedService == null)
        {
            logger.severe("Service is null!");
            return;
        }

        final FeedEntry[] feedEntries = bindedService.getFeedEntriesResult();
        if (feedEntries != null)
        {
            final boolean isUpdating = isUpdating();
            updateList(feedEntries);        //Тут меняется isListUpdating
            if (!isUpdating && isNoMoreEntries())
            {
                newsView.showOnlyText(R.string.no_entries);
            }
            newsView.swipeStopRefreshing();
            newsView.swipeSetEnabled(true);
        }

        unBindService();
    }

    void updateProgressBar(final int progress, final int progressPercent)
    {
        final int textId = ProgressCodes.getProgressMessageId(progress);

        newsView.showProgressBarLayout();
        newsView.setProgressBar(textId, progressPercent);
    }

    void displayErrorToast(final int errorCode)
    {
        newsView.showErrorToast(ErrorCodes.getErrorMessageId(errorCode));
        newsView.swipeStopRefreshing();
    }

    final void startReadItems()
    {
        unBindService();

        newsView.swipeStartRefreshingIcon();
        newsView.swipeSetEnabled(false);
        setUpdating(false);
        if (loadItemCount == NO_SETTINGS_VALUE)
        {
            final Intent intent = BindedService.getItemsReadIntent(context, DataBaseHelper.DEFAULT_OFFSET, DataBaseHelper.DEFAULT_LIMIT);
            bindService(intent);
        }
        else
        {
            {
                final Intent intent = BindedService.getItemsReadIntent(context, DataBaseHelper.DEFAULT_OFFSET, loadItemCount);
                bindService(intent);
            }
        }
    }

    private void bindService(final Intent intent)
    {
        logger.info("bindService " + intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    final void unBindService()
    {
        if (serviceIsBinded || bindedService != null)
        {
            logger.info("Call unbindService");
            context.unbindService(serviceConnection);
            //onServiceDisconnected зовётся не всегда
            bindedService = null;
            serviceIsBinded = false;
        }
    }

    final void checkAndLoadDataFromService()
    {
        logger.info("checkAndLoadDataFromService");
        if (bindedService != null)
        {
            if (bindedService.dataAvailable())
            {
                addItemsFromService();
            }
        }
        else
        {
            if (!isNoMoreEntries() && newsModel.isEmpty())
            {
                onStartUpdate();
            }
        }
    }

    final void showErrorDialog(final int errorCode)
    {
        newsView.showErrorDialog(ErrorCodes.getErrorMessageId(errorCode));
        newsView.swipeStopRefreshing();
        newsView.swipeSetEnabled(true);
        newsView.hideProgressBarLayout();

        unBindService();
    }
}
