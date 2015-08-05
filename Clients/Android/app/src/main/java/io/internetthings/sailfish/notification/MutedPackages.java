package io.internetthings.sailfish.notification;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.internetthings.sailfish.SailfishPreferences;

/**
 * Created by gsapp on 8/2/2015.
 */
public class MutedPackages {

    private final String logTag = this.getClass().getName();

    private ConcurrentHashMap<String, Boolean> mutedPackages = new ConcurrentHashMap<>();
    //private HashMap<String, Boolean> mutedPackages;

    public MutedPackages(Context context){
            mutedPackages = new ConcurrentHashMap<>();

            String json = SailfishPreferences.getMutedPackages(context);
            loadHashMap(json);
    }

    private synchronized void loadHashMap(String json){
        if (json != null && json.length() > 0) {
            Gson g = new Gson();
            mutedPackages = g.fromJson(json, ConcurrentHashMap.class);
        }
    }

    private synchronized void saveHashMap(Context context){
        Gson g = new Gson();
        String json = g.toJson(mutedPackages);
        Log.w(logTag, "JSON:" + json);
        SailfishPreferences.setMutedPackages(context, json);
        SailfishPreferences.commit(context);
    }

    public synchronized Iterator<String> getPkgIterator(){
        return mutedPackages.keySet().iterator();
    }

    public synchronized void mutePackage(String pkg, Context context){
        mutedPackages.put(pkg, true);
        cleanUpPackages(context, true);
    }

    public synchronized void unMutePackage(String pkg, Context context){
        mutedPackages.put(pkg, false);
        saveHashMap(context);
    }

    public synchronized boolean isMuted(String pkg){
        if (mutedPackages.containsKey(pkg)) {
            return mutedPackages.get(pkg);
        }

        return false;
    }

    public synchronized void cleanUpPackages(Context context, boolean savePreferences){
        Log.w(logTag, "num mutedPackages: " + mutedPackages.size());

        ConcurrentHashMap<String, Boolean> overlap = new ConcurrentHashMap<>();
        List<PackageInfo> pkgAppsList = context.getPackageManager().getInstalledPackages(0);

        for (PackageInfo pkg : pkgAppsList) {
            String pkgName = pkg.packageName;
            if (mutedPackages.containsKey(pkgName)) {
                overlap.put(pkgName, mutedPackages.get(pkgName));
            }
        }

        mutedPackages = overlap;

        Log.w(logTag, "num mutedPackages2: " + mutedPackages.size());

        if (savePreferences)
            saveHashMap(context);
    }

    @Override
    public String toString(){
        Gson g = new Gson();
        return g.toJson(mutedPackages);
    }
}
