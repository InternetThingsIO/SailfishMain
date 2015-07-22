package io.internetthings.sailfish;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.splunk.mint.Mint;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/*
    Created by: Jason Maderski
    Date: 6/2/2015

    Notes: NService(Notification Service) runs in the background and currently displays in the
           logcat window when an notification was posted or removed
 */


public class SailfishNotificationService extends NotificationListenerService{

    private final String logTAG = this.getClass().getName();
    public static final String MY_PREFS_NAME = "SailFishPref";

    private ArrayList<SailfishNotification> issuedNotifications = new ArrayList<>();

    public SailfishNotificationService(){
        SailfishSocketIO.setupSocket(this);
    }

    //start sticky so it restarts on crash :-)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent,flags, startId);

        Log.i(logTAG, "SailfishNotificationService starting");

        Mint.initAndStartSession(this, Constants.MINT_API_KEY);

        return START_STICKY;
    }

    private void getPrefAndConnect() {

        if (SailfishSocketIO.isConnected()) {
            Log.i(logTAG, "Socket is already connected");
            return;
        }

        String email = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);

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

        getPrefAndConnect();

        SailfishNotification sn = new SailfishNotification(sbn, this, MessageActions.POST_NOTIFICATION);

        updateDuplicates(sn);



        //don't issue this notification if it shouldn't be issued
        if (!canIssueNotif(sn))
            return;

        sendMessage(sn);

        //add current notif
        //clear image to save memory
        sn.clearImage();
        if (!issuedNotifications.contains(sn))
            issuedNotifications.add(sn);

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

    private Boolean canIssueNotif(SailfishNotification sn) {

        String email = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);

        if (email == null || email.length() == 0) {
            Log.w(logTAG, "Email is null, can't issue notification");
            return false;
        }

        //check to see if this is a duplicate
        if (isDuplicate(sn))
            return false;

        return true;
    }

    private boolean isDuplicate(SailfishNotification sn){
        if (issuedNotifications.contains(sn)){
            Log.i(logTAG, "Duplicate notification detected");
            return true;
        }

        return false;
    }

    private void updateDuplicates(SailfishNotification sn){

        Iterator<SailfishNotification> it =  issuedNotifications.iterator();

        SailfishNotification currNotif;
        long elapsedTime;

        //clear expired notifs
        while (it.hasNext()){
            currNotif = it.next();
            elapsedTime = new Date().getTime() - currNotif.CreatedDate.getTime();

            if (elapsedTime > Constants.NOTIF_EXPIRATION_MS) {
                it.remove();
                Log.i(logTAG, "Removing expired notif ID: " + currNotif.ID);
            }
        }

        Log.i(logTAG, "Issued Notifications Size: " + issuedNotifications.size());
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

        String email = SailfishPreferences.reader(this).getString(SailfishPreferences.EMAIL_KEY, null);

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

        //bail if this isn't a valid notification
        if (!isNotifValid(sbn))
            return;

        Log.i(logTAG, "Notification REMOVED*************** " + "User: "
                + " Package Name: " + sbn.getPackageName() + " ID: " + sbn.getId());


        SailfishNotification sn = new SailfishNotification(sbn, this, MessageActions.REMOVE_NOTIFICATION);

        if (issuedNotifications.contains(sn)) {
            Log.i(logTAG, "Removing existing notification from issuedNotifications");
            issuedNotifications.remove(sn);
        }

        sn.clearImage();
        sendMessage(sn);
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
        SailfishSocketIO.Close();
    }

}
