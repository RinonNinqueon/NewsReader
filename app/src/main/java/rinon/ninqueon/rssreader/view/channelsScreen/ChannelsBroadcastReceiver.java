package rinon.ninqueon.rssreader.view.channelsScreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.BindedService;

/**
 * Created by Rinon Ninqueon on 03.03.2017.
 */

final class ChannelsBroadcastReceiver extends BroadcastReceiver
{
    private final Context context;
    private final ChannelsController channelsController;
    private final static String LOGGER_TAG                  = ChannelsBroadcastReceiver.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    ChannelsBroadcastReceiver(final Context context,
                              final ChannelsController channelsController)
    {
        this.context = context;
        this.channelsController = channelsController;
    }

    final void registerBroadcastReceiver()
    {
        final IntentFilter filter = new IntentFilter(BindedService.ACTION_CHANNELS_READ);
        filter.addAction(BindedService.ACTION_DOWNLOAD_STATUS);
        filter.addAction(BindedService.ACTION_CHANNEL_DELETE);
        filter.addAction(BindedService.ACTION_CHANNEL_ADD);
        filter.addAction(BindedService.ACTION_CHANNEL_MODIFY);
        filter.addAction(BindedService.ACTION_CHANNELS_IMPORT);
        filter.addAction(BindedService.ACTION_CHANNELS_EXPORT);
        filter.addAction(BindedService.ACTION_CHANNELS_UPDATE);
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

        logger.info(intent.getAction());

        if (intent.getAction().equals(BindedService.ACTION_CHANNELS_READ))
        {
            channelsController.addChannelsFromService();
        }
        if (intent.getAction().equals(BindedService.ACTION_CHANNEL_ADD))
        {
            channelsController.startReadChannels();
        }
        if (intent.getAction().equals(BindedService.ACTION_CHANNEL_MODIFY))
        {
            channelsController.startReadChannels();
        }
        if (intent.getAction().equals(BindedService.ACTION_CHANNEL_DELETE))
        {
            channelsController.startReadChannels();
        }
        if (intent.getAction().equals(BindedService.ACTION_CHANNELS_EXPORT))
        {
            channelsController.showDialog(R.string.dialog_information_title, R.string.dialog_export_success);
            channelsController.unBindService();
        }
        if (intent.getAction().equals(BindedService.ACTION_CHANNELS_IMPORT))
        {
            channelsController.showDialog(R.string.dialog_information_title, R.string.dialog_import_success);
            channelsController.startReadChannels();
        }
        if (intent.getAction().equals(BindedService.ACTION_CHANNELS_UPDATE))
        {
            channelsController.startReadChannels();
        }
        if (intent.getAction().equals(BindedService.ACTION_ERROR))
        {
            final Bundle args = intent.getExtras();
            final int errorCode = args.getInt(BindedService.EXTRA_ERROR_ID);
            channelsController.showErrorToast(errorCode);
        }
        if (intent.getAction().equals(BindedService.ACTION_CRITICAL_ERROR))
        {
            final Bundle args = intent.getExtras();
            final int errorCode = args.getInt(BindedService.EXTRA_ERROR_ID);
            channelsController.showErrorDialog(errorCode);
        }
        if (intent.getAction().equals(BindedService.ACTION_DOWNLOAD_STATUS))
        {
            final Bundle args = intent.getExtras();
            final int progress = args.getInt(BindedService.EXTRA_PROGRESS);
            final int progressPercent = args.getInt(BindedService.EXTRA_PROGRESS_PERCENT);

            channelsController.updateProgressBar(progress, progressPercent);
        }
    }
}
