package io.internetthings.sailfish;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MutedPackagesActivity extends Activity {

    private HashMap<String, Boolean> mutedPackages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muted_packages);
        getMutedPrefs();
        createCheckboxes();
    }

    private void createCheckboxes(){
        Iterator<String> it = mutedPackages.keySet().iterator();
        Log.i("itsize: ", Integer.toString(mutedPackages.size()));
        while(it.hasNext()){
            String pkg = it.next();
            boolean value = mutedPackages.get(pkg);

            LinearLayout ll = (LinearLayout) findViewById(R.id.checkBoxLL);

            CheckBox cb = new CheckBox(this);
            cb.setChecked(value);
            cb.setText(pkg);
            ll.addView(cb);
        }
    }

    public void mutePackage(String pkg){
       mutedPackages.put(pkg, true);
    }

    public void unMutePackage(String pkg){        
        mutedPackages.put(pkg, false);
    }

    public void setMutedPackages(){
        //SailfishPreferences.setMutedPackages(this, mutedPackages);
        //SailfishPreferences.commit(this);
    }

    public void getMutedPrefs(){
        //mutedPackages  //(HashSet<String>)SailfishPreferences.getMutedPackages(this);
        mutedPackages = new HashMap<>();
        mutedPackages.put("com.test1", true);
        mutedPackages.put("com.test2", true);
        mutedPackages.put("com.test3", true);
        mutedPackages.put("com.test4", true);
        mutedPackages.put("com.test5", true);
        mutedPackages.put("com.test6", true);
        mutedPackages.put("com.test7", true);
        mutedPackages.put("com.test8", true);
        mutedPackages.put("com.test9", true);
        mutedPackages.put("com.test10", true);
        mutedPackages.put("com.test11", true);
        mutedPackages.put("com.test12", true);
        mutedPackages.put("com.test13", true);
        mutedPackages.put("com.test14", true);
        mutedPackages.put("com.test15", true);
        mutedPackages.put("com.test16", true);
        mutedPackages.put("com.test17", true);
        mutedPackages.put("com.test18", true);


    }




}
