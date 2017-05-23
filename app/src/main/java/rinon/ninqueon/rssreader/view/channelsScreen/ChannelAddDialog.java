package rinon.ninqueon.rssreader.view.channelsScreen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.BindedService;
import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.utils.UrlFormatter;

/**
 * Created by Rinon Ninqueon on 26.02.2017.
 */

public final class ChannelAddDialog extends DialogFragment
{
    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(R.layout.channel_add_dialog)
                .setPositiveButton(R.string.dialog_channel_button_add,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(final DialogInterface dialog, final int whichButton)
                            {
                                buttonOkOnClick();
                            }
                        }
                )
                .setNegativeButton(R.string.dialog_channel_button_cancel,
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(final DialogInterface dialog, final int whichButton)
                            {
                                onCancel(dialog);
                            }
                        }
                )
                .setTitle(R.string.dialog_channel_add_title);

        return builder.create();
    }

    private void buttonOkOnClick()
    {
        final TextView urlText = (TextView) getDialog().findViewById(R.id.dialog_channel_add_url_text);
        final String url = urlText.getText().toString();
        final String fixedString = UrlFormatter.formatUrl(url);

        if (UrlFormatter.isUrlValid(fixedString))
        {
            BindedService.startAddChannel(getContext(), fixedString);
            dismiss();
        }
        else
        {
            dismiss();
            sendErrorIntent();
        }
    }

    private void sendErrorIntent()
    {
        final Intent intent = new Intent();
        final Bundle args = new Bundle();
        args.putInt(BindedService.EXTRA_ERROR_ID, ErrorCodes.ERROR_WRONG_URL);
        intent.putExtras(args);

        intent.setAction(BindedService.ACTION_CRITICAL_ERROR);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
}
