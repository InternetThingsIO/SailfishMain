package io.internetthings.sailfish;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.internetthings.sailfish.notification.AutoDismissPackages;
import io.internetthings.sailfish.notification.MutedPackages;
import io.internetthings.sailfish.notification.SailfishNotificationService;

public class AutoDismissActivity extends Activity {

    private String sTAG = this.getClass().getName();
    private LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_dismiss);
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkNullMutedPackages();
        createAutoDismissList();
    }

    private void setupLinearLayout(){
        ll = (LinearLayout) findViewById(R.id.ADLL);
        ll.removeAllViews();
    }

    private void createAutoDismissList(){
        List<String> installedPackages = new ArrayList(SailfishNotificationService.autoDismissPackages.getPackages().keySet());

        setupLinearLayout();
        //runBlackList();

        Drawable icon = null;
        String appName = "No name found";
        PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo appInfo;

        for(String packageName : installedPackages){

            try{
                appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                icon = pm.getApplicationIcon(packageName);
                appName = pm.getApplicationLabel(appInfo).toString();
                checkboxCreator(packageName, icon, appName);
            }catch (Exception e){
                Log.e(sTAG, e.getMessage());
            }

        }

    }

    private void checkboxCreator(final String packageName, Drawable icon, String appName){

        if(appName.contains("com.")){
            Log.i(sTAG, "Not adding package: " + packageName);
        }else {
            CheckedTextView chkBox = new CheckedTextView(this);

            icon = scalableDrawable(icon);

            chkBox.setChecked(SailfishNotificationService.autoDismissPackages.isAutoDismissed(packageName, this));
            chkBox.setCheckMarkDrawable(R.drawable.custom_checkbox);
            chkBox.setText(" " + limitPkgNameSize(appName));
            chkBox.setGravity(0x10);
            chkBox.setCompoundDrawables(icon, null, null, null);
            chkBox.setTextSize(24f);
            chkBox.setTextColor(Color.WHITE);
            chkBox.setPadding(0, 0, 35, 20);

            final Context context = this;

            chkBox.setOnClickListener(new CheckedTextView.OnClickListener() {
                public void onClick(View v) {
                    CheckedTextView cur = (CheckedTextView) v;
                    cur.toggle();
                    if (cur.isChecked()) {
                        Log.d("Auto-Dismissed:", "CHECKED");
                        SailfishNotificationService.autoDismissPackages.setPackage(packageName, context, true);
                    } else {
                        Log.d("Auto-Dismissed:", "UNCHECKED");
                        SailfishNotificationService.autoDismissPackages.setPackage(packageName, context, false);
                    }
                }
            });
            ll.addView(chkBox);
        }

    }

    private String limitPkgNameSize(String pkgName){

        if(pkgName.length() > 15) {
            pkgName = pkgName.substring(0, 15);
            pkgName = pkgName + "...";
        }

        return pkgName;
    }

    private Drawable scalableDrawable(Drawable drawable){
        final float scale = this.getResources().getDisplayMetrics().density;
        int scaledWidth = (int)(scale * 35f);
        int scaledHeight = (int)(scale * 35f);

        drawable.setBounds(0, 0, scaledWidth, scaledHeight);
        return drawable;
    }

    private void checkNullMutedPackages() {
        if (SailfishNotificationService.autoDismissPackages== null)
            SailfishNotificationService.autoDismissPackages = new AutoDismissPackages(this);
    }

}
