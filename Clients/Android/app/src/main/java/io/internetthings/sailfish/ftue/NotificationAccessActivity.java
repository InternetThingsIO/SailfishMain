package io.internetthings.sailfish.ftue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import io.internetthings.sailfish.NotificationActions;
import io.internetthings.sailfish.R;

public class NotificationAccessActivity extends Activity {

    static final String shouldCheckAccess = "shouldCheckAccess";

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_access);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().getExtras()!= null && getIntent().getExtras().getBoolean(shouldCheckAccess, false)) {
            if(NotificationActions.checkNotificationAccess(this)) {
                Intent i = new Intent(this, ConfigureChromeActivity.class);
                startActivity(i);
                this.finish();
            }else
                NotificationActions.toastMSG(this, "Opps...looks like you didn't give us access!");
        }
    }

    public void onClickNext(View view){

        //load next activity
        Intent i = new Intent(this, NotificationAccessActivity.class);
        i.putExtra(shouldCheckAccess, true);
        startActivity(i);

        //check for notification access and ask for it if we don't have it
        if(!NotificationActions.checkNotificationAccess(getApplication()))
            NotificationActions.openNotificationAccess(getApplication());
        else
            NotificationActions.toastMSG(getApplication(), "You already gave us access, you sly dog!");

    }
}
