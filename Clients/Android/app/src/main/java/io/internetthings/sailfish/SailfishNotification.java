package io.internetthings.sailfish;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/*
        Created by: Jason Maderski
        Date: 6/4/2015

        Notes: Data model used for JSON string, also converts drawable to BASE64
*/

public class SailfishNotification extends SailfishMessage{

    private String Base64Image;
    private String Subject;
    private String Body;
    private String PackageName;
    private long PostTime;


    public SailfishNotification(Drawable icon, String subjectInput, String bodyInput, String packageNameInput, long postTimeInput){

        Base64Image = drawableToBase64(icon);
        Subject = subjectInput;
        Body = bodyInput;
        PackageName = packageNameInput;
        PostTime = postTimeInput;



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
}
