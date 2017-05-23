package rinon.ninqueon.rssreader.view.channelFeedScreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import rinon.ninqueon.rssreader.services.BindedService;

/**
 * Created by Rinon Ninqueon on 03.03.2017.
 */

final class ChannelFeedBroadcastReceiver extends BroadcastReceiver
{
    private final Context context;
    private final ChannelFeedController channelFeedController;

    ChannelFeedBroadcastReceiver(final Context context,
                                 final ChannelFeedController channelFeedController)
    {
        this.context = context;
        this.channelFeedController = channelFeedController;
    }


    final void registerBroadcastReceiver()
    {
        final IntentFilter filter = new IntentFilter(BindedService.ACTION_CHANNEL_ITEMS_READ);
        filter.addAction(BindedService.ACTION_LOAD_URL);
        filter.addAction(BindedService.ACTION_CHANNEL_ADD);
        filter.addAction(BindedService.ACTION_ERROR);
        filter.addAction(BindedService.ACTION_CRITICAL_ERROR);
        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
    }

    final void unregisterBroadcastReceiver()
    {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        if (intent == null)
        {
            return;
        }

        if (intent.getAction().equals(BindedService.ACTION_CHANNEL_ITEMS_READ))
        {
            channelFeedController.addItemsFromService();
        }
        if (intent.getAction().equals(BindedService.ACTION_LOAD_URL))
        {
            channelFeedController.addFeedFromService();
        }
        if (intent.getAction().equals(BindedService.ACTION_CHANNEL_ADD))
        {
            channelFeedController.onChannelAdd();
        }
        if (intent.getAction().equals(BindedService.ACTION_ERROR))
        {
            final Bundle args = intent.getExtras();
            final int errorCode = args.getInt(BindedService.EXTRA_ERROR_ID);
            channelFeedController.displayErrorToast(errorCode);
        }
        if (intent.getAction().equals(BindedService.ACTION_CRITICAL_ERROR))
        {
            final Bundle args = intent.getExtras();
            final int errorCode = args.getInt(BindedService.EXTRA_ERROR_ID);
            channelFeedController.showErrorDialog(errorCode);
        }
    }
}
