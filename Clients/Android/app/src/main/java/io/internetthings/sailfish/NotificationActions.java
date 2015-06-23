package io.internetthings.sailfish;

import android.app.NotificationManager;
import android.content.Context;
import java.lang.Math;

import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;


/**
 * Created by Dev on 6/18/2015.
 */
public class NotificationActions {

    private static final String sTAG = "NotificationActions";

    //Send Text Message from Notice
    public static void SendMSG(Context context, String Message){
        NotificationManager nManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int nID = Math.abs((int)System.currentTimeMillis());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setContentTitle("Notice Message")
            .setContentText(Message + "\n" + "Test Notification ID: " + nID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true);
        nManager.notify(sTAG, nID, builder.build());
    }

    //Checks whether or not the NoticeNotificationService has access
    public static void checkNotificationAccess(Context context){

        String enabledAppList = Settings.Secure.getString(context.getContentResolver(),
                "enabled_notification_listeners");
        if(enabledAppList == null)
            enabledAppList = "None";

        boolean checkAppAccessFlag = enabledAppList.contains("SailfishNotificationService");

        if (!checkAppAccessFlag) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
