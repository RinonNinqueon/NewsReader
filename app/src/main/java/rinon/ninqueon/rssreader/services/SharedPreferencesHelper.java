package rinon.ninqueon.rssreader.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import rinon.ninqueon.rssreader.R;

/**
 * Created by Rinon Ninqueon on 04.04.2017.
 */

public final class SharedPreferencesHelper
{
    public final static boolean THEME_LIGHT = false;
    public final static boolean THEME_DARK  = true;

    private static SharedPreferences getDefaultSharedPreferences(final Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getSharedString(final Context context, final String key, final String defaultValue)
    {
        final SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(context);
        return defaultSharedPreferences.getString(key, defaultValue);
    }

    private static int getSharedInteger(final Context context, final String key, final int defaultValue)
    {
        final SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(context);
        return defaultSharedPreferences.getInt(key, defaultValue);
    }

    static int getSharedIntegerId(final Context context, final int keyId, final int defaultValueId)
    {
        final String key = context.getResources().getString(keyId);
        final int defaultValue = context.getResources().getInteger(defaultValueId);
        return getSharedInteger(context, key, defaultValue);
    }

    private static boolean getSharedBoolean(final Context context, final String key, final boolean defaultValue)
    {
        final SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(context);
        return defaultSharedPreferences.getBoolean(key, defaultValue);
    }

    public static boolean getSharedBooleanId(final Context context, final int keyId, final int defaultValueId)
    {
        final String key = context.getResources().getString(keyId);
        final boolean defaultValue = context.getResources().getBoolean(defaultValueId);
        return getSharedBoolean(context, key, defaultValue);
    }

    public static String getThemeFromSettings(final Context context)
    {
        final String resourceKey = context.getString(R.string.settings_application_theme);
        final String values[] = context.getResources().getStringArray(R.array.settings_application_theme_list_values);
        return SharedPreferencesHelper.getSharedString(context, resourceKey, values[0]);
    }

    public static boolean getThemeType(final Context context)
    {
        final String resourceKey = context.getString(R.string.settings_application_theme);
        final String values[] = context.getResources().getStringArray(R.array.settings_application_theme_list_values);
        final String settings_application_theme = SharedPreferencesHelper.getSharedString(context, resourceKey, values[0]);

        if (settings_application_theme.equals(values[0]))
        {
            return THEME_LIGHT;
        }
        if (settings_application_theme.equals(values[1]))
        {
            return THEME_DARK;
        }

        return THEME_LIGHT;
    }
}
