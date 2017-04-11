package rinon.ninqueon.rssreader.services;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import rinon.ninqueon.rssreader.database.DataBaseHelper;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 06.03.2017.
 */

final class ServiceDatabaseWork
{
    private final static int MILLISECONDS_IN_SECOND     = 1000;
    private final static int SECONDS_IN_MINUTE          = 60;
    private final static int MINUTES_IN_HOUR            = 60;
    private final static int HOURS_IN_DAY               = 24;

    private ServiceDatabaseWork()
    {
        throw new UnsupportedOperationException("ServiceDatabaseWork.Constructor");
    }

    static FeedEntry[] readAllChannels(final Context context) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        FeedEntry feedEntry[] = null;
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getReadableDatabase();
            feedEntry = mDbHelper.readAllChannels(db);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }

        return feedEntry;
    }

    static FeedEntry[] readItems(final Context context, final long start, final long count) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        FeedEntry feedEntry[] = null;
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getReadableDatabase();
            feedEntry = mDbHelper.readItems(db, start, count);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }

        return feedEntry;
    }

    static FeedEntry[] readItemsByChannelId(final Context context, final long channelId, final long start, final long count) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        FeedEntry feedEntry[] = null;
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getReadableDatabase();
            feedEntry = mDbHelper.readItemsByChannelId(db, channelId, start, count);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }

        return feedEntry;
    }

    static int writeItems(final Context context, final FeedEntry feedEntries[]) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;
        int insertedItemsCount = 0;

        try
        {
            db = mDbHelper.getWritableDatabase();
            insertedItemsCount = mDbHelper.writeItems(db, feedEntries);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }
        return insertedItemsCount;
    }

    static void writeChannelUrl(final Context context, final String channelUrl) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.writeChannelUrl(db, channelUrl);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }
    }

    static void deleteChannel(final Context context, final long id) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.deleteChannel(db, id);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }
    }

    static boolean isChannelExists(final Context context, final String url) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;
        boolean channelExists = false;

        try
        {
            db = mDbHelper.getWritableDatabase();
            channelExists = mDbHelper.channelExists(db, url);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }

        return channelExists;
    }

    static void updateChannel(final Context context, final FeedEntry feedEntry, final boolean deleteOld) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.updateChannelsById(db, feedEntry, deleteOld);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }
    }

    static void updateChannelUrlById(final Context context, final long channelId, final String channelUrl, final boolean deleteOld) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.updateChannelUrlById(db, channelId, channelUrl, deleteOld);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }
    }

    static void deleteItemsAll(final Context context) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.deleteItemsAll(db);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }
    }

    static void deleteItemsOld(final Context context, final int daysSavePeriod) throws SQLException
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;
        long date = new Date().getTime();
        long millisecondsSavePeriod = daysToMilliseconds(daysSavePeriod);
        date -= millisecondsSavePeriod;

        try
        {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.deleteItemsOld(db, date);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }
    }

    static void updateChannelError(final Context context, final long channelId, final int errorCode)
    {
        DataBaseHelper mDbHelper = new DataBaseHelper(context);
        SQLiteDatabase db = null;

        try
        {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.updateChannelError(db, channelId, errorCode);
        }
        finally
        {
            if (db != null)
            {
                db.close();
            }
        }
    }

    private static long daysToMilliseconds(long days)
    {
        return days * HOURS_IN_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND;
    }

    static long hoursToMilliseconds(long hours)
    {
        return hours * MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND;
    }
}
