package rinon.ninqueon.rssreader.view.feedEntryScreen;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.view.ToolbarActivity;

/**
 * Created by Rinon Ninqueon on 22.03.2017.
 */

final class FeedEntryView
{
    private final Context context;
    private final TextView titleView;
    private final TextView descriptionView;
    private final TextView linkView;
    private final TextView dateView;

    FeedEntryView(final Context context,
                  final View rootView)
    {
        this.context = context;
        titleView = (TextView) rootView.findViewById(R.id.item_title);
        descriptionView = (TextView) rootView.findViewById(R.id.item_description);
        linkView = (TextView) rootView.findViewById(R.id.item_link);
        dateView = (TextView) rootView.findViewById(R.id.item_pubdate);

        ((ToolbarActivity)context).initToolBar(R.id.toolbar);
    }

    final void setTitle(final String text)
    {
        if (text != null)
        {
            titleView.setText(text);
        }
        else
        {
            titleView.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressWarnings("deprecation")
    final void setDescription(final String text)
    {
        if (text != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                descriptionView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
            }
            else
            {
                descriptionView.setText(Html.fromHtml(text));
            }
        }
        else
        {
            descriptionView.setVisibility(View.INVISIBLE);
        }
    }

    final void setLink(final String text)
    {
        if (text != null)
        {
            linkView.setText(text);
        }
        else
        {
            linkView.setVisibility(View.INVISIBLE);
        }
    }

    final void setDate(final String text)
    {
        if (text != null)
        {
            dateView.setText(text);
        }
        else
        {
            dateView.setVisibility(View.INVISIBLE);
        }
    }

    final void setToolbarTitle(final String  textId)
    {
        ((ToolbarActivity)context).setTitle(textId);
    }
}
