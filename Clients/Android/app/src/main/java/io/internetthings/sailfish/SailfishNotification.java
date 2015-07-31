package io.internetthings.sailfish;

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

/*
        Created by: Jason Maderski
        Date: 6/4/2015

        Notes: Data model used for JSON string, also converts drawable to BASE64
*/

public class SailfishNotification {

    //Chrome params
    private String iconUrl;
    private String title;
    private String message;
    private int priority;
    private double eventTime;
    private TemplateType type;

    public SailfishNotification(StatusBarNotification sbn, Context context){

        this.iconUrl = getIconBase64(sbn, context);
        this.title = getSubjectText(sbn);
        this.message = getBodyText(sbn);
        this.eventTime = sbn.getPostTime();
        this.priority = sbn.getNotification().priority;
        this.type = TemplateType.basic;

    }

    private String drawableToBase64(Drawable icon){
        Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] bitmapdata = stream.toByteArray();
        //Convert bitArray data to Base64
        return "data:image/*;base64," + Base64.encodeToString(bitmapdata, Base64.DEFAULT);

    }

    private String getBodyText(StatusBarNotification sbn){

        CharSequence body = sbn.getNotification().extras.getCharSequence("android.text");
        if (body != null)
            return body.toString();
        else
            return null;


    }

    private String getSubjectText(StatusBarNotification sbn){
        return sbn.getNotification().extras.getString("android.title");
    }

    private String getIconBase64(StatusBarNotification sbn, Context context){
        Drawable icon = null;
        if (sbn.getNotification().largeIcon != null) {
            icon = new BitmapDrawable(context.getResources(), sbn.getNotification().largeIcon);
        }else {
            try {
                icon = context.getPackageManager().getApplicationIcon(sbn.getPackageName());
            }catch(PackageManager.NameNotFoundException ex){
                Log.e(this.getClass().getName(), "Name of the package wasn't found for some reason");
                icon = null;
            }
        }
        if (icon != null)
            return  drawableToBase64(icon);
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
