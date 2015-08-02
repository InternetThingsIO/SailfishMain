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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class MainActivity extends Activity{

    private final String logTAG = this.getClass().getName();

    BroadcastReceiver onSocketConnectReceiver;
    BroadcastReceiver onSocketDisconnectReceiver;

    private Boolean loadedDebug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBroadcastManagers();
    }
    //Changes Connection status text to "Connected", sets text color to green and
    //changes the typeface to BOLD
    private void setConnectedText(){
        ImageView status = (ImageView)findViewById(R.id.statusImage);
        status.setImageResource(R.drawable.connected);
    }
    //Changes Connection status text to "Disconnected", sets text color to red and
    //changes typeface to BOLD
    private void setDisconnectedText(){
        ImageView status = (ImageView)findViewById(R.id.statusImage);
        status.setImageResource(R.drawable.disconnected);
    }
    //Shows users logged in email on main_Activity
    private void setLoggedInEmailText(String email){
        TextView loggedInEmail = (TextView)findViewById(R.id.emailTxtView);
        loggedInEmail.setText(email);
    }

    public void openOptions(View view){
        Intent i = new Intent(this, OptionsActivity.class);
        startActivity(i);
    }

    //Opens WEBSITE
    public void openWebsite(View view){
        String website = "http://www.internetthings.io/";

        Uri url = Uri.parse(website);
        Intent i = new Intent(Intent.ACTION_VIEW, url);
        startActivity(i);
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

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();

        loadedDebug = false;

        //try to connect
        //SailfishSocketIO.setupSocket(this);
        //SailfishSocketIO.connect();

        //get current socket status
        if (SailfishSocketIO.isConnected)
            setConnectedText();
        else
            setDisconnectedText();

        setProfileInformation();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(onSocketConnectReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onSocketDisconnectReceiver);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        //if 4 fingers touch at once, open debug menu
        if (event.getPointerCount() == 4 && loadedDebug == false){

            Log.i(logTAG, "Entering debug menu");

            loadedDebug = true;
            Intent i = new Intent(this, DebugActivity.class);
            startActivity(i);

        }

        return true;
    }

    //Displays person email in Logcat if connected
    private void setProfileInformation(){
        try{
            String email = SailfishPreferences.getEmail(this);

            if(email != null){
                //display Person ID in logcat
                Log.d(logTAG, "Name: " + email);
                //display Person ID on Home screen
                setLoggedInEmailText(email);


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

