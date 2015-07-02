package io.internetthings.sailfish;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by gsapp on 7/1/2015.
 */
public class GoogleAuth {

    private static final String logTAG = "GoogleAuth";

    public static String getToken(Context context, String email){
        String scopes = "oauth2:https://www.googleapis.com/auth/userinfo.email";
        String token = null;
        try {
            token = GoogleAuthUtil.getToken(context, email, scopes);
        } catch (IOException e) {
            Log.e(logTAG, e.getMessage());
        } catch (UserRecoverableAuthException e) {
            //startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
        } catch (GoogleAuthException e) {
            switch (Log.e(logTAG, e.getMessage())) {
            }
        }

        //make a request with the token

        BufferedReader in = null;
        String data = null;

        return token;

    }

}
