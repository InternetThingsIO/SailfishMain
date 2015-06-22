package io.internetthings.sailfish;

/*
    Created by: Jason Maderski
    Date: 6/2/2015
    Project Name: Sailfish
    Version: 0.1
    Notes: Initial build of project, currently just display's login email in logcat

 */
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.provider.Settings;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.splunk.mint.Mint;

import java.io.IOException;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    //Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 0;

    private final String logTAG = this.getClass().getName();

    //Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
       us from starting further intents.
     */
    private boolean mIntentInProgress;
    private boolean nAccessEnabled;

    private TextView connectionStatusColor;
    private TextView loggedInEmail;

    BroadcastReceiver onSocketConnectReceiver;
    BroadcastReceiver onSocketDisconnectReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Line of code to add Splunk Mint to the project
        Mint.initAndStartSession(MainActivity.this, Constants.MINT_API_KEY);

        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope("https://www.googleapis.com/auth/userinfo.email"))
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                //.setAccountName("george@internetthings.io")
                .build();

        setupBroadcastManagers();
    }

    private void setupBroadcastManagers(){
        onSocketConnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                connectionStatusColor.setTextColor(getResources().getColor(R.color.Green));
                connectionStatusColor.setText("Connected!");
                connectionStatusColor.setTypeface(Typeface.DEFAULT_BOLD);
            }
        };

        onSocketDisconnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                connectionStatusColor.setTextColor(getResources().getColor(R.color.Red));
                connectionStatusColor.setText("Disconnected!");
                connectionStatusColor.setTypeface(Typeface.DEFAULT_BOLD);
            }
        };

        connectionStatusColor = (TextView)findViewById(R.id.status);

        LocalBroadcastManager.getInstance(this).registerReceiver(onSocketConnectReceiver,
                new IntentFilter("onSocketConnect"));

        LocalBroadcastManager.getInstance(this).registerReceiver(onSocketDisconnectReceiver,
                new IntentFilter("onSocketDisconnect"));

    }
    //Method runs when user is signed on
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("", "onConnected Success");
        getProfileInformation();


    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }


    //Runs when Google+ sign in fails
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("", "onConnectionFailed");
        if(!mIntentInProgress && result.hasResolution()){
            try{
                mIntentInProgress = true;
                result.startResolutionForResult(this, RC_SIGN_IN);
            }catch (SendIntentException e){
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent){
        if(requestCode == RC_SIGN_IN){
            if(responseCode != RESULT_OK){

            }

            mIntentInProgress = false;

            if(!mGoogleApiClient.isConnected()){
                mGoogleApiClient.reconnect();
            }
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onSocketConnectReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onSocketDisconnectReceiver);
        super.onDestroy();

    }

    //Display's person email in Logcat if connected
    private void getProfileInformation(){
        try{
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

            if(email != null){
                //display Person ID in logcat
                Log.d(logTAG, "Name: " + email);
                //display Person ID on Home screen
                loggedInEmail = (TextView)findViewById(R.id.emailTxtView);
                loggedInEmail.setText(email);

                //get the token here in case we need to provide extra permissions to do it
                new RetrieveTokenTask().execute(email);

                //tell mint who we are
                Mint.setUserIdentifier(email);

                SailfishPreferences.editor(this).putString(SailfishPreferences.EMAIL_KEY, email);
                SailfishPreferences.editor(this).commit();

                startNotificationService();

                Log.d(logTAG, "Got email successfully");

            }else{
                Log.e("", "Person information is NULL");
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //Checks whether or not the NoticeNotificationService has access
    private void checkNotificationAccess(){

        String enabledAppList = Settings.Secure.getString(this.getContentResolver(),
                "enabled_notification_listeners");
        if(enabledAppList == null)
            enabledAppList = "None";

        boolean checkAppAccessFlag = enabledAppList.contains("SailfishNotificationService");

        if (!checkAppAccessFlag) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
    }

    //Sends Notice created text message
    private void send1stMSG(){
        SendNotification sn = new SendNotification(this);
        sn.SendMSG("Hello there Stranger!");
    }

    private void startNotificationService(){

        Intent i = new Intent(this, SailfishNotificationService.class);
        this.startService(i);

    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {
        private static final int REQ_SIGN_IN_REQUIRED = 55664;
        boolean retry = true;

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:https://www.googleapis.com/auth/userinfo.email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
            } catch (IOException e) {
                Log.e(logTAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                if (retry) {
                    retry = false;
                    startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
                }
            } catch (GoogleAuthException e) {
                switch (Log.e(logTAG, e.getMessage())) {
                }
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e(logTAG, "Token Value: " + s);

            //get notification access after everything else works
            checkNotificationAccess();

        }
    }

}

