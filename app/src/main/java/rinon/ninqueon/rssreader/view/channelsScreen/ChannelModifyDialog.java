package rinon.ninqueon.rssreader.view.channelsScreen;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.BindedService;
import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.utils.UrlFormatter;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 26.02.2017.
 */

public final class ChannelModifyDialog extends DialogFragment
{
    private final static String ARG_URL          = "url";
    private final static String ARG_CHANNEL_ID   = "channelId";

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        System.out.println("onCreateDialog");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.channel_add_dialog, null);
        final String url = getUrl();

        final TextView textView = (TextView) view.findViewById(R.id.dialog_channel_add_url_text);

        if (url != null)
        {
            textView.setText(url);
        }

        builder.setView(view)
                .setPositiveButton(R.string.dialog_channel_button_modify,
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
                .setTitle(R.string.dialog_channel_modify_title);

        return builder.create();
    }

    private void buttonOkOnClick()
    {
        final TextView textView = (TextView) getDialog().findViewById(R.id.dialog_channel_add_url_text);
        final String url = textView.getText().toString();
        final long channelId = getArguments().getLong(ARG_CHANNEL_ID);


        final String fixedString = UrlFormatter.formatUrl(url);

        if (UrlFormatter.isUrlValid(fixedString))
        {
            BindedService.startChannelModify(getContext(), channelId, fixedString);
            dismiss();
        }
        else
        {
            dismiss();
            sendErrorIntent();
        }
    }

    private String getUrl()
    {
        final Bundle args = getArguments();

        if (args == null)
        {
            return null;
        }

        return args.getString(ARG_URL);
    }

    static DialogFragment getChannelModifyDialog(final FeedEntry feedEntry)
    {
        final DialogFragment dialog = new ChannelModifyDialog();

        final Bundle args = new Bundle();
        args.putString(ChannelModifyDialog.ARG_URL, feedEntry.getSource());
        args.putLong(ChannelModifyDialog.ARG_CHANNEL_ID, feedEntry.getId());
        dialog.setArguments(args);

        return dialog;
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
