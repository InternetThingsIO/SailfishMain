package io.internetthings.sailfish;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by gsapp on 6/18/2015.
 */
public class SailfishPreferences {

    private static SharedPreferences.Editor _editor;

    public static final String MY_PREFS_NAME = "SailFishPref";

    public static final String EMAIL_KEY = "email";
    public static final String FTUE_COMPLETED_KEY = "FTUECompleted";
    public static final String MUTED_PACKAGES_KEY = "mute";
    public static final String RATING_SHOWN_KEY = "RATING_SHOWN_KEY";

    private static SharedPreferences.Editor editor(Context context){

        if (_editor == null) {
            _editor = context.getSharedPreferences(MY_PREFS_NAME, context.MODE_MULTI_PROCESS).edit();
            _editor.commit();
        }

        return _editor;

    }

    private static SharedPreferences reader(Context context){

        return context.getSharedPreferences(MY_PREFS_NAME, context.MODE_MULTI_PROCESS);

    }

    public static void setFTUECompleted(Context context, boolean completed){
        editor(context).putBoolean(FTUE_COMPLETED_KEY, completed);
    }

    public static boolean getFTUECompleted(Context context){
        return reader(context).getBoolean(FTUE_COMPLETED_KEY, false);
    }

    public static void setEmail(Context context, String email){
        editor(context).putString(EMAIL_KEY, email);
    }

    public static String getEmail(Context context){
        return reader(context).getString(EMAIL_KEY, null);
    }

    public static void setMutedPackages(Context context, String value){
        editor(context).putString(MUTED_PACKAGES_KEY, value);
    }

    public static String getMutedPackages(Context context){
        return reader(context).getString(MUTED_PACKAGES_KEY, null);
    }

    public static Boolean getRatingShown(Context context){
        return reader(context).getBoolean(RATING_SHOWN_KEY, false);
    }

    public static void setRatingShown(Context context, Boolean value){
        editor(context).putBoolean(RATING_SHOWN_KEY, value);
    }

    public static void commit(Context context){
        editor(context).commit();
        _editor = null;
    }


}
