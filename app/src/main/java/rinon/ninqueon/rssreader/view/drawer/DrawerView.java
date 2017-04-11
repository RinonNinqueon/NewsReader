package rinon.ninqueon.rssreader.view.drawer;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

/**
 * Created by Rinon Ninqueon on 22.03.2017.
 */

final class DrawerView
{
    private final ListView drawerList;
    private final DrawerLayout drawerLayout;
    private final ActionBarDrawerToggle drawerToggle;

    DrawerView(final Context context,
               final View rootView,
               final String menuItems[],
               final DrawerController drawerController)
    {
        drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
        drawerList = (ListView) rootView.findViewById(R.id.left_drawer);

        drawerList.setAdapter(new ArrayAdapter<>(context, R.layout.drawer_item, menuItems));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
            {
                drawerController.onItemClick(position);
            }
        });

        Toolbar toolbar = ((ToolbarActivity)context).initToolBar(R.id.toolbar);

        drawerToggle = new ActionBarDrawerToggle(((ToolbarActivity)context), drawerLayout, toolbar, R.string.menu_open, R.string.menu_close)
        {
            public void onDrawerClosed(final View view)
            {
                ((ToolbarActivity)context).invalidateOptionsMenu();
            }

            public void onDrawerOpened(final View drawerView)
            {
                ((ToolbarActivity)context).invalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
    }

    final void syncState()
    {
        drawerToggle.syncState();
    }

    final void onConfigurationChanged(final Configuration newConfig)
    {
        drawerToggle.onConfigurationChanged(newConfig);
    }

    final void closeDrawers()
    {
        drawerLayout.closeDrawers();
    }

    final void setItemChecked(final int position, final boolean value)
    {
        drawerList.setItemChecked(position, value);
    }
}
