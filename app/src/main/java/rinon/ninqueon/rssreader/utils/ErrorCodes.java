package rinon.ninqueon.rssreader.utils;

import rinon.ninqueon.rssreader.R;

/**
 * Created by Rinon Ninqueon on 13.03.2017.
 */

public final class ErrorCodes
{
    public final static int ERROR_NO_ERROR                 = 0;
    public final static int ERROR_NO_CONNECTION            = -1;
    public final static int ERROR_ONLY_WIFI                = -2;
    public final static int ERROR_PARSE                    = -3;
    public final static int ERROR_DOCUMENT_TYPE            = -4;
    public final static int ERROR_DATABASE                 = -5;
    public final static int ERROR_CHANNEL_EXISTS           = -6;
    public final static int ERROR_WRONG_URL                = -7;
    public final static int ERROR_IO_ERROR                 = -8;
    public final static int ERROR_EXTERNAL_STORAGE_ERROR   = -9;
    public final static int ERROR_CONNECTION_ERROR         = -10;
    public final static int ERROR_UNCHECKED_CHANNEL        = -11;
    private ErrorCodes()
    {
        throw new UnsupportedOperationException("ErrorCodes.Constructor");
    }

    public static int getErrorMessageId(final int errorCode)
    {
        switch (errorCode)
        {
            case ERROR_NO_ERROR:
                return R.string.error_no_error;
            case ERROR_NO_CONNECTION:
                return R.string.error_no_internet_connection;
            case ERROR_ONLY_WIFI:
                return R.string.error_only_wifi;
            case ERROR_PARSE:
                return R.string.error_parse;
            case ERROR_DOCUMENT_TYPE:
                return R.string.error_document_type;
            case ERROR_DATABASE:
                return R.string.error_database;
            case ERROR_WRONG_URL:
                return R.string.error_wrong_url;
            case ERROR_CHANNEL_EXISTS:
                return R.string.error_channel_exists;
            case ERROR_IO_ERROR:
                return R.string.error_io;
            case ERROR_EXTERNAL_STORAGE_ERROR:
                return R.string.error_external_storage;
            case ERROR_CONNECTION_ERROR:
                return R.string.error_connection;
            case ERROR_UNCHECKED_CHANNEL:
                return R.string.error_unchecked;
            default:
                return R.string.error;
        }
    }
}
