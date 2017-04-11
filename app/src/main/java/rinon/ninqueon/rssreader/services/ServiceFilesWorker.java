package rinon.ninqueon.rssreader.services;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import rinon.ninqueon.rssreader.xmlfeed.FeedEntry;

/**
 * Created by Rinon Ninqueon on 10.04.2017.
 */

final class ServiceFilesWorker
{
    private final static String DEFAULT_FILE_NAME   = "NewsReaderChannels.list";
    static void writeToFile(final FeedEntry[] feedEntries) throws IOException
    {
        FileOutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;

        try
        {
            File file = new File(Environment.getExternalStorageDirectory(), DEFAULT_FILE_NAME);

            outputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            for (final FeedEntry feedEntry : feedEntries)
            {
                final String url = feedEntry.getChannelLink();
                if (url != null)
                {
                    bufferedWriter.write(url);
                    bufferedWriter.write('\n');
                }
            }
        }
        finally
        {
            if (bufferedWriter != null)
            {
                bufferedWriter.close();
            }
            if (outputStream != null)
            {
                outputStream.close();
            }
        }
    }

    static ArrayList<String> readFromFile() throws IOException
    {
        FileInputStream inputStream = null;
        BufferedReader bufferedReader = null;

        final ArrayList<String> result = new ArrayList<>();
        try
        {
            File file = new File(Environment.getExternalStorageDirectory(), DEFAULT_FILE_NAME);

            inputStream = new FileInputStream(file);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String readString;

            while((readString = bufferedReader.readLine()) != null)
            {
                result.add(readString);
            }
        }
        finally
        {
            if (bufferedReader != null)
            {
                bufferedReader.close();
            }
            if (inputStream != null)
            {
                inputStream.close();
            }
        }

        return result;
    }

    static boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    static boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
