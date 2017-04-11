package rinon.ninqueon.rssreader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.logging.Logger;

import rinon.ninqueon.rssreader.utils.ErrorCodes;
import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 23.02.2017.
 */

public final class DataBaseHelper extends SQLiteOpenHelper
{
    private final static String LOGGER_TAG      = DataBaseHelper.class.getName();
    private final static Logger logger          = Logger.getLogger(LOGGER_TAG);
    private final static int DATABASE_VERSION   = 1;
    private final static String DATABASE_NAME   = "NewsReader.db";
    public final static int DEFAULT_OFFSET      = 0;
    public final static int DEFAULT_LIMIT       = 10;

    public DataBaseHelper(final Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db)
    {
        try
        {
            db.execSQL(DataBaseChannels.DataBaseChannelsEntry.SQL_CREATE_ENTRIES);
            db.execSQL(DataBaseItems.DataBaseItemsEntry.SQL_CREATE_ENTRIES);
        }
        catch (final SQLException ex)
        {
            logger.severe(ex.getMessage());
        }
    }

    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
    {
        try
        {
            db.execSQL(DataBaseItems.DataBaseItemsEntry.SQL_DELETE_ENTRIES);
            db.execSQL(DataBaseChannels.DataBaseChannelsEntry.SQL_DELETE_ENTRIES);
        }
        catch (final SQLException ex)
        {
            logger.severe(ex.getMessage());
        }
        onCreate(db);
    }
    public final void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    public final void writeChannelUrl(final SQLiteDatabase db, final String channelUrl)
    {
        if (db == null || channelUrl == null || !db.isOpen())
        {
            return;
        }

        final ContentValues values = new ContentValues();
        values.put(DataBaseChannels.DataBaseChannelsEntry.COL_SOURCE, channelUrl);
        values.put(DataBaseChannels.DataBaseChannelsEntry.COL_ERROR, ErrorCodes.ERROR_UNCHECKED_CHANNEL);

        beginTransaction(db);

        try
        {
            try
            {
                db.insertOrThrow(DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME, null, values);
            }

            catch (SQLiteConstraintException e)
            {
                logger.warning("Channel already exists.");
            }
            setTransactionSuccessful(db);
        }
        finally
        {
            endTransaction(db);
        }
    }

    public final int writeItems(final SQLiteDatabase db, final FeedEntry entries[])
    {
        if (db == null || entries == null)
        {
            return 0;
        }

        int insertedItemsCount = 0;

        beginTransaction(db);
        try
        {
            for (final FeedEntry entry : entries)
            {
                final ContentValues values = feedEntryToValuesInsert(entry);

                try
                {
                    db.insertOrThrow(DataBaseItems.DataBaseItemsEntry.TABLE_NAME, null, values);
                    insertedItemsCount++;
                }
                catch (SQLiteConstraintException e)
                {
                    updateItemsByChannelDate(db, entry);
                    logger.warning("Entry already exists.");
                }
            }
            setTransactionSuccessful(db);
        }
        finally
        {
            endTransaction(db);
        }

        return insertedItemsCount;
    }

