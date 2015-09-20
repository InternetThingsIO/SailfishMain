package io.internetthings.sailfish;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.util.DisplayMetrics;

import com.google.android.gms.common.AccountPicker;

import io.internetthings.sailfish.ftue.ConfigureChromeActivity;
import io.internetthings.sailfish.ftue.SelectEmailActivity;


public class OptionsActivity extends Activity {

    static final int PICK_ACCOUNT_REQUEST =1;
    private final String logTAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        setVersionText();
    }

    //Displays version number
    private void setVersionText(){
        String versionInfo;
        PackageInfo pkgInfo = null;
        TextView tv = (TextView) findViewById(R.id.versionTxt);
        try {
            pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch(Exception e){
            Log.e(logTAG, e.getMessage());
        }
        versionInfo = "v" + pkgInfo.versionName.toString()
                + " build: " + pkgInfo.versionCode;

        DisplayMetrics metrics = getResources().getDisplayMetrics();

       versionInfo += " " + metrics.densityDpi + "dpi";

        tv.setText(versionInfo);
    }

    public void sendTestMSG(View view){
        NotificationActions.sendMSG(this, "Notice", "Hello!  This is a Test Message");
    }
    //Launches window to display gmail accounts for user to choose
    public void changeACCTEmail(View view){
        Intent i = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(i, PICK_ACCOUNT_REQUEST);
    }
    //User clicks ok and this method sets the selected email as the EMAIL_KEY, then stops and starts the service
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == PICK_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            SailfishPreferences.setEmail(this, accountName); //editor(this).putString(SailfishPreferences.EMAIL_KEY, accountName);
            SailfishPreferences.commit(this);
            Log.i("Email: ", accountName);

            NotificationActions notif = new NotificationActions();

            notif.toastMSG(getApplication(), "Set to: " + accountName);

            //We have to ask the user to grant permissions for this new email if they haven't already been granted.
            Intent i = new Intent(this, GoogleAuth2Activity.class);
            startActivity(i);
        }
    }

    public void startChromeSetup(View view){
        Intent i = new Intent(this, ConfigureChromeActivity.class);
        startActivity(i);
    }

    public void restartSetup(View view){

        SailfishPreferences.setFTUECompleted(this, false);
        SailfishPreferences.commit(this);

        Intent i = new Intent(this, SelectEmailActivity.class);
        startActivity(i);
    }

    public void mutedApps(View view){
        Intent i = new Intent(this, MutedPackagesActivity.class);
        startActivity(i);
    }

    public void autoDismissApps(View view){
        Intent i = new Intent(this, AutoDismissActivity.class);
        startActivity(i);
    }

    public void backToMain(View view){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        this.finish();
    }

}
