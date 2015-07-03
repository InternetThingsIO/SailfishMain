package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by gsapp on 7/1/2015.
 */
public class GoogleAuthActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String logTAG = "GoogleAuth";

    //Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 0;

    //Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    private Activity context;

    private Intent connectionSuccess;

    /* A flag indicating that a PendingIntent is in progress and prevents
       us from starting further intents.
     */
    private boolean mIntentInProgress;

    public GoogleAuthActivity(Activity context){

        this.context = context;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(new Scope("https://www.googleapis.com/auth/userinfo.email"))
                .setAccountName(SailfishPreferences.reader(context).getString(SailfishPreferences.EMAIL_KEY, null))
                .build();
    }

    public void Connect(Intent connectionSuccess){

        this.connectionSuccess = connectionSuccess;

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    public void Disconnect(){
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    //Method runs when user is signed on
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(logTAG, "Successfully connected to Google");

        context.startActivity(connectionSuccess);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    //Runs when Google+ sign in fails
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(logTAG, "onConnectionFailed");
        if(!mIntentInProgress && result.hasResolution()){
            try{
                mIntentInProgress = true;
                result.startResolutionForResult(context, RC_SIGN_IN);
                Log.e(logTAG, "Resolving connection failure");
            }catch (IntentSender.SendIntentException e){
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent){

        Log.w(logTAG, "Got onActivityResult");

        if(requestCode == RC_SIGN_IN){
            if(responseCode != context.RESULT_OK){

            }

            mIntentInProgress = false;

            if(!mGoogleApiClient.isConnected()){
                mGoogleApiClient.reconnect();
            }
        }
    }


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
