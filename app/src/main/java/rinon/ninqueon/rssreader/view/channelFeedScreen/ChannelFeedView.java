package rinon.ninqueon.rssreader.view.channelFeedScreen;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

/**
 * Created by Rinon Ninqueon on 23.03.2017.
 */

final class ChannelFeedView
{
    private final Context context;

    private final ListView listView;
    private final ArrayAdapter<FeedEntry> adapter;

    ChannelFeedView(final Context context,
             final View rootView,
             final ArrayList<FeedEntry> feedEntries,
             final ChannelFeedController channelFeedController)
    {
        this.context = context;

        listView = (ListView) rootView.findViewById(R.id.channel_entries_list_view);

        adapter = new FeedEntryAdapter(context, feedEntries);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState)
            {

            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount)
            {
                channelFeedController.onScroll(firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
                channelFeedController.onItemClick(position);
            }
        });

        ((ToolbarActivity)context).initToolBar(R.id.toolbar);
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

    final void setToolbarTitle(final String  textId)
    {
        ((ToolbarActivity)context).setTitle(textId);
    }

    final void showErrorDialog(final int messageId)
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
                .setTitle(R.string.dialog_channel_error)
                .setMessage(messageId)
                .create();

        builder.show();
    }
}
