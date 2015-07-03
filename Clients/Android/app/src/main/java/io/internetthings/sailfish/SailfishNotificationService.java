package io.internetthings.sailfish;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.gson.Gson;
import com.splunk.mint.Mint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

    public SailfishNotificationService(){
        SailfishSocketIO.setupSocket(this);
    }

    //start sticky so it restarts on crash :-)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(logTAG, "SailfishNotificationService starting");

        Mint.initAndStartSession(this, Constants.MINT_API_KEY);

        return START_STICKY;
    }

    private void getPrefAndConnect() {

        if (this.email == null){
            //get preferences
            this.email = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);
        }

        if (SailfishSocketIO.isConnected()) {
            Log.i(logTAG, "Socket is already connected");
            return;
        }



        //connect if we have an email
        if (email != null) {
            Log.e(logTAG, "Connecting to socket, found email: " + email);

            SailfishSocketIO.connect();

            Mint.setUserIdentifier(email);

        }else{
            Log.e(logTAG, "Email is NULL");
        }

    }

    //Displays notification posted with the ID in the logcat window
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){

        //bail if this isn't a valid notification
        if (!isNotifValid(sbn))
            return;

        Log.i("checkingOUT", "Notification POSTED " + "\n"
                        + " Package Name: " + sbn.getPackageName()
                        + "\n" + " ID: " + sbn.getId()
                        + "\n" + " Tag: " + sbn.getTag()
                        + "\n" + " onGoing: " + sbn.isOngoing()
                        + "\n" + " isClearable: " + sbn.isClearable()
                        + "\n" + " getNumber: " + sbn.getNotification().number
                        + "\n" + " getActiveNotifications: " + getActiveNotifications().length
        );

        //this.cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
        getPrefAndConnect();

        //don't issue this notification if it shouldn't be issued
        if (!canUseNotif(sbn))
            return;

        Drawable icon = null;
        try {
            icon = getPackageManager().getApplicationIcon(sbn.getPackageName());
        }catch (Exception e){}

        String bodyText = getBodyText(sbn);

        SailfishNotification sn = new SailfishNotification(icon,
                sbn.getNotification().extras.getString("android.title"),
                bodyText,
                sbn.getPackageName(),
                sbn.getPostTime(),
                sbn.getNotification().priority);

        sn.Action = MessageActions.POST_NOTIFICATION;
        sn.ID = getMessageID(sbn);
        sendMessage(sn);

    }

    private Boolean isNotifValid(StatusBarNotification sbn){
        if (sbn == null || sbn.getNotification() == null) {
            Log.w(logTAG, "sbn was null, notification is not valid");
            return false;
        }

        String bodyText = getBodyText(sbn);

        //if we have no body, don't send notifications.
        //this rids us of grouped notifications also
        if (bodyText == null) {
            Log.w(logTAG, "Notification body text is null");
            return false;
        }

        return true;

    }

    private Boolean canUseNotif(StatusBarNotification sbn) {

        if (email == null || email.length() == 0) {
            Log.w(logTAG, "Email is null, can't issue notification");
            return false;
        }

        return true;
    }

    private String getMessageID(StatusBarNotification sbn){
        StringBuilder sb = new StringBuilder();

        try {
            //packagename:tag:id
            sb.append(URLEncoder.encode(sbn.getPackageName(), "utf-8"));

            sb.append(":");

            if (!TextUtils.isEmpty(sbn.getTag()))
                sb.append(URLEncoder.encode(sbn.getTag(), "utf-8"));

            sb.append(":");

            if (!TextUtils.isEmpty(String.valueOf(sbn.getId())))
                sb.append(URLEncoder.encode(String.valueOf(sbn.getId()), "utf-8"));

            //String body = getBodyText(sbn);
            //if (body != null) {
            //    String trimmed = body.substring(0, Math.min(body.length(), 50));
            //    sb.append(trimmed);
            //}
        }catch (UnsupportedEncodingException ex){
            Log.e(logTAG, "Had some error with encoding type");
        }

        return sb.toString();
    }

    private String toBase64(String str){
        try {
            byte[] data = str.getBytes("UTF-8");
            return Base64.encodeToString(data, Base64.DEFAULT);
        }catch(Exception ex){
            return str;
        }
    }

    private String getBodyText(StatusBarNotification sbn){

        CharSequence body = sbn.getNotification().extras.getCharSequence("android.text");
        if (body != null)
            return body.toString();
        else
            return null;


    }

    private void sendMessage(SailfishMessage sm){

        Log.w(logTAG, "Sending message");

        Gson gson = new Gson();
        String json = gson.toJson(sm);
        Log.i("JSONTest", json);

        String token = GoogleAuth2Activity.getToken(this, email);

        if (!TextUtils.isEmpty(token))
            SailfishSocketIO.attemptSend(token, email, json);
        else
            Log.e(logTAG, "Token came back empty on sendMessage");
    }

    public String ReadBigStringIn(BufferedReader buffIn) throws IOException {
        StringBuilder everything = new StringBuilder();
        String line;
        while( (line = buffIn.readLine()) != null) {
            everything.append(line);
        }
        return everything.toString();
    }

    //Displays notification that has been removed in the logcat window
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Log.i(logTAG, "Notification REMOVED*************** " + "User: "
                + " Package Name: " + sbn.getPackageName() + " ID: " + sbn.getId());

        if (!canUseNotif(sbn))
            return;

        SailfishMessage sm = new SailfishMessage();
        sm.Action = MessageActions.REMOVE_NOTIFICATION;
        sm.ID = getMessageID(sbn);

        sendMessage(sm);
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
        SailfishSocketIO.Close();
    }

}
