package io.internetthings.sailfish;

import android.graphics.Bitmap;

/**
 * Created by Dev on 6/3/2015.
 */
public class SailfishNotification {

    private String nSubject;
    private String nBody;
    private String nPackageName;
    private long nPostTime;


    public SailfishNotification(String subjectInput, String bodyInput, String packageNameInput, long postTimeInput){

        nSubject = subjectInput;
        nBody = bodyInput;
        nPackageName = packageNameInput;
        nPostTime = postTimeInput;

    }

    @Override
    public String toString(){
        return "NotificationObject [Subject=" + nSubject + ", Body=" + nBody
                + ", PackageName=" + nPackageName + ", PostTime=" + nPostTime + "]";
    }
}
