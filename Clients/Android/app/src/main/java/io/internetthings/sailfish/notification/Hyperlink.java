package io.internetthings.sailfish.notification;

import android.util.Log;

/**
 * Created by Jason on 10/1/15.
 */
public class Hyperlink {

    private String logTAG = this.getClass().getName();

    public void testHyperlink(){
        String test = generateHyperLink("www.canyouseeme.com Please go to www.stackoverflow.com it can be handy to use " +
                "or check out this article http://www.theverge.com/2015/10/1/9433783/google-self-" +
                "driving-car-prototype-obstacle-course it's cool!  For more secure site https://" +
                "www.securetest.com and don't forget to try www.cool.com");
        Log.i(logTAG, test);
    }

    public String generateHyperLink(String message){

        String hyperlink = "";

        hyperlink = message.replaceAll("(http://|https://|www).+?([^\\s]+)/{0,1}", "<a href=\"$0\">$0</a>");


        return hyperlink;
    }

}
