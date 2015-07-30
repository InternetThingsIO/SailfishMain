package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MutedPackagesActivity extends Activity {

    private HashMap<String, Boolean> mutedPackages;
    private String sTAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muted_packages);
        getMutedPrefs();
        createCheckboxes(this);
    }

    private void createCheckboxes(Context context){
        Drawable icon = null;
        String pkgName = "No name found!";

        Iterator<String> it = mutedPackages.keySet().iterator();
        Log.i("itsize: ", Integer.toString(mutedPackages.size()));
        while(it.hasNext()){
            String pkg = it.next();
            boolean value = mutedPackages.get(pkg);
            ApplicationInfo appInfo;
            try{
                appInfo = context.getPackageManager().getApplicationInfo(pkg, 0);
                icon = context.getPackageManager().getApplicationIcon(appInfo);
                pkgName = context.getPackageManager().getApplicationLabel(appInfo).toString();
            }catch (Exception e){
                Log.e(sTAG, e.getMessage());
            }

            LinearLayout ll = (LinearLayout) findViewById(R.id.checkBoxLL);

            CheckedTextView chkBox = new CheckedTextView(this);
            chkBox.setChecked(value);
            chkBox.setCheckMarkDrawable(R.drawable.custom_checkbox);
            chkBox.setText(pkgName);
            chkBox.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            chkBox.setTextSize(24f);
            chkBox.setTextColor(Color.WHITE);
            chkBox.isClickable();
            chkBox.setOnClickListener(new CheckedTextView.OnClickListener() {
                public void onClick(View v) {
                    ((CheckedTextView) v).toggle();
                }
            });
            ll.addView(chkBox);
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
        mutedPackages.put("com.google.android.gm", false);
        mutedPackages.put("com.google.android.talk", true);
        /*mutedPackages.put("com.google.android.gm", false);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);
        mutedPackages.put("com.google.android.gm", true);*/


    }




}
