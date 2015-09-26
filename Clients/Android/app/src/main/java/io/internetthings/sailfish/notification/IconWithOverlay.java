package io.internetthings.sailfish.notification;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by Jason on 9/25/15.
 */
public class IconWithOverlay {

    private String logTAG = this.getClass().getName();

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
        Drawable icon = null;

        icon = new BitmapDrawable(context.getResources(), sbn.getNotification().largeIcon);

        return icon;
    }

    private Drawable makeIconSmall(Drawable drawable, Context context){
        Bitmap b = ((BitmapDrawable)drawable).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 20, 20, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }

    public Drawable overlayedIcon(StatusBarNotification sbn, Context context) {
        Drawable smallIcon = makeIconSmall(getPackageIcon(sbn, context), context);
        Drawable largeIcon = getNotificationIcon(sbn, context);

        int left = 300;
        int top = 200;
        int right = 1;
        int bottom = 1;

        int getWidth = largeIcon.getIntrinsicWidth();
        int getHeight = largeIcon.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(getWidth, getHeight, Bitmap.Config.ARGB_8888);

        Drawable[] layers = new Drawable[2];
        layers[0] = largeIcon;
        layers[1] = smallIcon;

        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setLayerInset(1, left, top, right, bottom);
        layerDrawable.setBounds(0, 0, getWidth, getHeight);
        layerDrawable.draw(new Canvas(bitmap));

        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
        //bitmapDrawable.setBounds(0, 0, getWidth, getHeight);

        return bitmapDrawable;
    }

}
