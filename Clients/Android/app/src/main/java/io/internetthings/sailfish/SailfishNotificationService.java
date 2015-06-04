package io.internetthings.sailfish;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
/*
    Created by: Jason Maderski
    Date: 6/2/2015

    Notes: NService(Notification Service) runs in the background and currently displays in the
           logcat window when an notification was posted or removed
 */


public class SailfishNotificationService extends NotificationListenerService{

    private final String logTAG = this.getClass().getName();
    public static final String MY_PREFS_NAME = "SailFishPref";

    private String email;

    private List<Object> recentNotifications = new ArrayList<Object>();



    public SailfishNotificationService(){}

    private boolean isDuplicate(StatusBarNotification notif){

        //if this notification is already in the hashmap it's a duplicate and we return true
        if (recentNotifications.contains(notif.getNotification().hashCode())) {
            Log.i(logTAG, "is Duplicate");
            return true;
        }

        recentNotifications.add(notif.getNotification().hashCode());

        if(recentNotifications.size()>1000){
            recentNotifications.remove(0);
            Log.i(logTAG, "size()>1 notification removed");
        }
        return false;

    }

    private void getPrefAndConnect() {

        if (SailfishSocketIO.SocketSingleton().connected())
            return;

        //get preferences
        try {
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_MULTI_PROCESS);
            this.email = prefs.getString("email", null);
        }catch(Exception ex){
            Log.e(logTAG, "Failed to get preferences");
            //throw ex;
        }

        //connect if we got an email
        if (email != null) {
            Log.e(logTAG, "Started service, found email: " + email);
            SailfishSocketIO.connect(email);
        }

    }

    //Displays notification posted with the ID in the logcat window
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Log.w("checkingOUT", "Notification POSTED " + "\n"
                + " Package Name: " + sbn.getPackageName()
                + "\n" + " ID: " + sbn.getId()
                + "\n" + " Tag: " + sbn.getTag()
                + "\n" + " onGoing: " + sbn.isOngoing()
                + "\n" + " isClearable: " + sbn.isClearable()
                //+ "\n" + " getkey: " + sbn.getKey()
                + "\n" + " getNumber: " + sbn.getNotification().number
        );
        try {
            Drawable icon = getPackageManager().getApplicationIcon(sbn.getPackageName());
            SailfishSocketIO.sendPackageImage(icon, sbn.getPackageName());
        }catch (Exception e){}

        SailfishNotification sn = new SailfishNotification("SubjectTEST", "BodyTEST", sbn.getPackageName(), sbn.getPostTime());
        Gson gson = new Gson();
        String json = gson.toJson(sn);

        Log.i("JSONTest", json);

        getPrefAndConnect();

        //stop execution if this is a duplicate
        //if (isDuplicate(sbn))
       //    return;



        //if (NoticeSocketIO.SocketSingleton().connected())
        SailfishSocketIO.attemptSend(email, json);

        /*try {
            String pack = sbn.getPackageName();
            //        String ticker = sbn.getNotification().tickerText.toString();
            Bundle extras = sbn.getNotification().extras;
            String title = extras.getString("android.title");
            String text = extras.getCharSequence("android.text").toString();
            NoticeSocketIO.attemptSend(title);
        }catch (Exception ex){}*/
    }

    //Displays notification that has been removed in the logcat window
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Log.w(logTAG, "Notification REMOVED*************** " + "User: "
                + " Package Name: " + sbn.getPackageName() + " ID: " + sbn.getId());
    }
}
