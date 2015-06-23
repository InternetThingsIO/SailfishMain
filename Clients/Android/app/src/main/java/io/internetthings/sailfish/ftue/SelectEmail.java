package io.internetthings.sailfish.ftue;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import io.internetthings.sailfish.MainActivity;
import io.internetthings.sailfish.R;
import io.internetthings.sailfish.SailfishPreferences;

public class SelectEmail extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_email);

        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");

        addRadioButtons(accounts);

    }
    //Sets selected email and launches MainActivity when Next button is clicked
    public void onClickNext(View view){
        RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);

        if(rdoEmails.getCheckedRadioButtonId()!=-1){
            int id = rdoEmails.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton) rdoEmails.findViewById(id);
            SailfishPreferences.editor(this).putString(SailfishPreferences.EMAIL_KEY, (String) radioButton.getText());
            SailfishPreferences.editor(this).commit();
            //Log.i("Email:", selectedEmail + "\n" + "RadioButton ID: " + String.valueOf(id));
        }

        Intent i = new Intent(this, ConfigureChromeActivity.class);
        startActivity(i);
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
