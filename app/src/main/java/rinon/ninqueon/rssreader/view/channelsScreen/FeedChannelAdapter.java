package rinon.ninqueon.rssreader.view.channelsScreen;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import rinon.ninqueon.rssreader.R;
import rinon.ninqueon.rssreader.services.SharedPreferencesHelper;
import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

final class FeedChannelAdapter extends ArrayAdapter<FeedEntry>
{
    private final ArrayList<FeedEntry> feedEntries;


    FeedChannelAdapter(final Context context, final ArrayList<FeedEntry> objects)
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
            view = LayoutInflater.from(getContext()).inflate(R.layout.channel_entry_fragment, parent, false);
        }

        final FeedEntry feedEntry = getItem(position);
        if (feedEntry == null)
        {
            return view;
        }

        final TextView titleView = (TextView) view.findViewById(R.id.channel_title);
        final TextView descriptionView = (TextView) view.findViewById(R.id.channel_description);
        final TextView linkView = (TextView) view.findViewById(R.id.channel_link);
        final TextView dateView = (TextView) view.findViewById(R.id.channel_pubdate);
        final ImageView imageView = (ImageView) view.findViewById(R.id.channel_error_image);

        final String title = feedEntry.getTitle();
        final String description = feedEntry.getDescription();
        final String link = feedEntry.getChannelLink();
        final String date = feedEntry.getStringDate();
        final int errorCode = feedEntry.getError();

        if (title != null)
        {
            titleView.setText(title);
        }
        else
        {
            titleView.setVisibility(View.INVISIBLE);
        }

        if (description != null)
        {
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


        final String errorText = getContext().getString(ErrorCodes.getErrorMessageId(errorCode));
        imageView.setContentDescription(errorText);

        boolean theme = SharedPreferencesHelper.getThemeType(getContext());
        if (errorCode == ErrorCodes.ERROR_NO_ERROR)
        {
            if (SharedPreferencesHelper.THEME_LIGHT == theme)
            {
                final Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_action_ok_light);
                imageView.setImageDrawable(icon);
            }
            if (SharedPreferencesHelper.THEME_DARK == theme)
            {
                final Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_action_ok_dark);
                imageView.setImageDrawable(icon);
            }
        }
        else if (errorCode == ErrorCodes.ERROR_UNCHECKED_CHANNEL)
        {
            if (SharedPreferencesHelper.THEME_LIGHT == theme)
            {
                final Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_action_unchecked_light);
                imageView.setImageDrawable(icon);
            }
            if (SharedPreferencesHelper.THEME_DARK == theme)
            {
                final Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_action_unchecked_dark);
                imageView.setImageDrawable(icon);
            }
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setText(errorText);
        }
        else
        {
            if (SharedPreferencesHelper.THEME_LIGHT == theme)
            {
                final Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_action_error_light);
                imageView.setImageDrawable(icon);
            }
            if (SharedPreferencesHelper.THEME_DARK == theme)
            {
                final Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_action_error_dark);
                imageView.setImageDrawable(icon);
            }
            descriptionView.setVisibility(View.VISIBLE);
            descriptionView.setText(errorText);
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
