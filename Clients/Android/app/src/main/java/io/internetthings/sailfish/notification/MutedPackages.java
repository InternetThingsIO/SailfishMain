package io.internetthings.sailfish.notification;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Iterator;

import io.internetthings.sailfish.SailfishPreferences;

/**
 * Created by gsapp on 8/2/2015.
 */
public class MutedPackages {

    public static boolean reloadPackages = true;

    private HashMap<String, Boolean> mutedPackages;
    private Iterator<String> iterator;

    public MutedPackages(){
        mutedPackages = new HashMap<String, Boolean>();
        iterator = mutedPackages.keySet().iterator();
    }

    public MutedPackages(String json){
        loadHashMap(json);
    }

    private void loadHashMap(String json){
        Gson g = new Gson();
        mutedPackages = g.fromJson(json, HashMap.class);
    }

    public static void ReloadPackages(){
        reloadPackages = true;
    }

    public String next(){
        return iterator.next();
    }

    public boolean hasNext(){
        return iterator.hasNext();
    }

    public void mutePackage(String pkg){
        mutedPackages.put(pkg, true);
    }

    public void unMutePackage(String pkg){
        mutedPackages.put(pkg, false);
    }

    public boolean isMuted(Context context, StatusBarNotification sbn){
        if (reloadPackages){
            loadMutedPackages(context);
        }

        return mutedPackages.containsKey(sbn.getPackageName());

    }

    private void loadMutedPackages(Context context){
       loadHashMap(SailfishPreferences.getMutedPackages(context));
    }


    @Override
    public String toString(){
        Gson g = new Gson();
        return g.toJson(mutedPackages);
    }
}
