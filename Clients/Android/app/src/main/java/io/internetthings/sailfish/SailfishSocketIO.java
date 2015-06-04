package io.internetthings.sailfish;

/*
        Created by: Jason Maderski
        Date: 6/3/2015

        Notes: This class does SocketIO stuff...put more detailed description later
*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import java.io.ByteArrayOutputStream;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;

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

    public static void connect(String emailIn){

        email = emailIn;

        //call singleton so mSocket is instantiated
        SocketSingleton();

        Emitter.Listener onConnect = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.i("NoticeSocketIO", "NoticeSocketIO onConnect");
                mSocket.emit("join room", email);

            }
        };
        mSocket.on("connect", onConnect);
        mSocket.connect();
    }

    public static void disconnect(){
        mSocket.disconnect();

    }

    public static void attemptSend(String email, String message){

        mSocket.emit("send notification", email, message);
    }


}
