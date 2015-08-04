package io.internetthings.sailfish.notification;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.service.notification.StatusBarNotification;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.internetthings.sailfish.SailfishPreferences;

/**
 * Created by gsapp on 8/2/2015.
 */
public class MutedPackages {

    private HashMap<String, Boolean> mutedPackages;

    public MutedPackages(Context context){
        mutedPackages = new HashMap<>();

        String json = SailfishPreferences.getMutedPackages(context);
        loadHashMap(json);

    }

    private void loadHashMap(String json){
        Gson g = new Gson();
        mutedPackages = g.fromJson(json, HashMap.class);
    }

    private void saveHashMap(Context context){
        Gson g = new Gson();
        String json = g.toJson(mutedPackages);
        SailfishPreferences.setMutedPackages(context, json);
    }

    public Iterator<String> getPkgIterator(){
        return mutedPackages.keySet().iterator();
    }

    public void mutePackage(String pkg, Context context){
        mutedPackages.put(pkg, true);
        cleanUpPackages(context, true);
    }

    public void unMutePackage(String pkg, Context context){
        mutedPackages.put(pkg, false);
        saveHashMap(context);
    }

    public boolean isMuted(String pkg){

        if (mutedPackages.containsKey(pkg)){
            return mutedPackages.get(pkg);
        }

        return false;

    }

    public void cleanUpPackages(Context context, boolean savePreferences){
        HashMap<String, Boolean> overlap = new HashMap<>();
        List<PackageInfo> pkgAppsList = context.getPackageManager().getInstalledPackages(0);

        for (PackageInfo pkg : pkgAppsList){
            String pkgName = pkg.packageName;
            if (mutedPackages.containsKey(pkgName)){
                overlap.put(pkgName, mutedPackages.get(pkgName));
            }
        }

        mutedPackages = overlap;

        if (savePreferences)
            saveHashMap(context);

    }

    @Override
    public String toString(){
        Gson g = new Gson();
        return g.toJson(mutedPackages);
    }
}
