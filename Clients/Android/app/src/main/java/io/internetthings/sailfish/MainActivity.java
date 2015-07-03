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
import android.view.MotionEvent;
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

public class MainActivity extends Activity{

    private final String logTAG = this.getClass().getName();

    BroadcastReceiver onSocketConnectReceiver;
    BroadcastReceiver onSocketDisconnectReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Line of code to add Splunk Mint to the project
        Mint.initAndStartSession(MainActivity.this, Constants.MINT_API_KEY);

        setupBroadcastManagers();

        setProfileInformation();

        setContentView(R.layout.activity_main);
    }
    //Changes Connection status text to "Connected", sets text color to green and
    //changes the typeface to BOLD
    private void setConnectedText(){
        TextView connectionStatusColor = (TextView)findViewById(R.id.status);
        connectionStatusColor.setTextColor(getResources().getColor(R.color.Green));
        connectionStatusColor.setText("Connected!");
        connectionStatusColor.setTypeface(Typeface.DEFAULT_BOLD);
    }
    //Changes Connection status text to "Disconnected", sets text color to red and
    //changes typeface to BOLD
    private void setDisconnectedText(){
        TextView connectionStatusColor = (TextView)findViewById(R.id.status);
        connectionStatusColor.setTextColor(getResources().getColor(R.color.Red));
        connectionStatusColor.setText("Disconnected!");
        connectionStatusColor.setTypeface(Typeface.DEFAULT_BOLD);
    }
    //Shows users logged in email on main_Activity
    private void setLoggedInEmailText(String email){
        TextView loggedInEmail = (TextView)findViewById(R.id.emailTxtView);
        loggedInEmail.setText(email);
    }

    private void setupBroadcastManagers(){
        onSocketConnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setConnectedText();
            }
        };

        onSocketDisconnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setDisconnectedText();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(onSocketConnectReceiver,
                new IntentFilter("onSocketConnect"));

        LocalBroadcastManager.getInstance(this).registerReceiver(onSocketDisconnectReceiver,
                new IntentFilter("onSocketDisconnect"));

    }


    @Override
    protected void onStart(){
        super.onStart();

        SailfishSocketIO.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();

        //try to connect
        SailfishSocketIO.connect();

        //get current socket status
        if (SailfishSocketIO.isConnected())
            setConnectedText();
        else
            setDisconnectedText();

    }

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onSocketConnectReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onSocketDisconnectReceiver);
        super.onDestroy();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        //if 4 fingers touch at once, open debug menu
        if (event.getPointerCount() == 4){
            Log.i(logTAG, "Entering debug menu");

            Intent i = new Intent(this, DebugActivity.class);
            startActivity(i);

        }

        return true;
    }

    //Displays person email in Logcat if connected
    private void setProfileInformation(){
        try{
            String email = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);

            if(email != null){
                //display Person ID in logcat
                Log.d(logTAG, "Name: " + email);
                //display Person ID on Home screen
                setLoggedInEmailText(email);

                //tell mint who we are
                Mint.setUserIdentifier(email);

                //startNotificationService();

            }else{
                Log.e("", "Person information is NULL");
            }

        } catch (Exception e){
            e.printStackTrace();
        }
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
            if(!NotificationActions.checkNotificationAccess(getApplication())){
                NotificationActions.toastMSG(getApplication(), "Notice does not have Notification Access");
            }

        }
    }

}

