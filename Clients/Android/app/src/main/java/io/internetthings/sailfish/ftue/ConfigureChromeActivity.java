package io.internetthings.sailfish.ftue;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.internetthings.sailfish.MainActivity;
import io.internetthings.sailfish.R;
import io.internetthings.sailfish.SailfishPreferences;

public class ConfigureChromeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_chrome);
        setEmail();
    }

    //Starts the NotificationAccess Activity on click of button in configure chrome activity
    public void onClickCompletedButton(View view){
        Intent i = new Intent(this, NotificationAccessActivity.class);
        startActivity(i);

        this.finish();
    }

    //gets email that user selected in SelectEmail Activity and sets it in ConfigureChrome Activity
    private void setEmail(){

        String email = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);
        TextView ftueemail = (TextView)findViewById(R.id.FTUEEmail);
        ftueemail.setText("To: " + email);
        Log.i("To: ", email);
    }


}
