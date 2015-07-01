package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import io.internetthings.sailfish.ftue.SelectEmailActivity;

/*
        Created by: Jason Maderski
        Date: 6/10/2015

        Notes: Generates splashscreen
*/
public class SplashScreen extends Activity {

    //Splash screen time displayed
    private static int SPLASHSCREEN_TIME_OUT = 0;

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
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, R.animator.fadeout);
            }
        }, SPLASHSCREEN_TIME_OUT);
        */

}

    private void doStartupTasks(){

        //do not access the Android UI toolkit from outside the UI thread. that means below!!!!
        new Thread(new Runnable() {

            public void run() {

                startupTasks();

                //unnecessary delay, delete later
                try {
                    Thread.sleep(500);
                }catch(Exception ex){}

                endStartup();

            }

            //put startup tasks here
            private void startupTasks(){

            }

            private void endStartup(){

                boolean FTUECompleted =
                        SailfishPreferences.reader(getApplicationContext())
                                .getBoolean(SailfishPreferences
                                        .FTUE_COMPLETED_KEY, false);

                Class newActivity;

                if (FTUECompleted){

                    newActivity = MainActivity.class;

                }else{

                    //start FTUE here

                    newActivity = SelectEmailActivity.class;
                }

                Intent i = new Intent(SplashScreen.this, newActivity);
                startActivity(i);
                finish();
                overridePendingTransition(0, R.animator.fadeout);

            }

        }).start();

    }
}
