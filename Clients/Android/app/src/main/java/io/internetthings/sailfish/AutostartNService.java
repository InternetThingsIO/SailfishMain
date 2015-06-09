package io.internetthings.sailfish;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
    Created by: Jason Maderski
    Date: 6/2/2015

    Notes: This class automatically starts NService when the phone boots up
 */

public class AutostartNService extends BroadcastReceiver{
    public void onReceive(Context arg0, Intent argl){
        Intent intent = new Intent(arg0,SailfishNotificationService.class);
        arg0.startService(intent);
        Log.i("AutoSTART", "Notice Notification service AUTO-Started");
    }


}
