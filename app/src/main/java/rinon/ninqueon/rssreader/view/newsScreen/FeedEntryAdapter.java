package rinon.ninqueon.rssreader.view.newsScreen;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

final class FeedEntryAdapter extends ArrayAdapter<FeedEntry>
{
    private final ArrayList<FeedEntry> feedEntries;

    private final static int MAX_DESCRIPTION_LENGTH     = 128;


    FeedEntryAdapter(final Context context, final ArrayList<FeedEntry> objects)
    {
        super(context, R.layout.feed_entry_fragment, objects);
        this.feedEntries = objects;
    }

    @SuppressWarnings("deprecation")
    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent)
    {
        View view = convertView;

        if (convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(R.layout.feed_entry_fragment, parent, false);
        }

        final FeedEntry feedEntry = getItem(position);
        if (feedEntry == null)
        {
            return view;
        }

        final TextView titleView = (TextView) view.findViewById(R.id.item_title);
        final TextView descriptionView = (TextView) view.findViewById(R.id.item_description);
        final TextView linkView = (TextView) view.findViewById(R.id.item_link);
        final TextView dateView = (TextView) view.findViewById(R.id.item_pubdate);

        final String channelTitle = feedEntry.getChannelTitle();
        final String title = feedEntry.getTitle();
        String description = feedEntry.getDescription();
        final String link = feedEntry.getLink();
        final String date = feedEntry.getStringDate();

        if (title != null && channelTitle != null)
        {
            final String entryTitle = channelTitle + ": " + title;
            titleView.setText(entryTitle);
        }
        else
        if (title != null)
        {
            titleView.setText(title);
        }
        else if (channelTitle != null)
        {
            titleView.setText(channelTitle);
        }
        else
        {
            titleView.setVisibility(View.INVISIBLE);
        }

        if (description != null)
        {
            if (description.length() > FeedEntryAdapter.MAX_DESCRIPTION_LENGTH)
            {
                description = description.substring(0, FeedEntryAdapter.MAX_DESCRIPTION_LENGTH) + "...";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                descriptionView.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY));
            }
            else
            {
                descriptionView.setText(Html.fromHtml(description));
            }
        }
        else
        {
            descriptionView.setVisibility(View.INVISIBLE);
        }

        if (link != null)
        {
            linkView.setText(link);
        }
        else
        {
            linkView.setVisibility(View.INVISIBLE);
        }

        if (date != null)
        {
            dateView.setText(date);
        }
        else
        {
            dateView.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Nullable
    @Override
    public FeedEntry getItem(final int position)
    {
        if (feedEntries == null)
        {
            return null;
        }

        if (position >= feedEntries.size())
        {
            return null;
        }

        return feedEntries.get(position);
    }

    @Override
    public int getCount()
    {
        if (feedEntries == null)
        {
            return 0;
        }

        return feedEntries.size();
    }
}
