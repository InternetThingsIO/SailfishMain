package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.splunk.mint.Mint;

import io.internetthings.sailfish.ftue.SelectEmailActivity;

/*
        Created by: Jason Maderski
        Date: 6/10/2015

        Notes: Generates splashscreen
*/
public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
    }

    @Override
    protected void onStart(){
        super.onStart();

        //starts everything we need to do before the app finishes loading
        doStartupTasks();

}

    private void doStartupTasks(){

        //restart notif service first thing
        if (SailfishPreferences.getFTUECompleted(this))
            SailfishNotificationService.restartService(this);

        //have to do this in the main thread for some reason
        setupMint();

        //do not access the Android UI toolkit from outside the UI thread. that means below!!!!
        new Thread(new Runnable() {

            public void run() {

                startupTasks();

                //unnecessary delay, delete later
                try {
                    Thread.sleep(1000);
                }catch(Exception ex){}

                endStartup();

            }

            //put startup tasks here
            private void startupTasks(){

            }

            private void endStartup(){

                boolean FTUECompleted =
                        SailfishPreferences.getFTUECompleted(getApplicationContext());

                Class newActivity;

                if (FTUECompleted){

                    newActivity = MainActivity.class;

                }else{

                    //start FTUE here
                    newActivity = SelectEmailActivity.class;
                }

                Intent i = new Intent(SplashScreen.this, newActivity);
                startActivity(i);
                overridePendingTransition(0, R.animator.fadeout);

                finish();

            }

        }).start();

    }

    private void setupMint(){
        String email = SailfishPreferences.getEmail(this);
        //Line of code to add Splunk Mint to the project
        Mint.initAndStartSession(this, Constants.MINT_API_KEY);

        if (email != null)
            Mint.setUserIdentifier(email);

    }
}
