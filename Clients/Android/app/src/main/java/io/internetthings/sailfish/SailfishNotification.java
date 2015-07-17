package io.internetthings.sailfish;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/*
        Created by: Jason Maderski
        Date: 6/4/2015

        Notes: Data model used for JSON string, also converts drawable to BASE64
*/

public class SailfishNotification extends SailfishMessage implements Comparable<SailfishNotification>{

    private String Base64Image;
    private String Subject;
    private String Body;
    private String PackageName;
    private long PostTime;
    private int Priority;

    public SailfishNotification(StatusBarNotification sbn, Context context, MessageActions action){

        super(sbn, action);

        this.Base64Image = getIconBase64(sbn, context);
        this.Subject = getSubjectText(sbn);
        this.Body = getBodyText(sbn);
        this.PackageName = sbn.getPackageName();
        this.PostTime = sbn.getPostTime();
        this.Priority = sbn.getNotification().priority;

    }

    public int compareTo(SailfishNotification sn){
        return getComposite().compareTo(sn.getComposite());
    }

    @Override
    public String toString(){
        return "NotificationObject [Subject=" + Subject + ", Body=" + Body
                + ", PackageName=" + PackageName + ", PostTime=" + PostTime + ", Base64Image=" + Base64Image + "]";
    }

    private String drawableToBase64(Drawable icon){
        Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] bitmapdata = stream.toByteArray();
        //Convert bitArray data to Base64
        return Base64.encodeToString(bitmapdata, Base64.DEFAULT);

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
            return drawableToBase64(icon);
        else
            return "";
    }

    public String getComposite(){
        return this.Subject + this.Body;
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

}
