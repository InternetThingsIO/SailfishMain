package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.internetthings.sailfish.ftue.ConfigureChromeActivity;
import io.internetthings.sailfish.ftue.SelectEmailActivity;


public class OptionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
    }

    public void sendTestMSG(View view){
        NotificationActions.sendMSG(this, "Notice", "Hello!  This is a Test Message");
    }

    public void changeACCTEmail(View view){
        Intent i = new Intent(this, SelectEmailActivity.class);
        startActivity(i);
    }

    public void sendChromeSetupEmail(View view){
        ConfigureChromeActivity email = new ConfigureChromeActivity();
        email.sendEmail();
    }

    public void backToMain(View view){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        this.finish();
    }

}
