package io.internetthings.sailfish.notification;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by Jason on 9/25/15.
 */
public class IconWithOverlay {

    private String logTAG = this.getClass().getName();
    private static final int IMAGE_SIZE_PX = 80;
    private static final int OVERLAY_SIZE_PX = 30;

    private Drawable getPackageIcon(StatusBarNotification sbn, Context context){
        Drawable icon = null;
        ApplicationInfo appInfo;
        String pkg = sbn.getPackageName();

        try{
            appInfo = context.getPackageManager().getApplicationInfo(pkg, 0);
            icon = context.getPackageManager().getApplicationIcon(appInfo);

        }catch (Exception e){
            Log.e(logTAG, e.getMessage());
        }

        return icon;
    }

    private Drawable getNotificationIcon(StatusBarNotification sbn, Context context){

        Drawable icon = new BitmapDrawable(context.getResources(), sbn.getNotification().largeIcon);

        return icon;
    }

    private Bitmap getBitmapBackground(){
        return Bitmap.createBitmap(IMAGE_SIZE_PX+8, IMAGE_SIZE_PX+8, Bitmap.Config.ARGB_8888);
    }

    //Scales a drawable maintaining it's aspect ratio
    private Bitmap scaleDrawable(Drawable drawable, Context context, float maxBound){
        float scaledHeight, scaledWidth;

        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        if(bitmap.getHeight() > bitmap.getWidth()){
            scaledHeight = maxBound;
            scaledWidth = (maxBound / new Float(bitmap.getHeight())) * bitmap.getWidth();

        }else{
            scaledHeight = (maxBound / new Float(bitmap.getWidth())) * bitmap.getHeight();
            scaledWidth = maxBound;
        }

        return Bitmap.createScaledBitmap(bitmap, (int) scaledWidth, (int) scaledHeight, false);
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2, Bitmap bmp3) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        int dx, dy;
        dx = (bmp1.getWidth()/2) - (bmp2.getWidth()/2);
        dy = (bmp1.getHeight()/2) - (bmp2.getHeight()/2);
        canvas.drawBitmap(bmp2, dx, dy, null);
        dx = canvas.getWidth() - bmp3.getWidth() - 1;
        dy = canvas.getHeight() - bmp3.getHeight() - 1;
        canvas.drawBitmap(bmp3,dx,dy, null);
        return bmOverlay;
    }

    public Bitmap overlayedIcon(StatusBarNotification sbn, Context context) {
        Bitmap notifBitmap =  scaleDrawable(getNotificationIcon(sbn, context), context, IMAGE_SIZE_PX);
        Bitmap appBitmap =  scaleDrawable(getPackageIcon(sbn, context), context, OVERLAY_SIZE_PX);

        return overlay(getBitmapBackground(), notifBitmap, appBitmap);
    }

}
