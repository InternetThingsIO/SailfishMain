package io.internetthings.sailfish;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class FTUE extends Activity {

    public static String selectedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftue);

        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");

        addRadioButtons(accounts);

    }
    //Sets selected email and launches MainActivity when Next button is clicked
    public void emailSelectedNext(View view){
        RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);

        if(rdoEmails.getCheckedRadioButtonId()!=-1){
            int id = rdoEmails.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton) rdoEmails.findViewById(id);
            selectedEmail = (String) radioButton.getText();
            Log.i("Email:", selectedEmail + "\n" + "RadioButton ID: " + String.valueOf(id));
        }
        backToMainActivity();
    }

    //dynamically adds radiobuttons for email selection
    private void addRadioButtons(Account[] accounts){

        RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);

        RadioButton rdoButton;

        for (Account acct : accounts){
            rdoButton = new RadioButton(this);
            rdoButton.setText(acct.name);
            rdoEmails.addView(rdoButton);
        }

    }
    //Sends user back to Main Activity
    private void backToMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
