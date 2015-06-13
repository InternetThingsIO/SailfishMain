package io.internetthings.sailfish;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import java.net.URISyntaxException;

/*
        Created by: Jason Maderski
        Date: 6/3/2015

        Notes: This class does SocketIO stuff...put more detailed description later
*/

public class SailfishSocketIO {

    private static Socket mSocket;
    private static String email;
    private static final String logTAG = "SailfishSocketIO";

    public static Socket SocketSingleton(){
        if (mSocket == null) {

            try{
                mSocket = IO.socket("http://api.internetthings.io");
            }catch (URISyntaxException e){}

            if (!mSocket.connected()) {

            }

        }

        return mSocket;

    }

    public static void connect(String emailIn, final Context context){

        email = emailIn;

        //call singleton so mSocket is instantiated
        SocketSingleton();

        Emitter.Listener onConnect = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.i("NoticeSocketIO", "NoticeSocketIO onConnect");
                Intent intent = new Intent("onSocketConnect");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            }
        };
        mSocket.on("connect", onConnect);

        Emitter.Listener onDisconnect = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.i(logTAG, "onDisconnect Listener");
                Intent intent = new Intent("onSocketDisconnect");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        };
        mSocket.on("disconnect", onDisconnect);
        mSocket.connect();
    }

    public static void disconnect() {
        mSocket.disconnect();

    }

    public static void attemptSend(String email, String message){

        if(mSocket != null)
            mSocket.emit("send message", email, message);
    }

}
