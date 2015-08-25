package io.internetthings.sailfish.ftue;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.androidsx.rateme.RateMeDialog;

import io.internetthings.sailfish.EmailSender;
import io.internetthings.sailfish.MainActivity;
import io.internetthings.sailfish.NotificationActions;
import io.internetthings.sailfish.R;
import io.internetthings.sailfish.SailfishPreferences;

public class ConfigureChromeActivity extends Activity {

    private final String logTag = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_chrome);
        setEmail();
        sendEmail();
    }

    //Starts the NotificationAccess Activity on click of button in configure chrome activity
    public void onClickCompletedButton(View view){

        Boolean FTUECompleted = SailfishPreferences.getFTUECompleted(this); //.reader(this).getBoolean(SailfishPreferences.FTUE_COMPLETED_KEY, false);

        if (FTUECompleted){
            this.finish();
        }else {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

            //mark FTUE completed
            SailfishPreferences.setFTUECompleted(this, true);
            SailfishPreferences.commit(this);
        }

        //send Test Notification
        NotificationActions.sendMSG(this, "Notice", "Yay!  Notice has been successfully setup!");

    }

    //gets email that user selected in SelectEmail Activity and sets it in ConfigureChrome Activity
    private void setEmail(){

        String email = SailfishPreferences.getEmail(this);
        TextView ftueemail = (TextView)findViewById(R.id.FTUEEmail);
        ftueemail.setText("To: " + email);
        Log.i("To: ", email);
    }

    public void sendEmail() {

        EmailSender sender = new EmailSender();
        sender.sendEmail(this);
        sender = null;
    }

}
