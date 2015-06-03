package io.internetthings.sailfish;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
/*
    Created by: Jason Maderski
    Date: 6/2/2015

    Notes: NService(Notification Service) runs in the background and currently displays in the
           logcat window when an notification was posted or removed
 */


public class NoticeNotificationService extends NotificationListenerService{

    private final String logTAG = this.getClass().getName();

    public NoticeNotificationService(){

    }

    //Displays notification posted with the ID in the logcat window
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Log.w(logTAG, "Notification POSTED*************** " + "User: "
                + " Package Name: " + sbn.getPackageName() + " ID: " + sbn.getId());
    }

    //Displays notification that has been removed in the logcat window
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        Log.w(logTAG, "Notification REMOVED*************** " + "User: "
                + " Package Name: " + sbn.getPackageName() + " ID: " + sbn.getId());
    }
}
