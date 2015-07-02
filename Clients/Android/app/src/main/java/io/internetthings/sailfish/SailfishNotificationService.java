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
    }

    //start sticky so it restarts on crash :-)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(logTAG, "SailfishNotificationService starting");

        Mint.initAndStartSession(this, Constants.MINT_API_KEY);

        getPrefAndConnect();

        return START_STICKY;
    }

    private void getPrefAndConnect() {

        if (SailfishSocketIO.SocketSingleton().connected())
            return;

        //get preferences
        this.email = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);

        //connect if we have an email
        if (email != null) {
            Log.e(logTAG, "Started service, found email: " + email);
            SailfishSocketIO.connect(email, getApplicationContext());

            Mint.setUserIdentifier(email);

        }else{
            Log.e(logTAG, "Email is NULL");
        }

        //wait until we are connected
        //while(!SailfishSocketIO.SocketSingleton().connected()){}

    }

    //Displays notification posted with the ID in the logcat window
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){

        //bail if this isn't a valid notification
        if (!isNotifValid(sbn))
            return;

        Log.w("checkingOUT", "Notification POSTED " + "\n"
                        + " Package Name: " + sbn.getPackageName()
                        + "\n" + " ID: " + sbn.getId()
                        + "\n" + " Tag: " + sbn.getTag()
                        + "\n" + " onGoing: " + sbn.isOngoing()
                        + "\n" + " isClearable: " + sbn.isClearable()
                        + "\n" + " getNumber: " + sbn.getNotification().number
                        + "\n" + " getActiveNotifications: " + getActiveNotifications().length
        );

        //don't issue this notification if it shouldn't be issued
        if (!canUseNotif(sbn))
            return;


        getPrefAndConnect();

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
        if (sbn == null || sbn.getNotification() == null)
            return false;

        String bodyText = getBodyText(sbn);

        //if we have no body, don't send notifications.
        //this rids us of grouped notifications also
        if (bodyText == null)
            return false;

        return true;

    }

    private Boolean canUseNotif(StatusBarNotification sbn) {

        if (email == null || email.length() == 0)
            return false;

        return true;
    }

    private String getMessageID(StatusBarNotification sbn){
        StringBuilder sb = new StringBuilder();

        sb.append(sbn.getPackageName());

        if(!TextUtils.isEmpty(sbn.getTag()))
            sb.append(sbn.getTag());

        if(!TextUtils.isEmpty(String.valueOf(sbn.getId())))
            sb.append(sbn.getId());

        //String body = getBodyText(sbn);
        //if (body != null) {
        //    String trimmed = body.substring(0, Math.min(body.length(), 50));
        //    sb.append(trimmed);
        //}

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
        Gson gson = new Gson();
        String json = gson.toJson(sm);
        Log.i("JSONTest", json);

        String token = getToken();

        if (!TextUtils.isEmpty(token))
            SailfishSocketIO.attemptSend(token, email, json);
        else
            Log.e(logTAG, "Token came back empty on sendMessage");
    }

    private String getToken(){
        String scopes = "oauth2:https://www.googleapis.com/auth/userinfo.email";
        String token = null;
        try {
            token = GoogleAuthUtil.getToken(getApplicationContext(), email, scopes);
        } catch (IOException e) {
            Log.e(logTAG, e.getMessage());
        } catch (UserRecoverableAuthException e) {
            //startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
        } catch (GoogleAuthException e) {
            switch (Log.e(logTAG, e.getMessage())) {
            }
        }

        //make a request with the token

        BufferedReader in = null;
        String data = null;

        return token;

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
        Log.w(logTAG, "Notification REMOVED*************** " + "User: "
                + " Package Name: " + sbn.getPackageName() + " ID: " + sbn.getId());

        if (!canUseNotif(sbn))
            return;

        SailfishMessage sm = new SailfishMessage();
        sm.Action = MessageActions.REMOVE_NOTIFICATION;
        sm.ID = getMessageID(sbn);

        sendMessage(sm);
    }

}
