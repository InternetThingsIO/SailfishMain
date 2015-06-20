package io.internetthings.sailfish;

import android.accounts.AccountManager;
import android.accounts.Account;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class FTUE extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftue);

        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");

        addRadioButtons(accounts);

        setupNextButton();

    }

    private void setupNextButton(){
        Button btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);

                if(rdoEmails.getCheckedRadioButtonId()!=-1){
                    int id = rdoEmails.getCheckedRadioButtonId();
                    RadioButton radioButton = (RadioButton) rdoEmails.findViewById(id);
                    String selectedEmail = (String) radioButton.getText();
                    Log.i("Email:", selectedEmail + "\n" + "RadioButton ID: " + String.valueOf(id));
                }
                
            }
        });

    }


    private void addRadioButtons(Account[] accounts){

        RadioGroup rdoEmails = (RadioGroup) findViewById(R.id.rdoEmails);

        //TextView title = new TextView(this);
        //title.setText("Select Email:");
        //title.setTextColor(Color.RED);
        //ll.addView(title);

        RadioButton rdoButton;

        for (Account acct : accounts){
            rdoButton = new RadioButton(this);
            rdoButton.setText(acct.name);
            rdoEmails.addView(rdoButton);
        }

        //ll.addView(rdoEmails);

    }
}
