package rinon.ninqueon.rssreader.view.channelsScreen;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

/**
 * Created by Rinon Ninqueon on 23.03.2017.
 */

final class ChannelsView
{
    private final Context context;

    private final ArrayAdapter<FeedEntry> adapter;
    private final ListView listView;
    private final SwipeRefreshLayout swipeRefreshLayout;

    private final View progressBarView;
    private final ProgressBar progressBar;
    private final TextView textView;

    ChannelsView(final Context context,
             final View rootView,
             final ArrayList<FeedEntry> feedEntries,
             final ChannelsController channelsController)
    {
        this.context = context;

        listView = (ListView) rootView.findViewById(R.id.channels_list_view);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.channels_swipe_refresh_layout);
        progressBarView = rootView.findViewById(R.id.download_progress_container);
        progressBar = (ProgressBar) rootView.findViewById(R.id.download_progressbar);
        textView = (TextView) rootView.findViewById(R.id.download_progress_text);

        //swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark_Light);
        //swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorPrimaryDark_Light);

        adapter = new FeedChannelAdapter(context, feedEntries);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
                channelsController.onItemClick(position);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                channelsController.onRefresh();
            }
        });
    }

    final void showOnlyText(final int textId)
    {
        progressBarView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.VISIBLE);
        textView.setText(textId);
    }

    final void showProgressBarLayout()
    {
        progressBarView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
    }

    final void hideProgressBarLayout()
    {
        progressBarView.setVisibility(View.INVISIBLE);
    }

    final void setProgressBar(final int stringId, final int progress)
    {

        if (progress >= 0)
        {
            progressBar.setProgress(progress);
        }
        else if (progress == -1)
        {
            progressBar.setProgress(100);
        }
        else
        {
            hideProgressBarLayout();
        }

        if (stringId >= 0)
        {
            String label = context.getResources().getString(stringId);
            if (progress >= 0)
            {
                label += ": " + progress + "%";
            }

            textView.setText(label);
        }
    }

    final void swipeStopRefreshing()
    {
        swipeRefreshLayout.setRefreshing(false);
    }

    final void swipeStartRefreshingIcon()
    {
        swipeRefreshLayout.setRefreshing(true);
    }

    final void swipeSetEnabled(final boolean enabled)
    {
        swipeRefreshLayout.setEnabled(enabled);
    }

    final void notifyDataSetChanged()
    {
        adapter.notifyDataSetChanged();
    }

    final void setSelection(final int position)
    {
        listView.setSelection(position);
    }

    final int getFirstVisiblePosition()
    {
        return listView.getFirstVisiblePosition();
    }

    final void showErrorToast(int messageId)
    {
        Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show();
    }

    private FragmentManager getFragmentManager()
    {
        return ((ToolbarActivity)context).getSupportFragmentManager();
    }

    final void openChannelAddDialog()
    {
        DialogFragment dialog = new ChannelAddDialog();
        dialog.show(getFragmentManager(), ChannelAddDialog.class.getName());
    }

    final void openChannelModifyDialog(final FeedEntry feedEntry)
    {
        final DialogFragment dialog = ChannelModifyDialog.getChannelModifyDialog(feedEntry);
        dialog.show(getFragmentManager(), ChannelModifyDialog.class.getName());
    }

    final void showDialog(final int titleId, final int messageId)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setNegativeButton(R.string.dialog_channel_button_close,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which)
                    {
                        dialog.cancel();
                    }
                })
                .setTitle(titleId)
                .setMessage(messageId)
                .create();

        builder.show();
    }

    final void showErrorDialog(final int messageId)
    {
        showDialog(R.string.dialog_channel_error, messageId);
    }
}
