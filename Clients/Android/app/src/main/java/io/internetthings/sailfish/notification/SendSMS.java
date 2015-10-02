package io.internetthings.sailfish.notification;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Jason on 9/29/15.
 */
public class SendSMS {

    private String logTAG = this.getClass().getName();

    public void testSMSSending(Context context){
        String phoneNumber = "8505299238";
        String message = "SMS test message, sent from Notice that works only with phones that have an active SIM card";

        try {
            sendMessage(phoneNumber, message);
            Toast.makeText(context, "Message sent to: " + phoneNumber, Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Log.i(logTAG, e.getMessage());
        }

    }

    public void sendMessage(String phoneNumber, String message){

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);

    }


}
