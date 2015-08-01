package io.internetthings.sailfish;

import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by Dev on 6/8/2015.
 */
public class SailfishMessage {

    public MessageActions Action;
    public String ID;
    public Date CreatedDate;
    public Object Payload;
    public String messageVersion = "1.0";

    public SailfishMessage(StatusBarNotification sbn, MessageActions action){
        setupMessage(sbn, action, null);
    }

    public SailfishMessage(StatusBarNotification sbn, MessageActions action, Object payload){
        setupMessage(sbn, action, payload);
    }

    private void setupMessage(StatusBarNotification sbn, MessageActions action, Object payload){
        this.ID = getMessageID(sbn);
        this.CreatedDate = new Date();
        this.Action = action;
        this.Payload = payload;
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

        }catch (UnsupportedEncodingException ex){
            Log.e(this.getClass().getName(), "Had some error with encoding type");
        }

        return sb.toString();
    }


}
