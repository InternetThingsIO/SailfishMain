package io.internetthings.sailfish.ftue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import io.internetthings.sailfish.MainActivity;
import io.internetthings.sailfish.NotificationActions;
import io.internetthings.sailfish.R;
import io.internetthings.sailfish.SailfishPreferences;

public class CheckListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);
        checkChromeAccess();
        checkSelectedEmail();
        checkNAccess();
    }

    private boolean checkSelectedEmail(){
        String check = "";
        check = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);

        if(check != null){
            CheckBox cb = (CheckBox) findViewById(R.id.emailSelected);
            cb.setChecked(true);
            return true;
        }
        //NotificationActions.toastMSG(this, "No email was selected");
        return false;
    }

    private boolean checkNAccess(){

        boolean hasAccess = NotificationActions.checkNotificationAccess(this);

        CheckBox cb = (CheckBox) findViewById(R.id.NAgranted);
        cb.setChecked(hasAccess);

        return hasAccess;
    }

    private boolean checkChromeAccess(){

        CheckBox cb = (CheckBox) findViewById(R.id.configuredChrome);
        cb.setChecked(true);
        return true;
    }

    public void onClickFinished(View view){


            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);


            this.finish();



    }
}
