package rinon.ninqueon.rssreader.view.newsScreen;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.logging.Logger;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.NotificationsHelper;

public final class NewsFragment extends Fragment
{
    private final static String LOGGER_TAG                  = NewsFragment.class.getName();
    private final static Logger logger                      = Logger.getLogger(LOGGER_TAG);

    private NewsController newsController;
    private NewsBroadcastReceiver newsBroadcastReceiver;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        logger.info("onCreateView");
        final View view = inflater.inflate(R.layout.news_fragment, container, false);

        newsController = new NewsController(getContext(), view);
        newsBroadcastReceiver = new NewsBroadcastReceiver(getContext(), newsController);

        newsController.onCreate(savedInstanceState);

        getActivity().setTitle(R.string.menu_news_title);
        setHasOptionsMenu(true);

        NotificationsHelper.clearNotification(getContext());

        return view;
    }

    @Override
    public void onResume()
    {
        logger.info("onResume");
        super.onResume();
        newsBroadcastReceiver.registerBroadcastReceiver();
        newsController.checkAndLoadDataFromService();
    }

    @Override
    public void onPause()
    {
        logger.info("onPause");
        super.onPause();
        newsBroadcastReceiver.unregisterBroadcastReceiver();
        newsController.unBindService();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
    {
        inflater.inflate(R.menu.news_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState)
    {
        super.onSaveInstanceState(outState);
        newsController.saveState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if (item != null)
        {
            newsController.onOptionsItemSelected(item.getItemId());
        }

        return super.onOptionsItemSelected(item);
    }

    public static void loadNewsFragment(final AppCompatActivity host, final int contentFrameId)
    {
        final NewsFragment fragment = new NewsFragment();
        final FragmentManager fragmentManager = host.getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(contentFrameId, fragment).commit();
    }
}