    public final FeedEntry[] readAllChannels(final SQLiteDatabase db)
    {
        if (db == null)
        {
            return null;
        }

        final String query = "SELECT * FROM " + DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME +
                " ORDER BY " + DataBaseChannels.DataBaseChannelsEntry.COL_TITLE + " DESC";

        final Cursor cursor = db.rawQuery(query, null);
        final ArrayList<FeedEntry> result = new ArrayList<>();

        while(cursor.moveToNext())
        {
            final long id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseChannels.DataBaseChannelsEntry._ID));
            final String title = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseChannels.DataBaseChannelsEntry.COL_TITLE));
            final String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseChannels.DataBaseChannelsEntry.COL_DESCRIPTION));
            final String link = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseChannels.DataBaseChannelsEntry.COL_LINK));
            final long pubDate = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseChannels.DataBaseChannelsEntry.COL_PUBDATE));
            final int error = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseChannels.DataBaseChannelsEntry.COL_ERROR));

            final String channelLink = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseChannels.DataBaseChannelsEntry.COL_SOURCE));

            final FeedEntry feedEntry = new FeedEntry(id, title, description, link, pubDate, channelLink, error);

            result.add(feedEntry);
        }
        cursor.close();

        FeedEntry resultArray[] = new FeedEntry[result.size()];
        resultArray = result.toArray(resultArray);

        return resultArray;
    }

    public final FeedEntry[] readItems(final SQLiteDatabase db, final long offset, final long limit)
    {
        if (db == null)
        {
            return null;
        }

        final String query = "SELECT itemTable.*, " +
                "channelTable." + DataBaseChannels.DataBaseChannelsEntry.COL_TITLE +
                " AS " + DataBaseItems.DataBaseItemsEntry.COL_CHANNEL_TITLE +
                ", channelTable." + DataBaseChannels.DataBaseChannelsEntry.COL_LINK +
                " AS " + DataBaseItems.DataBaseItemsEntry.COL_CHANNEL_LINK +
                " FROM " + DataBaseItems.DataBaseItemsEntry.TABLE_NAME + " AS itemTable, " +
                DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME + " AS channelTable" +
                " WHERE itemTable." + DataBaseItems.DataBaseItemsEntry.COL_CHANNEL +
                "=channelTable." + DataBaseChannels.DataBaseChannelsEntry._ID +
                " ORDER BY itemTable." + DataBaseItems.DataBaseItemsEntry.COL_PUBDATE + " DESC" +
                " LIMIT " + offset + "," + limit;

        final Cursor cursor = db.rawQuery(query, null);
        final ArrayList<FeedEntry> result = new ArrayList<>();

        while(cursor.moveToNext())
        {
            final long id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry._ID));
            final String title = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_TITLE));
            final String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_DESCRIPTION));
            final String link = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_LINK));
            final long pubDate = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_PUBDATE));

            final String channelTitle = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_CHANNEL_TITLE));
            final String channelLink = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_CHANNEL_LINK));

            final long channelId = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_CHANNEL));


            final FeedEntry feedEntry = new FeedEntry(id, title, description, link, pubDate, channelId, channelTitle, channelLink, ErrorCodes.ERROR_NO_ERROR);

            result.add(feedEntry);
        }
        cursor.close();

        FeedEntry resultArray[] = new FeedEntry[result.size()];
        resultArray = result.toArray(resultArray);

        return resultArray;
    }

    public final FeedEntry[] readItemsByChannelId(final SQLiteDatabase db, final long channel_id, final long offset, final long limit)
    {
        if (db == null)
        {
            return null;
        }

        final String query = "SELECT itemTable.*, " +
                "channelTable." + DataBaseChannels.DataBaseChannelsEntry.COL_TITLE +
                " AS " + DataBaseItems.DataBaseItemsEntry.COL_CHANNEL_TITLE +
                ", channelTable." + DataBaseChannels.DataBaseChannelsEntry.COL_LINK +
                " AS " + DataBaseItems.DataBaseItemsEntry.COL_CHANNEL_LINK +
                " FROM " + DataBaseItems.DataBaseItemsEntry.TABLE_NAME + " AS itemTable, " +
                DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME + " AS channelTable" +
                " WHERE itemTable." + DataBaseItems.DataBaseItemsEntry.COL_CHANNEL + "=" + channel_id +
                " AND " + DataBaseItems.DataBaseItemsEntry.COL_CHANNEL +
                "=channelTable." + DataBaseChannels.DataBaseChannelsEntry._ID +
                " ORDER BY itemTable." + DataBaseItems.DataBaseItemsEntry.COL_PUBDATE + " DESC" +
                " LIMIT " + offset + "," + limit;

        final Cursor cursor = db.rawQuery(query, null);
        final ArrayList<FeedEntry> result = new ArrayList<>();

        while(cursor.moveToNext())
        {
            final long id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry._ID));
            final String title = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_TITLE));
            final String description = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_DESCRIPTION));
            final String link = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_LINK));
            final long pubDate = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_PUBDATE));

            final String channelTitle = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_CHANNEL_TITLE));
            final String channelLink = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_CHANNEL_LINK));

            final long channelId = cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseItems.DataBaseItemsEntry.COL_CHANNEL));


            final FeedEntry feedEntry = new FeedEntry(id, title, description, link, pubDate, channelId, channelTitle, channelLink, ErrorCodes.ERROR_NO_ERROR);

            result.add(feedEntry);
        }
        cursor.close();

        FeedEntry resultArray[] = new FeedEntry[result.size()];
        resultArray = result.toArray(resultArray);

        return resultArray;
    }

    public void updateChannelsById(final SQLiteDatabase db, final FeedEntry feedEntry, final boolean deleteItems)
    {
        if (db == null || feedEntry == null)
        {
            return;
        }

        if (deleteItems)
        {
            deleteItemWithChannelId(db, feedEntry.getId());
        }

        final ContentValues values = feedChannelToValuesUpdateById(feedEntry);

        final String selection = DataBaseChannels.DataBaseChannelsEntry._ID + "=?";
        String idString = String.valueOf(feedEntry.getId());
        final String[] selectionArgs = {idString};

        db.update(
                DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public void updateChannelUrlById(final SQLiteDatabase db, final long channelId, final String channelUrl, final boolean deleteItems)
    {
        if (db == null || channelUrl == null)
        {
            return;
        }

        if (deleteItems)
        {
            deleteItemWithChannelId(db, channelId);
        }

        final ContentValues values = new ContentValues();
        values.put(DataBaseChannels.DataBaseChannelsEntry.COL_SOURCE, channelUrl);

        final String selection = DataBaseChannels.DataBaseChannelsEntry._ID + "=?";
        String idString = String.valueOf(channelId);
        final String[] selectionArgs = {idString};

        db.update(
                DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public void updateChannelError(final SQLiteDatabase db, final long channelId, final int errorCode)
    {
        if (db == null)
        {
            return;
        }

        final ContentValues values = new ContentValues();
        values.put(DataBaseChannels.DataBaseChannelsEntry.COL_ERROR, errorCode);

        final String selection = DataBaseChannels.DataBaseChannelsEntry._ID + "=?";
        String idString = String.valueOf(channelId);
        final String[] selectionArgs = {idString};

        db.update(
                DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    private void updateItemsByChannelDate(final SQLiteDatabase db, final FeedEntry feedEntry)
    {
        if (db == null || feedEntry == null)
        {
            return;
        }

        final ContentValues values = feedEntryToValuesUpdate(feedEntry);

        final String selection = DataBaseItems.DataBaseItemsEntry.COL_PUBDATE + "=?" +
                " AND " + DataBaseItems.DataBaseItemsEntry.COL_CHANNEL + "=?";
        final String date = String.valueOf(feedEntry.getPubDate().getTime());
        final String channel = String.valueOf(feedEntry.getChannelId());
        final String[] selectionArgs = {date, channel};

        db.update(
                DataBaseItems.DataBaseItemsEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    private ContentValues feedEntryToValuesInsert(final FeedEntry feedEntry)
    {
        if (feedEntry == null)
        {
            return null;
        }

        final ContentValues values = new ContentValues();

        if (feedEntry.getTitle() != null)
        {
            values.put(DataBaseItems.DataBaseItemsEntry.COL_TITLE, feedEntry.getTitle());
        }
        if (feedEntry.getDescription() != null)
        {
            values.put(DataBaseItems.DataBaseItemsEntry.COL_DESCRIPTION, feedEntry.getDescription());
        }
        if (feedEntry.getLink() != null)
        {
            values.put(DataBaseItems.DataBaseItemsEntry.COL_LINK, feedEntry.getLink());
        }
        if (feedEntry.getPubDate() != null)
        {
            values.put(DataBaseItems.DataBaseItemsEntry.COL_PUBDATE, feedEntry.getPubDate().getTime());
        }

        values.put(DataBaseItems.DataBaseItemsEntry.COL_CHANNEL, feedEntry.getChannelId());

        return values;
    }

    private ContentValues feedEntryToValuesUpdate(final FeedEntry feedEntry)
    {
        if (feedEntry == null)
        {
            return null;
        }

        final ContentValues values = new ContentValues();

        if (feedEntry.getTitle() != null)
        {
            values.put(DataBaseItems.DataBaseItemsEntry.COL_TITLE, feedEntry.getTitle());
        }
        if (feedEntry.getDescription() != null)
        {
            values.put(DataBaseItems.DataBaseItemsEntry.COL_DESCRIPTION, feedEntry.getDescription());
        }
        if (feedEntry.getLink() != null)
        {
            values.put(DataBaseItems.DataBaseItemsEntry.COL_LINK, feedEntry.getLink());
        }

        return values;
    }

    private ContentValues feedChannelToValuesUpdateById(final FeedEntry feedEntry)
    {
        if (feedEntry == null)
        {
            return null;
        }

        final ContentValues values = new ContentValues();

        if (feedEntry.getTitle() != null)
        {
            values.put(DataBaseChannels.DataBaseChannelsEntry.COL_TITLE, feedEntry.getTitle());
        }
        if (feedEntry.getDescription() != null)
        {
            values.put(DataBaseChannels.DataBaseChannelsEntry.COL_DESCRIPTION, feedEntry.getDescription());
        }
        if (feedEntry.getLink() != null)
        {
            values.put(DataBaseChannels.DataBaseChannelsEntry.COL_LINK, feedEntry.getLink());
        }
        if (feedEntry.getPubDate() != null)
        {
            values.put(DataBaseChannels.DataBaseChannelsEntry.COL_PUBDATE, feedEntry.getPubDate().getTime());
        }
        if (feedEntry.getSource() != null)
        {
            values.put(DataBaseChannels.DataBaseChannelsEntry.COL_SOURCE, feedEntry.getSource());
        }

        return values;
    }

    public final void deleteChannel(final SQLiteDatabase db, final long id)
    {
        if (db == null)
        {
            return;
        }

        deleteItemWithChannelId(db, id);

        final String selection = DataBaseChannels.DataBaseChannelsEntry._ID + "=?";
        final String _id = String.valueOf(id);
        final String[] selectionArgs = {_id};

        db.delete(DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME, selection, selectionArgs);
    }

    public final void deleteItemsAll(final SQLiteDatabase db)
    {
        if (db == null)
        {
            return;
        }

        db.delete(DataBaseItems.DataBaseItemsEntry.TABLE_NAME, null, null);
    }

    private void deleteItemWithChannelId(final SQLiteDatabase db, final long id)
    {
        if (db == null)
        {
            return;
        }

        final String selection = DataBaseItems.DataBaseItemsEntry.COL_CHANNEL + "=?";
        final String _id = String.valueOf(id);
        final String[] selectionArgs = {_id};

        db.delete(DataBaseItems.DataBaseItemsEntry.TABLE_NAME, selection, selectionArgs);
    }

    public final boolean channelExists(final SQLiteDatabase db, final String url)
    {
        final String selection = DataBaseChannels.DataBaseChannelsEntry.COL_SOURCE + "=?";
        final String[] selectionArgs = {url};

        long count = DatabaseUtils.queryNumEntries(db, DataBaseChannels.DataBaseChannelsEntry.TABLE_NAME, selection, selectionArgs);

        return (count > 0);
    }

    public void deleteItemsOld(final SQLiteDatabase db, final long dateLong)
    {
        if (db == null)
        {
            return;
        }

        final String selection = DataBaseItems.DataBaseItemsEntry.COL_PUBDATE + "<?";
        final String pubDate = String.valueOf(dateLong);
        final String[] selectionArgs = {pubDate};

        db.delete(DataBaseItems.DataBaseItemsEntry.TABLE_NAME, selection, selectionArgs);
    }

    private void beginTransaction(final SQLiteDatabase db)
    {
        if (db == null || !db.isOpen())
        {
            return;
        }

        if (!db.inTransaction())
        {
            db.beginTransaction();
        }
    }

    private void setTransactionSuccessful(final SQLiteDatabase db)
    {
        if (db == null || !db.isOpen())
        {
            return;
        }

        if (db.inTransaction())
        {
            db.setTransactionSuccessful();
        }
    }

    private void endTransaction(final SQLiteDatabase db)
    {
        if (db == null || !db.isOpen())
        {
            return;
        }

        if (db.inTransaction())
        {
            db.endTransaction();
        }
    }
}
