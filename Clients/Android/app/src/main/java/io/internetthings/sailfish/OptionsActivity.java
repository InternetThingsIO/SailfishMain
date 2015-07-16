package io.internetthings.sailfish;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.AccountPicker;

import io.internetthings.sailfish.ftue.ConfigureChromeActivity;
import io.internetthings.sailfish.ftue.SelectEmailActivity;


public class OptionsActivity extends Activity {

    static final int PICK_ACCOUNT_REQUEST =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
    }

    public void sendTestMSG(View view){
        NotificationActions.sendMSG(this, "Notice", "Hello!  This is a Test Message");
    }

    public void changeACCTEmail(View view){
        Intent i = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(i, PICK_ACCOUNT_REQUEST);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == PICK_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            SailfishPreferences.editor(this).putString(SailfishPreferences.EMAIL_KEY, accountName);
            SailfishPreferences.editor(this).commit();
            Log.i("Email: ", accountName);
            NotificationActions.toastMSG(getApplication(), "Set to: " + accountName);
            stopService(new Intent(this, SailfishNotificationService.class));
            Log.i("Service: ", "STOPPED");
            startService(new Intent(this, SailfishNotificationService.class));
            Log.i("Service: ", "STARTED");
        }
    }

    public void sendChromeSetupEmail(View view){
        ConfigureChromeActivity email = new ConfigureChromeActivity();
        email.sendEmail();
        NotificationActions.toastMSG(getApplication(), "Sent email to: " + SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null));
    }

    public void restartSetup(View view){
        Intent i = new Intent(this, SelectEmailActivity.class);
        startActivity(i);
    }

    public void backToMain(View view){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        this.finish();
    }

}
