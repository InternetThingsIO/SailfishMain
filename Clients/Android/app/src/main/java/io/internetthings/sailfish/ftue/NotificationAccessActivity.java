package io.internetthings.sailfish.ftue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.internetthings.sailfish.NotificationActions;
import io.internetthings.sailfish.R;

public class NotificationAccessActivity extends Activity {

    static final String shouldCheckAccess = "shouldCheckAccess";

    private boolean askedNotifAccess = false;

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

        if (askedNotifAccess) {
            if (NotificationActions.checkNotificationAccess(this))
                goNextActivity();
            else
                NotificationActions.toastMSG(this, "Oops..  Looks like you didn't give us access");
        }
        askedNotifAccess = false;
    }

    public void onClickNext(View view){

        //check for notification access and ask for it if we don't have it
        if(!NotificationActions.checkNotificationAccess(getApplication())) {
            askedNotifAccess = true;
            NotificationActions.openNotificationAccess(getApplication());
        }else {
            NotificationActions.toastMSG(getApplication(), "You already gave us access, you sly dog!");
            goNextActivity();
        }
    }

    public void goNextActivity(){
        Intent i = new Intent(this, ConfigureChromeActivity.class);
        startActivity(i);
        this.finish();
    }
}
