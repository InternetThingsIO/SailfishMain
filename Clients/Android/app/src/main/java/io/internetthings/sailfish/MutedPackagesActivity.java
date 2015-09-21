package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import java.util.HashMap;
import java.util.Iterator;

import io.internetthings.sailfish.notification.MutedPackages;
import io.internetthings.sailfish.notification.SailfishNotificationService;

public class MutedPackagesActivity extends Activity {

    private String sTAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muted_packages);
    }

    @Override
    protected void onResume(){
        super.onResume();
        SailfishNotificationService.mutedPackages.cleanUpPackages(this, true);
        checkNullMutedPackages();
        checkboxCreator(this);
    }

    private void checkboxCreator(final Context context){
        Drawable icon = null;
        String pkgName = "No name found";

        MutedPackages mp = SailfishNotificationService.mutedPackages;
        Iterator<String> it = mp.getPkgIterator();

        LinearLayout ll = (LinearLayout) findViewById(R.id.checkBoxLL);
        ll.removeAllViews();
        
        while(it.hasNext()){
            final String pkg = it.next();
            boolean value = mp.isMuted(pkg);
            ApplicationInfo appInfo;

            try{
                appInfo = context.getPackageManager().getApplicationInfo(pkg, 0);
                icon = context.getPackageManager().getApplicationIcon(appInfo);
                pkgName = context.getPackageManager().getApplicationLabel(appInfo).toString();
            }catch (Exception e){
                Log.e(sTAG, e.getMessage());
            }

            final CheckedTextView chkBox = new CheckedTextView(this);

            icon = scalableDrawable(icon);

            chkBox.setChecked(value);
            chkBox.setCheckMarkDrawable(R.drawable.custom_checkbox);
            chkBox.setText(" " + limitPkgNameSize(pkgName));
            chkBox.setGravity(0x10);
            chkBox.setCompoundDrawables(icon, null, null, null);
            chkBox.setTextSize(24f);
            chkBox.setTextColor(Color.WHITE);
            chkBox.setPadding(0, 0, 35, 20);
            chkBox.setOnClickListener(new CheckedTextView.OnClickListener() {
                public void onClick(View v) {
                    CheckedTextView cur = (CheckedTextView)v;
                    cur.toggle();
                    if(cur.isChecked())
                        SailfishNotificationService.mutedPackages.mutePackage(pkg, context);
                    else
                        SailfishNotificationService.mutedPackages.unMutePackage(pkg, context);

                    Log.i("Muted Package: ", pkg + " "
                            + SailfishNotificationService.mutedPackages.isMuted(pkg));
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
        if (SailfishNotificationService.mutedPackages == null)
            SailfishNotificationService.mutedPackages = new MutedPackages(this);
    }

}
