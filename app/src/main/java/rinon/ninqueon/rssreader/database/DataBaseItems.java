package rinon.ninqueon.rssreader.database;

import android.provider.BaseColumns;

/**
 * Created by Rinon Ninqueon on 23.02.2017.
 */

final class DataBaseItems
{
    private DataBaseItems()
    {
        throw new UnsupportedOperationException("DataBaseItems.Constructor");
    }

    final static class DataBaseItemsEntry implements BaseColumns
    {
        final static String TABLE_NAME = "items";
        final static String COL_TITLE = "title";
        final static String COL_DESCRIPTION = "description";
        final static String COL_LINK = "link";
        final static String COL_PUBDATE = "pubDate";
        final static String COL_CHANNEL = "channel";
        final static String COL_CHANNEL_TITLE = "channelTitle";
        final static String COL_CHANNEL_LINK = "channelLink";

        static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + DataBaseItemsEntry.TABLE_NAME + " (" +
                        DataBaseItemsEntry._ID + " INTEGER PRIMARY KEY," +
                        DataBaseItemsEntry.COL_TITLE + " TEXT," +
                        DataBaseItemsEntry.COL_DESCRIPTION + " TEXT, " +
                        DataBaseItemsEntry.COL_LINK + " TEXT, " +
                        DataBaseItemsEntry.COL_PUBDATE + " INTEGER, " +
                        DataBaseItemsEntry.COL_CHANNEL + " INTEGER, " +
                        "FOREIGN KEY(" + DataBaseItemsEntry.COL_CHANNEL + ") REFERENCES " + DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME + "(" + DataBaseChannels.DataBaseChannelsEntry._ID + "), " +
                        "CONSTRAINT " + DataBaseItemsEntry.TABLE_NAME + "_unique UNIQUE(" + DataBaseItemsEntry.COL_PUBDATE + ", " + DataBaseItemsEntry.COL_CHANNEL + "))";

        static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + DataBaseItemsEntry.TABLE_NAME;
    }
}
