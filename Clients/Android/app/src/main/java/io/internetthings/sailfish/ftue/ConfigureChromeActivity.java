package io.internetthings.sailfish.ftue;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import io.internetthings.sailfish.MainActivity;
import io.internetthings.sailfish.R;

public class ConfigureChromeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_chrome);
    }

    public void onClickCompletedButton(View view){
        Intent i = new Intent(this, NotificationAccessActivity.class);
        startActivity(i);
    }


}
