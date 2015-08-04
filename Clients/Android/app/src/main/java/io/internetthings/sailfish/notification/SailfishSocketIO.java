package io.internetthings.sailfish.notification;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.net.URLDecoder;

import io.internetthings.sailfish.GoogleAuth2Activity;
import io.internetthings.sailfish.SailfishPreferences;
import io.internetthings.sailfish.notification.SailfishNotificationService;


/*
        Created by: Jason Maderski
        Date: 6/3/2015

        Notes: This class does SocketIO stuff...put more detailed description later
*/

public class SailfishSocketIO {

    private Socket mSocket;
    private static final String logTAG = "SailfishSocketIO";

    public boolean isConnected(){
        return mSocket.connected();
    }

    public SailfishSocketIO(SailfishNotificationService context){
        setupSocket(context);
    }

    private void setupSocket(final SailfishNotificationService context){

        if (mSocket != null) {
            Log.i(logTAG, "Socket was already setup, we won't do it again");
            return;
        }

        Log.i(logTAG, "Setting up socket");

        try{

            mSocket = IO.socket("https://api.internetthings.io");

            //optimize some stuff for battery life
            mSocket.io().reconnection(true);
            mSocket.io().reconnectionDelay(5000);
            mSocket.io().reconnectionDelayMax(90000);

        }catch (URISyntaxException e){}

        Emitter.Listener onConnect = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.w(logTAG, "NoticeSocketIO onConnect");
                Intent intent = new Intent("onSocketConnect");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                joinUsersRoom(context);
            }
        };
        mSocket.on(Socket.EVENT_CONNECT, onConnect);


        Emitter.Listener onDisconnect = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.w(logTAG, "onDisconnect Listener");
                Intent intent = new Intent("onSocketDisconnect");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        };
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);


        Emitter.Listener onReconnected = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.w(logTAG, "NoticeSocketIO onReconnected");
                joinUsersRoom(context);
            }
        };
        mSocket.on(Socket.EVENT_RECONNECT, onReconnected);


        Emitter.Listener onReconnectFailed = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.w(logTAG, "NoticeSocketIO onReconnectFailed");
                //mSocket.connect();
            }
        };
        mSocket.on(Socket.EVENT_RECONNECT_FAILED, onReconnectFailed);


        //listener for removing notification
        Emitter.Listener onReceiveMessage = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                if (args[0] instanceof org.json.JSONObject){
                    Log.i(logTAG, "Notif dismissed");

                    Gson g = new Gson();
                    SailfishMessage msg = g.fromJson(args[0].toString(), SailfishMessage.class);

                    if (msg.Action == MessageActions.MUTE_NOTIFICATION){
                        mutePackage(context, msg.ID);
                    }else if (msg.Action == MessageActions.REMOVE_NOTIFICATION){
                        dismissNotif(context, msg.ID);
                    }

                }

            }
        };
        mSocket.on("dismiss_notif_device", onReceiveMessage);

    }

    private class packageDetails{
        public String id = null;
        public String tag = null;
        public String packageName = null;
    }

    private packageDetails getPackageDetails(String concatID){
        String[] split = concatID.split(":");
        packageDetails details = new packageDetails();

        try {
            //packagename:tag:id
            if (split.length == 3) {
                details.packageName = URLDecoder.decode(split[0], "utf-8");
                details.id = URLDecoder.decode(split[2], "utf-8");
                details.tag = URLDecoder.decode(split[1], "utf-8");

                details.packageName = details.packageName.length() == 0 ? null : details.packageName;
                details.id = details.id.length() == 0 ? null : details.id;
                details.tag = details.tag.length() == 0? null : details.tag;

                return details;

            }
        }catch(Exception ex){
            Log.e(logTAG, "had some encoding exception or notification didn't exist, can't dismiss notification");
        }

        return new packageDetails();
    }

    private void mutePackage(SailfishNotificationService context, String concatID){
        packageDetails details = getPackageDetails(concatID);
        SailfishNotificationService.mutedPackages.mutePackage(details.packageName, context);

    }

    private void dismissNotif(SailfishNotificationService context, String concatID){
        packageDetails details = getPackageDetails(concatID);
        try {
            context.cancelNotification(details.packageName, details.tag, Integer.parseInt(details.id));
        }catch(Exception e){
            Log.e(logTAG, "Couldn't dismiss notification message: " + e.getMessage());
        }
    }

    public void disconnect() {
        mSocket.disconnect();

    }

    public void connect(){
        if (!mSocket.connected())
            mSocket.connect();
    }

    public void joinUsersRoom(Context context){
        String email = SailfishPreferences.getEmail(context);
        if (email != null) {
            Log.w(logTAG, "Joining user's room");
            joinRoom(GoogleAuth2Activity.getToken(context, email), email);
        }
        else
            Log.e(logTAG, "email was null for some reason in joinUsersRoom");
    }

    public void attemptSend(String token, String email, String message){

        if(mSocket != null) {
            mSocket.emit("send message", token, email, message);
        }else{
            Log.e(logTAG, "mSocket was null for some reason in attemptSend");
        }
    }

    public void joinRoom(String token, String room){
        if(mSocket != null) {
            mSocket.emit("join room", token, room);
        }else{
            Log.e(logTAG, "mSocket was null for some reason in joinRoom");
        }
    }

    public void Close(){

        mSocket.disconnect();
        mSocket.off();
        mSocket.close();
        mSocket = null;

    }
}
