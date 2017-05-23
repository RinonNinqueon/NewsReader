package rinon.ninqueon.rssreader.utils;

import rinon.ninqueon.rssreader.R;

/**
 * Created by Rinon Ninqueon on 11.04.2017.
 */

public final class ProgressCodes
{
    public final static int ERROR = -1;
    public final static int START = 0;
    public final static int XML_GET_SUCCESS = 1;
    public final static int XML_PARSE_SUCCESS = 2;
    public final static int READY_TO_WRITE_DB = 3;
    public final static int WRITE_DB_SUCCESS = 4;

    public final static int TEXT_NO_CODE = -1;

    private ProgressCodes()
    {
        throw new UnsupportedOperationException("ProgressCodes.Constructor");
    }

    public static int getProgressMessageId(final int progressCode)
    {
        switch (progressCode)
        {
            case ERROR:
                return R.string.download_error;
            case START:
                return  R.string.download_start;
            case XML_GET_SUCCESS:
                return R.string.download_xml_get_success;
            case XML_PARSE_SUCCESS:
                return R.string.download_xml_parse_success;
            case READY_TO_WRITE_DB:
                return R.string.download_ready_to_write_db;
            case WRITE_DB_SUCCESS:
                return R.string.download_write_db_success;
            default:
                return TEXT_NO_CODE;
        }
    }
}
