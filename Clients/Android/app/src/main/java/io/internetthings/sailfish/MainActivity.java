package io.internetthings.sailfish;

/*
    Created by: Jason Maderski
    Date: 6/2/2015
    Project Name: Sailfish
    Version: 0.1
    Notes: Initial build of project, currently just display's login email in logcat

 */
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.provider.Settings;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.splunk.mint.Mint;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    //Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 0;
    public static final String MY_PREFS_NAME = "SailFishPref";
    private final String logTAG = this.getClass().getName();

    //Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
       us from starting further intents.
     */
    private boolean mIntentInProgress;

    private TextView connectionStatusColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Line of code to add Splunk Mint to the project
        Mint.initAndStartSession(MainActivity.this, "50573816");

        setContentView(R.layout.activity_main);

        checkNotificationAccess();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.sign_out_and_sign_in).setOnClickListener(this);

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

    //When button any button is pushed "onClick() is displayed in logcat, this method associates
    // buttons with actions
    @Override
    public void onClick(View v) {
        Log.d("", "onClick()");
        if(v.getId() == R.id.sign_in_button
            && !mGoogleApiClient.isConnecting()){
            mGoogleApiClient.connect();
        }

        else if(v.getId() == R.id.sign_out_button){
            if(mGoogleApiClient.isConnected()){
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                Log.d("", "User signed out");
            }
        }

        else if(v.getId() == R.id.sign_out_and_sign_in) {
            if(mGoogleApiClient.isConnected()){
                Log.d("", "User signed out");
                mGoogleApiClient.clearDefaultAccountAndReconnect();
            }
        }
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
        super.onDestroy();
    }

    //Display's person email in Logcat if connected
    private void getProfileInformation(){
        try{
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

            if(email != null){
                //display Person ID in logcat
                Log.d(logTAG, "Name: " + email);

                SharedPreferences.Editor editor =
                        getSharedPreferences(MY_PREFS_NAME, MODE_MULTI_PROCESS).edit();
                editor.putString("email", email);
                editor.commit();

                Log.d(logTAG, "Restarting service");

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

    //Changes the Connection UI text
    private void connectionStatus(){
        Log.i(logTAG, "connectionStatus");
        connectionStatusColor = (TextView)findViewById(R.id.status);
        if(SailfishSocketIO.SocketSingleton().connected()) {
            connectionStatusColor.setTextColor(getResources().getColor(R.color.Green));
            connectionStatusColor.setText("Connected!");
            connectionStatusColor.setTypeface(Typeface.DEFAULT_BOLD);
        }else{
            connectionStatusColor.setTextColor(getResources().getColor(R.color.Red));
            connectionStatusColor.setText("Not Connected");
        }
    }

}
