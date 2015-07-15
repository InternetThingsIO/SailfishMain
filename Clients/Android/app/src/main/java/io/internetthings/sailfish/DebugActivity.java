package io.internetthings.sailfish;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.Activity;


public class DebugActivity extends Activity {

    private final String logTag = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //hooked up to button via activity XML
    public void onClickTestMint(View view) {
        Object nullReference = null;

        //should get a null reference exception here
        nullReference.toString();
    }

    public void onClickExit(View view){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void onClickSendEmail(View view){

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
