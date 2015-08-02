package io.internetthings.sailfish;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.net.ssl.SSLContext;

/*
        Created by: Jason Maderski
        Date: 6/3/2015

        Notes: This class does SocketIO stuff...put more detailed description later
*/

public class SailfishSocketIO {

    private Socket mSocket;
    private static final String logTAG = "SailfishSocketIO";

    public static Boolean isConnected = false;

    public boolean isConnected(){
        return mSocket.connected();
    }

    public void setupSocket(final Context context){

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

                isConnected = true;
            }
        };
        mSocket.on(Socket.EVENT_CONNECT, onConnect);


        Emitter.Listener onDisconnect = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.w(logTAG, "onDisconnect Listener");
                Intent intent = new Intent("onSocketDisconnect");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                isConnected = false;
            }
        };
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);


        Emitter.Listener onReconnected = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.w(logTAG, "NoticeSocketIO onReconnected");
                joinUsersRoom(context);
                isConnected = true;
            }
        };
        mSocket.on(Socket.EVENT_RECONNECT, onReconnected);


        Emitter.Listener onReconnectFailed = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.w(logTAG, "NoticeSocketIO onReconnectFailed");
                //mSocket.connect();
                isConnected = false;
            }
        };
        mSocket.on(Socket.EVENT_RECONNECT_FAILED, onReconnectFailed);


        //listener for removing notification
        Emitter.Listener onNotificationRemoved = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {


                //get the android ID
                String concatID = (String)args[0];

                Log.w(logTAG, "NoticeSocketIO dismiss_notif: " + concatID);

                String[] split = concatID.split(":");
                String id, tag, packageName;

                try {
                    //packagename:tag:id
                    if (split.length == 3) {
                        packageName = URLDecoder.decode(split[0], "utf-8");
                        id = URLDecoder.decode(split[2], "utf-8");
                        tag = URLDecoder.decode(split[1], "utf-8");

                        packageName = packageName.length() == 0 ? null : packageName;
                        id = id.length() == 0 ? null : id;
                        tag = tag.length() == 0? null : tag;

                        Log.w(logTAG, "Dismissing notification with package: " + packageName + " tag: " + tag + " id: " + id);

                        Intent intent = new Intent(Constants.NOTIFICATON_DISMISSED);
                        intent.putExtra("ID", id);
                        intent.putExtra("tag", tag);
                        intent.putExtra("pkg", packageName);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


                    }
                }catch(Exception ex){
                    Log.e(logTAG, "had some encoding exception or notification didn't exist, can't dismiss notification");
                }
            }
        };
        mSocket.on("dismiss_notif_device", onNotificationRemoved);

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
