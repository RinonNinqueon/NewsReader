package rinon.ninqueon.rssreader.database;

import android.provider.BaseColumns;

/**
 * Created by Rinon Ninqueon on 23.02.2017.
 */

final class DataBaseChannels
{
    private DataBaseChannels()
    {
        throw new UnsupportedOperationException("DataBaseChannels.Constructor");
    }

    final static class DataBaseChannelsEntry implements BaseColumns
    {
        final static String TABLE_NAME       = "channels";
        final static String COL_TITLE        = "title";
        final static String COL_DESCRIPTION  = "description";
        final static String COL_LINK         = "link";
        final static String COL_PUBDATE      = "pubDate";
        final static String COL_SOURCE       = "source";
        final static String COL_ERROR        = "error";

        static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + DataBaseChannelsEntry.TABLE_NAME + " (" +
                        DataBaseChannelsEntry._ID + " INTEGER PRIMARY KEY, " +
                        DataBaseChannelsEntry.COL_TITLE + " TEXT, " +
                        DataBaseChannelsEntry.COL_DESCRIPTION + " TEXT, " +
                        DataBaseChannelsEntry.COL_LINK + " TEXT, " +
                        DataBaseChannelsEntry.COL_PUBDATE + " INTEGER, " +
                        DataBaseChannelsEntry.COL_SOURCE + " TEXT NOT NULL, " +
                        DataBaseChannelsEntry.COL_ERROR + " INTEGER, " +
                        "CONSTRAINT " + DataBaseChannelsEntry.COL_SOURCE + "_unique UNIQUE(" + DataBaseChannelsEntry.COL_SOURCE + "))";

        static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + DataBaseChannelsEntry.TABLE_NAME;
    }
}
