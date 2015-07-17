package io.internetthings.sailfish.ftue;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import io.internetthings.sailfish.EmailSender;
import io.internetthings.sailfish.MainActivity;
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
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);

        //mark FTUE completed
        SailfishPreferences.editor(this).putBoolean(SailfishPreferences.FTUE_COMPLETED_KEY, true);
        SailfishPreferences.editor(this).commit();

        this.finish();
    }

    //gets email that user selected in SelectEmail Activity and sets it in ConfigureChrome Activity
    private void setEmail(){

        String email = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);
        TextView ftueemail = (TextView)findViewById(R.id.FTUEEmail);
        ftueemail.setText("To: " + email);
        Log.i("To: ", email);
    }

    public void sendEmail(){

        final String recipient = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);

        //bail if we don't have an email for some reason
        if (recipient == null) {
            Log.e(logTag, "Couldn't send an email because we had no email address");
            return;
        }

        final EmailSender emailSender = new EmailSender();
        new AsyncTask<Void, Void, Void>() {
            @Override public Void doInBackground(Void... arg) {

                Log.d(logTag, "I ran " + "sent to: " + recipient);
                emailSender.sendMail(recipient);
                return null;
            }
        }.execute();
    }


}
