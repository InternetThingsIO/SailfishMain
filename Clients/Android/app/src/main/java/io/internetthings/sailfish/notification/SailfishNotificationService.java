package io.internetthings.sailfish.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import io.internetthings.sailfish.GoogleAuth2Activity;
import io.internetthings.sailfish.SailfishPreferences;


/*
    Created by: Jason Maderski
    Date: 6/2/2015

    Notes: NService(Notification Service) runs in the background and currently displays in the
           logcat window when an notification was posted or removed
 */


public class SailfishNotificationService extends NotificationListenerService{

    private final String logTAG = this.getClass().getName();

    private static SailfishSocketIO socket;
    public static MutedPackages mutedPackages;
    public static AutoDismissPackages autoDismissPackages;

    private HashSet<String> PkgWhiteList;

    public static void restartService(Context context){
        context.stopService(new Intent(context, SailfishNotificationService.class));
        context.startService(new Intent(context, SailfishNotificationService.class));

    }

    public static boolean socketIsConnected(){
        if (socket != null)
            return socket.isConnected();
        else
            return false;
    }

    public static void socketConnect(){
        if (socket != null)
            socket.connect();
    }

    public static void socketDisconnect(){
        if (socket != null)
            socket.disconnect();
    }

    private void doStartup(){

        Log.w(logTAG, "Doing startup!!");

        if (socket == null){
            socket = new SailfishSocketIO(this);
            socket.connect();
        }


        mutedPackages = new MutedPackages(this);

        //create white list
        PkgWhiteList = new HashSet<>();
        PkgWhiteList.add("com.google.android.dialer");
        PkgWhiteList.add("com.skype.raider");
        PkgWhiteList.add("com.viber.voip");
        PkgWhiteList.add("com.skype.android");
        PkgWhiteList.add("com.whatsapp");
        PkgWhiteList.add("com.android.providers.downloads");

    }

    //start sticky so it restarts on crash :-)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        doStartup();

        Log.e(logTAG, "Service Started");

        return START_STICKY;
    }

    private void getPrefAndConnect() {

        if (socket.isConnected()) {
            Log.i(logTAG, "Socket is already connected");
            return;
        }

        String email = SailfishPreferences.getEmail(this);

        //connect if we have an email
        if (email != null) {
            Log.e(logTAG, "Connecting to socket, found email: " + email);
            socket.connect();
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
                        + "\n" + " body: " + sbn.getNotification().extras.getCharSequence("android.text")
                        + "\n" + " inboxStyle: " + Arrays.toString(sbn.getNotification().extras.getCharSequenceArray("android.textLines"))
        );

        getPrefAndConnect();
        SailfishNotification sn = new SailfishNotification(sbn, this);
        SailfishMessage message = new SailfishMessage(sbn, MessageActions.POST_NOTIFICATION, sn);

        sendMessage(message);

    }

    private Boolean isNotifValid(StatusBarNotification sbn){

        if (PkgWhiteList == null)
            return false;

        if (sbn == null || sbn.getNotification() == null) {
            Log.w(logTAG, "sbn was null, notification is not valid");
            return false;
        }

        String bodyText = getBodyText(sbn);

        //if we have no body, don't send notifications.
        //this rids us of grouped notifications also
        /*if (bodyText == null) {
            Log.w(logTAG, "Notification body text is null");
            return false;
        }*/

        //if ongoing or not clearable and not on white list, return false
        if (sbn.isClearable() == false || sbn.isOngoing() == true){
            if (!PkgWhiteList.contains(sbn.getPackageName())) {
                Log.i(logTAG, "Package: " + sbn.getPackageName() + " is NOT on the white list");
                return false;
            }
            Log.i(logTAG, "Package: " + sbn.getPackageName() + " IS on the white list");
        }

        if (mutedPackages.isMuted(sbn.getPackageName())) {
            Log.i(logTAG, "Package: " + sbn.getPackageName() + " is muted");
            return false;
        }

        if(autoDismissPackages.isAutoDismissed(sbn.getPackageName())){
            dismissNotification(sbn);
            Log.i(logTAG, "Package: " + sbn.getPackageName() + " is auto-dismissed");
            return false;
        }

        //don't issue notifcation if app already has list and this is not a list
        if (!notifIsList(sbn) && hasList(sbn.getPackageName()))
            return false;

        return true;

    }

    private boolean hasList(String packageName){

            StatusBarNotification[] activeNotifs = getActiveNotifications();
            for (StatusBarNotification sbn : activeNotifs) {
                if (sbn.getPackageName().compareTo(packageName) == 0) {
                    if (notifIsList(sbn))
                        return true;
                }
            }

            return false;
    }

    private boolean notifIsList(StatusBarNotification sbn){

        if(sbn.getNotification().extras.getCharSequenceArray("android.textLines") == null)
            return false;

        return true;
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

        String email = SailfishPreferences.getEmail(this);

        String token = GoogleAuth2Activity.getToken(this, email);

        if (!TextUtils.isEmpty(token))
            socket.attemptSend(token, email, json);
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


        SailfishMessage sn = new SailfishMessage(sbn, MessageActions.REMOVE_NOTIFICATION);

        sendMessage(sn);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (socket != null) {
            socket.Close();
            socket = null;
        }

        Log.e(logTAG, "Service Stopped");
    }

    private void dismissNotificationOld(String PackageName, String tag, int id, String key){
        if(Build.VERSION.SDK_INT < 21){
            cancelNotification(PackageName, tag, id);
        }else{
            cancelNotification(key);
        }

        Log.i("cancel", tag + " " + Integer.toString(id));

    }

    private void dismissNotification(StatusBarNotification sbn){
        if(Build.VERSION.SDK_INT < 21){
            cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
        }else{
            cancelNotification(sbn.getKey());
        }

        Log.i("cancel", sbn.getTag() + " " + Integer.toString(sbn.getId()));
    }

}
