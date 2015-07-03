package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.io.BufferedReader;
import java.io.IOException;

import io.internetthings.sailfish.ftue.ConfigureChromeActivity;


public class GoogleAuth2Activity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String logTAG = "GoogleAuth";

    //Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 0;

    //Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
       us from starting further intents.
     */
    private boolean mIntentInProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_google_auth2);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(new Scope("https://www.googleapis.com/auth/userinfo.email"))
                .setAccountName(SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null))
                .build();

        mGoogleApiClient.connect();

    }

    //Method runs when user is signed on
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(logTAG, "Successfully connected to Google");

        Intent i = new Intent(this, ConfigureChromeActivity.class);
        startActivity(i);

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
                result.startResolutionForResult(this, RC_SIGN_IN);
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
            if(responseCode != this.RESULT_OK){
                Log.i(logTAG, "We successfully connected!!!");
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
