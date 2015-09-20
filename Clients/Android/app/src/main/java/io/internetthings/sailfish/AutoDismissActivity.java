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
    private HashSet<String> PkgBlackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_dismiss);
        checkNullMutedPackages();
        checkboxCreator(this);
    }

    private void runBlackList(){
        PkgBlackList = new HashSet<>();
        PkgBlackList.add("io.internetthings.sailfish");
        PkgBlackList.add("jp.co.omronsoft.iwnnime.ml.kbd.white");
        PkgBlackList.add("com.google.android.gsf");
        PkgBlackList.add("com.google.android.gsf.login");
        PkgBlackList.add("jp.co.omronsoft.iwnnime.ml");
        PkgBlackList.add("android");
        PkgBlackList.add("com.google.android.tts");
        PkgBlackList.add("com.google.android.backuptransport");
        PkgBlackList.add("com.google.android.onetimeinitializer");
        PkgBlackList.add("com.google.android.partnersetup");
        PkgBlackList.add("com.google.android.feedback");
        PkgBlackList.add("com.google.android.webview");
        PkgBlackList.add("com.qualcomm.qcrilmsgtunnel");
        PkgBlackList.add("com.google.android.setupwizard ");
        PkgBlackList.add("com.lge.update");
        PkgBlackList.add("com.google.android.configupdater");
        PkgBlackList.add("com.qualcomm.shutdownlistner");
        PkgBlackList.add("com.google.android.androidforwork");
        PkgBlackList.add("com.qualcomm.timeservice");
        PkgBlackList.add("com.google.android.setupwizard");
        PkgBlackList.add("com.lge.SprintHiddenMenu");
        PkgBlackList.add("com.google.android.apps.inputmethod.hindi");

    }

    private List<String> filteredPkgs(){
        final PackageManager pm =  getPackageManager();

        List<String> newPkgList = new ArrayList<String>();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Iterator<ApplicationInfo> it = packages.listIterator();

        runBlackList();

        while(it.hasNext()){
            String pkg = it.next().packageName;
            if(PkgBlackList.contains(pkg))
                Log.i(sTAG, "Pkg on Blacklist: " + pkg);
            else if(pkg.contains("com.android") || pkg.contains("com.google.android.inputmethod"))
                Log.i(sTAG, "Pkg rejected: " + pkg);
            else
                newPkgList.add(pkg);
        }

        return newPkgList;

    }

    private void checkboxCreator(final Context context){
        Drawable icon = null;
        String appName = "No name found";

        AutoDismissPackages adp = SailfishNotificationService.autoDismissPackages;

        Iterator<String> it = filteredPkgs().listIterator();

        LinearLayout ll = (LinearLayout) findViewById(R.id.ADLL);
        ll.removeAllViews();

        while(it.hasNext()){
            final String packageName = it.next();
            final String pkg = packageName;

            boolean value = adp.isAutoDismissed(pkg);
            ApplicationInfo appInfo;

            try{
                appInfo = context.getPackageManager().getApplicationInfo(pkg, 0);
                icon = context.getPackageManager().getApplicationIcon(appInfo);
                appName = context.getPackageManager().getApplicationLabel(appInfo).toString();


            }catch (Exception e){
                Log.e(sTAG, e.getMessage());
            }

            if(appName.contains("com.")){
                Log.i(sTAG, "Not adding package: " + pkg);
            }else {
                Log.i(sTAG, pkg + " " + appName);
                final CheckedTextView chkBox = new CheckedTextView(this);

                icon = new ScaleDrawable(icon, 0, 200f, 200f).getDrawable();
                icon = scalableDrawable(icon);

                chkBox.setChecked(value);
                chkBox.setCheckMarkDrawable(R.drawable.custom_checkbox);
                chkBox.setText(" " + limitPkgNameSize(appName));
                chkBox.setGravity(0x10);
                chkBox.setCompoundDrawables(icon, null, null, null);
                chkBox.setTextSize(24f);
                chkBox.setTextColor(Color.WHITE);
                chkBox.setPadding(0, 0, 35, 20);

                chkBox.setOnClickListener(new CheckedTextView.OnClickListener() {
                    public void onClick(View v) {
                        CheckedTextView cur = (CheckedTextView) v;
                        cur.toggle();
                        if (cur.isChecked()) {
                            Log.i("Auto-Dismissed:", "CHECKED");
                            SailfishNotificationService.autoDismissPackages.autoDismissPackage(pkg, context);
                        } else {
                            Log.i("Auto-Dismissed:", "UNCHECKED");
                            SailfishNotificationService.autoDismissPackages.dontAutoDismissPackage(pkg, context);

                            Log.i("Auto-D Package: ", pkg + " "
                                    + SailfishNotificationService.autoDismissPackages.isAutoDismissed(pkg));
                        }
                    }
                });
                ll.addView(chkBox);
            }
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
