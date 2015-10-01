package io.internetthings.sailfish.notification;

import android.util.Log;

/**
 * Created by Jason on 10/1/15.
 */
public class Hyperlink {

    private String logTAG = this.getClass().getName();

    public void testHyperlink(){
        String test = generateHyperLink("Please go to http://www.stackoverflow.com it can be handy to use");
        Log.i(logTAG, test);
    }

    public String generateHyperLink(String message){

        String hyperlink = "";

        if(message.contains("http://"))
            hyperlink = message.replaceAll("http://.+?(com|net|org)/{0,1}", "<a href=\"$0\">$0</a>");
        else if(message.contains("https://"))
            hyperlink = message.replaceAll("https://.+?(com|net|org)/{0,1}", "<a href=\"$0\">$0</a>");
        else if(message.contains("www."))
            hyperlink = message.replaceAll("www.+?(com|net|org)/{0,1}", "<a href=\"$0\">$0</a>");

        return hyperlink;
    }

}
