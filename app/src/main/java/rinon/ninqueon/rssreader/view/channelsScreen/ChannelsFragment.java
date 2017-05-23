package rinon.ninqueon.rssreader.view.channelsScreen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

public final class ChannelsFragment extends Fragment
{
    private ChannelsController channelsController;
    private ChannelsBroadcastReceiver channelsBroadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.channels_fragment, container, false);

        channelsController = new ChannelsController(getContext(), view);
        channelsBroadcastReceiver = new ChannelsBroadcastReceiver(getContext(), channelsController);
        channelsController.onCreate(savedInstanceState);

        final ListView listView = (ListView) view.findViewById(R.id.channels_list_view);
        registerForContextMenu(listView);

        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.menu_channels_title);
        return view;
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.channels_list_view)
        {
            getActivity().getMenuInflater().inflate(R.menu.channel_popup_menu, menu);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        channelsBroadcastReceiver.registerBroadcastReceiver();
        channelsController.checkAndLoadDataFromService();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        channelsBroadcastReceiver.unregisterBroadcastReceiver();
        channelsController.unBindService();
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item)
    {
        channelsController.onContextItemSelected(item);
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
    {
        inflater.inflate(R.menu.channels_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        channelsController.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState)
    {
        super.onSaveInstanceState(outState);
        channelsController.saveState(outState);
    }

    public static void loadChannelsFragment(final ToolbarActivity host, final int contentFrameId)
    {
        final ChannelsFragment fragment = new ChannelsFragment();
        final FragmentManager fragmentManager = host.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(contentFrameId, fragment).commit();
    }
}
