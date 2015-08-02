package io.internetthings.sailfish;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


public class DebugActivity extends Activity {

    private final String logTag = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    @Override
    protected void onResume(){
        super.onResume();
        TextView status =  (TextView)findViewById(R.id.txtConnectedStatus);
        status.setText("Status: " + SailfishSocketIO.isConnected());
    }

    public void onClickSocketDisconnect(View view){
        SailfishSocketIO.disconnect();
    }

    public void onClickSocketConnect(View view){
        SailfishSocketIO.connect();
    }

    public void onClickGetNotifAccess(View view){
        if(!NotificationActions.checkNotificationAccess(getApplication()))
            NotificationActions.openNotificationAccess(getApplication());
        else
            NotificationActions.toastMSG(getApplication(), "You already gave us access, you sly dog!");
    }

    public void onClickGAuth(View view){
        Intent i = new Intent(this, GoogleAuth2Activity.class);
        startActivity(i);
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
        this.finish();
    }

    public void onClickSendEmail(View view){


    }

    public void onClickTestMutedPkg(View view){
        Intent i = new Intent(this, MutedPackagesActivity.class);
        startActivity(i);
        this.finish();
    }

    public void onClickRestartService(View view){
        stopService(new Intent(this, SailfishNotificationService.class));
        Log.i("Service: ", "STOPPED");
        startService(new Intent(this, SailfishNotificationService.class));
        Log.i("Service: ", "STARTED");
        Toast.makeText(getApplication(), "SailfishNotification Service Restarted",Toast.LENGTH_LONG).show();
    }
}
