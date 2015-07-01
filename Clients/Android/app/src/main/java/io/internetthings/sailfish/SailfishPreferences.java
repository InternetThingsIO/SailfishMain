package io.internetthings.sailfish;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by gsapp on 6/18/2015.
 */
public class SailfishPreferences {

    private static SharedPreferences.Editor _editor;
    private static SharedPreferences _reader;

    public static final String MY_PREFS_NAME = "SailFishPref";

    public static final String EMAIL_KEY = "email";
    public static final String FTUE_COMPLETED_KEY = "FTUECompleted";

    public static SharedPreferences.Editor editor(Context context){

        if (_editor == null) {
            _editor = context.getSharedPreferences(MY_PREFS_NAME, context.MODE_MULTI_PROCESS).edit();
            _editor.commit();
        }

        return _editor;

    }

    public static SharedPreferences reader(Context context){

        if (_reader == null) {
            editor(context);
            _reader = context.getSharedPreferences(MY_PREFS_NAME, context.MODE_MULTI_PROCESS);
        }

        return _reader;

    }


}
