package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();
                overridePendingTransition(0, R.animator.fadeout);

            }
        }, SPLASHSCREEN_TIME_OUT);

    }


}
