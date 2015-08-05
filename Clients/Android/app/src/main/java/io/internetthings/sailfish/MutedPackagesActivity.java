package io.internetthings.sailfish;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
        checkNullMutedPackages();
        checkboxCreator(this);
    }

    private void checkboxCreator(final Context context){
        Drawable icon = null;
        String pkgName = "No name found";

        MutedPackages mp = SailfishNotificationService.mutedPackages;
        testing123(context, mp);
        Iterator<String> it = mp.getPkgIterator();
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
            LinearLayout ll = (LinearLayout) findViewById(R.id.checkBoxLL);
            final CheckedTextView chkBox = new CheckedTextView(this);

            chkBox.setChecked(value);
            chkBox.setCheckMarkDrawable(R.drawable.custom_checkbox);
            chkBox.setText(" " + pkgName);
            chkBox.setGravity(0x10);
            chkBox.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            chkBox.setTextSize(24f);
            chkBox.setTextColor(Color.WHITE);
            chkBox.setPadding(0, 0, 35, 0);
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

    private void checkNullMutedPackages() {
        if (SailfishNotificationService.mutedPackages == null)
            SailfishNotificationService.mutedPackages = new MutedPackages(this);
    }

    private void testing123(Context context, MutedPackages mp){

        mp.mutePackage("com.google.android.gm", context);
        mp.mutePackage("com.google.android.talk", context);
    }

}
