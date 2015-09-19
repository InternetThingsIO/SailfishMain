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

    private synchronized void saveHashMap(Context context){
        Gson g = new Gson();
        String json = g.toJson(autoDismissedPackages);
        Log.w(logTag, "JSON:" + json);
        SailfishPreferences.setAutoDismissedPackages(context, json);
        SailfishPreferences.commit(context);
    }


    public synchronized Iterator<String> getPkgIterator(){
        return autoDismissedPackages.keySet().iterator();
    }

    public synchronized void autoDismissPackage(String pkg, Context context){
        autoDismissedPackages.put(pkg, true);
        saveHashMap(context);
    }

    public synchronized void dontAutoDismissPackage(String pkg, Context context){
        autoDismissedPackages.put(pkg, false);
        saveHashMap(context);
    }

    public synchronized boolean isAutoDismissed(String pkg){
        if (autoDismissedPackages.containsKey(pkg)) {
            return autoDismissedPackages.get(pkg);
        }

        return false;
    }

}
