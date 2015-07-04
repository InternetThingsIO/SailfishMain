package io.internetthings.sailfish;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class DebugActivity extends ActionBarActivity {

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
        final String sendTo = "itsailfish123@gmail.com";
        final String sendFrom = "itsailfish123@gmail.com";
        final String sender = "itsailfish123@gmail.com";
        final String senderPw = "GeoJasTer3";

        final EmailSender emailSender = new EmailSender(sender, senderPw);
        new AsyncTask<Void, Void, Void>() {
            @Override public Void doInBackground(Void... arg) {
                try {
                    Log.d("emailSender", "I ran");
                    emailSender.sendMail("This is Subject",
                            "This is Body",
                            sendTo,
                            sendFrom);
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
                return null;}

        }.execute();
    }
}
