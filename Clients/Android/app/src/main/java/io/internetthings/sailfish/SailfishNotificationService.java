package io.internetthings.sailfish;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

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

    public SailfishNotificationService(){}

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
        }else{
            Log.e(logTAG, "Email is NULL");
        }

        while(!SailfishSocketIO.SocketSingleton().connected()){
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
                + "\n" + " getNumber: " + sbn.getNotification().number
                + "\n" + " getActiveNotifications: " + getActiveNotifications().length
        );

        getPrefAndConnect();

        Drawable icon = null;
        try {
            icon = getPackageManager().getApplicationIcon(sbn.getPackageName());
        }catch (Exception e){}

        SailfishNotification sn = new SailfishNotification(icon,
                sbn.getNotification().extras.getString("android.title"),
                getBodyOfMessage(sbn),//sbn.getNotification().extras.getCharSequence("android.text").toString(),
                sbn.getPackageName(),
                sbn.getPostTime());

        sn.Action = MessageActions.POST_NOTIFICATION;
        sn.ID = getMessageID(sbn);
        sendMessage(sn);

    }

    private String getBodyOfMessage(StatusBarNotification sbn){
        String bom2String;
        CharSequence bodyOfMessage = sbn.getNotification().extras.getCharSequence("android.text");

        if(bodyOfMessage != null)
            return bom2String = bodyOfMessage.toString();
        else
            return "Message is NULL";
    }

    private String getMessageID(StatusBarNotification sbn){
        StringBuilder sb = new StringBuilder();
        sb.append(sbn.getPackageName());
        if(!TextUtils.isEmpty(sbn.getTag()))
            sb.append(sbn.getTag());
        if(!TextUtils.isEmpty(String.valueOf(sbn.getId())))
            sb.append(sbn.getId());

        return sb.toString();
    }

    private void sendMessage(SailfishMessage sm){
        Gson gson = new Gson();
        String json = gson.toJson(sm);
        Log.i("JSONTest", json);

        SailfishSocketIO.attemptSend(email, json);
    }

    //Displays notification that has been removed in the logcat window
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Log.w(logTAG, "Notification REMOVED*************** " + "User: "
                + " Package Name: " + sbn.getPackageName() + " ID: " + sbn.getId());
        SailfishMessage sm = new SailfishMessage();
        sm.Action = MessageActions.REMOVE_NOTIFICATION;
        sm.ID = getMessageID(sbn);
        sendMessage(sm);
    }
}
