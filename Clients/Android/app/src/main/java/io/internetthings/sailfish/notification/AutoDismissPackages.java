package io.internetthings.sailfish.notification;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import io.internetthings.sailfish.SailfishPreferences;

/**
 * Created by Jason on 9/18/15.
 */
public class AutoDismissPackages {

    private final String logTag = this.getClass().getName();

    private ConcurrentHashMap<String, Boolean> autoDismissedPackages = new ConcurrentHashMap<>();

    public AutoDismissPackages(Context context){
        autoDismissedPackages = new ConcurrentHashMap<>();

        String json = SailfishPreferences.getAutoDismissedPackages(context);
        loadHashMap(json);
    }

    private synchronized void loadHashMap(String json){
        if (json != null && json.length() > 0) {
            Gson g = new Gson();
            autoDismissedPackages = g.fromJson(json, ConcurrentHashMap.class);
        }
    }

    public ConcurrentHashMap<String, Boolean> getPackages(){
        return autoDismissedPackages;
    }

    private synchronized void saveHashMap(Context context){
        Gson g = new Gson();
        String json = g.toJson(autoDismissedPackages);

        SailfishPreferences.setAutoDismissedPackages(context, json);
        SailfishPreferences.commit(context);
    }

    public synchronized void setPackage(String pkg, Context context, boolean autoDismiss){
        autoDismissedPackages.put(pkg, autoDismiss);
        saveHashMap(context);
    }

    public synchronized void addList(String pkg, Context context){
        if (!autoDismissedPackages.containsKey(pkg) && !context.getPackageName().equals(pkg)) {
            //add to list of packages the user can select from
            setPackage(pkg, context, false);
        }
    }

    public synchronized boolean isAutoDismissed(String pkg, Context context){
        if (autoDismissedPackages.containsKey(pkg)) {
            return autoDismissedPackages.get(pkg);
        }

        return false;
    }

}
