package io.internetthings.sailfish.notification;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;
import io.internetthings.sailfish.NotificationTemplateType.TemplateType;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/*
        Created by: Jason Maderski
        Date: 6/4/2015

        Notes: Data model used for JSON string, also converts drawable to BASE64
*/

public class SailfishNotification {

    //Chrome params
    private String iconUrl = "";
    private String title = "";
    private String message = "";
    private int priority;
    private double eventTime;
    private TemplateType type = TemplateType.basic;
    private NotificationItems[] items;

    public SailfishNotification(StatusBarNotification sbn, Context context){

        this.type = getType(sbn);
        this.iconUrl = getIconBase64(sbn, context);

        String tmp = getTitleText(sbn);
        this.title = tmp == null ? "" : tmp;

        if(type == TemplateType.list){
            Log.i("TemplateType: ", "list");
            CharSequence inboxStyle[] = sbn.getNotification().extras.getCharSequenceArray("android.textLines");
            int arraySize = inboxStyle.length;

            items = new NotificationItems[arraySize];

            for(int i = 0; i < arraySize; i++){
                items[i] = new NotificationItems();
                items[i].title = "";
                items[i].message = inboxStyle[i].toString();
            }
        }

        tmp = getMessageText(sbn);
        this.message = tmp == null ? "" : tmp;

        this.eventTime = sbn.getPostTime();
        this.priority = sbn.getNotification().priority;

    }

    private TemplateType getType(StatusBarNotification sbn){
        if(sbn.getNotification().extras.getCharSequenceArray("android.textLines") == null)
            return TemplateType.basic;
        else
            return TemplateType.list;

    }

    private String drawableToBase64(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] bitmapdata = stream.toByteArray();
        //Convert bitArray data to Base64
        return "data:image/*;base64," + Base64.encodeToString(bitmapdata, Base64.DEFAULT);

    }

    private String getMessageText(StatusBarNotification sbn){

        CharSequence body = sbn.getNotification().extras.getCharSequence("android.text");
        if (body != null)
            return body.toString();
        else
            return null;


    }

    private String getTitleText(StatusBarNotification sbn){
        return sbn.getNotification().extras.getString("android.title").toString();
    }

    private String getIconBase64(StatusBarNotification sbn, Context context){
        Bitmap bitmap = null;
        IconWithOverlay overlay = new IconWithOverlay();

        if (sbn.getNotification().largeIcon != null) {
            //icon = new BitmapDrawable(context.getResources(), sbn.getNotification().largeIcon);
            bitmap = overlay.overlayedIcon(sbn, context);
        }else {
            try {
                Drawable icon = context.getPackageManager().getApplicationIcon(sbn.getPackageName());
                bitmap = ((BitmapDrawable)icon).getBitmap();
            }catch(PackageManager.NameNotFoundException ex){
                Log.e(this.getClass().getName(), "Name of the package wasn't found for some reason");
                bitmap = null;
            }
        }
        if (bitmap != null)
            return  drawableToBase64(bitmap);
        else
            return "";
    }

    public String getComposite(){
        return this.title + this.message;
    }

    @Override
    public boolean equals(Object o){

        if (!(o instanceof SailfishNotification))
            return false;

        SailfishNotification sn = (SailfishNotification) o;

        if (getComposite().compareTo(sn.getComposite()) == 0)
            return true;
        else
            return false;

    }

    public void clearImage(){
        this.iconUrl = "";
    }

}

class NotificationItems{
    public String title;
    public String message;
}
