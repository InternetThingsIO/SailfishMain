package io.internetthings.sailfish;

import android.app.NotificationManager;
import android.content.Context;
import java.lang.Math;
import android.support.v4.app.NotificationCompat;


/**
 * Created by Dev on 6/18/2015.
 */
public class SendNotification {

    private NotificationManager nManager = null;
    private int nID;
    private String sTAG = this.getClass().getName();
    private Context c;

    public SendNotification(Context context){
        c = context;
        nManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void SendMSG(String Message){
        nID = Math.abs((int)System.currentTimeMillis());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
            .setContentTitle("Notice Message")
            .setContentText(Message + "\n" + "Test Notification ID: " + nID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true);
        nManager.notify(sTAG, nID, builder.build());



    }
}
