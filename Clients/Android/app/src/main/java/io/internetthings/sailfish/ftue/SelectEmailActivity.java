package io.internetthings.sailfish.ftue;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.splunk.mint.Mint;

import io.internetthings.sailfish.GoogleAuth2Activity;
import io.internetthings.sailfish.R;
import io.internetthings.sailfish.SailfishPreferences;

public class SelectEmailActivity extends Activity {

    private final String logTag = this.getClass().getName();

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
    public void onClick(View view){

        RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);

        if(rdoEmails.getCheckedRadioButtonId()!=-1){
            int id = rdoEmails.getCheckedRadioButtonId();
            RadioButton radioButton = (RadioButton) rdoEmails.findViewById(id);
            SailfishPreferences.setEmail(this, radioButton.getText().toString());
            SailfishPreferences.commit(this);
            
            //set email used by mint
            Mint.setUserIdentifier(radioButton.getText().toString());

            Intent i = new Intent(this, GoogleAuth2Activity.class);
            startActivity(i);

        }

    }

    //no longer dynamically adds radiobuttons for email selection *in old woman voice
    private void addRadioButtons(Account[] accounts){

        RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);
        RadioButton rdoButton;

        int number = rdoEmails.getChildCount();

        for (int i=0; i<number; i++){
            rdoEmails.getChildAt(i).setVisibility(View.INVISIBLE);
        }

        for (int i=0; i<accounts.length; i++){

            if (i < number) {
                rdoButton = (RadioButton) rdoEmails.getChildAt(i);
                //rdoButton.setButtonDrawable(R.drawable.custom_rdobtn);
                rdoButton.setText(accounts[i].name);
                //rdoButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18F);
                rdoButton.setVisibility(View.VISIBLE);

            }
        }

    }


}
