package io.internetthings.sailfish.ftue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

    boolean emailCHECK = false;
    boolean NAccessCHECK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);
        checkSelectedEmail();
        checkNAccess();
    }

    private void checkSelectedEmail(){
        String check = "";
        check = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);

        if(check != null){
            CheckBox cb = (CheckBox) findViewById(R.id.emailSelected);
            cb.setChecked(true);
            emailCHECK = true;
        }

    }

    private void checkNAccess(){

            CheckBox cb = (CheckBox) findViewById(R.id.NAgranted);
            cb.setChecked(true);
            NAccessCHECK = true;

    }

    public void onClickFinished(View view){


            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);

            NotificationActions.sendMSG(this, "Hello, Notice has been successfully setup");
            NotificationActions.toastMSG(this, "Notice Test Message Sent");
            this.finish();



    }
}
