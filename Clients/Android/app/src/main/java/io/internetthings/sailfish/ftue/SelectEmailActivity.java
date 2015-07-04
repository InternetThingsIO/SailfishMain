package io.internetthings.sailfish.ftue;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.splunk.mint.Mint;

import io.internetthings.sailfish.GoogleAuth2Activity;
import io.internetthings.sailfish.MainActivity;
import io.internetthings.sailfish.R;
import io.internetthings.sailfish.SailfishPreferences;

public class SelectEmailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_email);

        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");

        addRadioButtons(accounts);

    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    //Sets selected email and launches MainActivity when Next button is clicked
    public void onClickNext(View view){

        RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);

        if(rdoEmails.getCheckedRadioButtonId()!=-1){
            int id = rdoEmails.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton) rdoEmails.findViewById(id);
            SailfishPreferences.editor(this).putString(SailfishPreferences.EMAIL_KEY, (String) radioButton.getText());
            SailfishPreferences.editor(this).commit();
            
            //set email used by mint
            Mint.setUserIdentifier((String) radioButton.getText());

            Intent i = new Intent(this, GoogleAuth2Activity.class);
            startActivity(i);

        }else{
            changePlsSelectEmailTxt();
        }

    }

    //dynamically adds radiobuttons for email selection
    private void addRadioButtons(Account[] accounts){

        RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);
        rdoEmails.removeAllViews();

        RadioButton rdoButton;

        for (Account acct : accounts){
            rdoButton = new RadioButton(this);
            rdoButton.setText(acct.name);
            rdoEmails.addView(rdoButton);
        }

    }

    //changes Please select email text to bold red
    private void changePlsSelectEmailTxt(){
        TextView PlsSelectEmail = (TextView)findViewById(R.id.SetupMSG);
        PlsSelectEmail.setTextColor(getResources().getColor(R.color.Red));
        PlsSelectEmail.setTypeface(Typeface.DEFAULT_BOLD);
    }


}
