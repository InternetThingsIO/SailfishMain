package io.internetthings.sailfish;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * Created by Dev on 6/14/2015.
 */
public class Tabs extends Fragment implements TabHost.OnTabChangeListener {

    private static final String TAG = "FragmentTabs";
    public static final String TAB_IGNORE_APPS = "Ignore Apps";
    public static final String TAB_PURCHASE_FEATURES = "Purchase Features";
    public static final String TAB_SWITCH_EMAIL = "Switch Email";
    public static final String TAB_HELP = "Help";

    private View mRoot;
    private TabHost mTabHost;
    private int mCurrentTab;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        mRoot = inflater.inflate(R.layout.fragment_activity, null);
        mTabHost = (TabHost) mRoot.findViewById(android.R.id.tabhost);
        setupTabs();
        return mRoot;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        mTabHost.setOnTabChangedListener(this);
        mTabHost.setCurrentTab(mCurrentTab);
        // manually start loading stuff in the first tab
        updateTab(TAB_IGNORE_APPS, R.id.tab_1);
    }

    private void setupTabs(){
        mTabHost.setup(); //must be called before adding the tabs!
        mTabHost.addTab(newTab(TAB_IGNORE_APPS, R.string.tab_ignore_apps, R.id.tab_1));
                mTabHost.addTab(newTab(TAB_SWITCH_EMAIL, R.string.tab_switch_email, R.id.tab_2));
        mTabHost.addTab(newTab(TAB_HELP, R.string.tab_help, R.id.tab_3));
    }

    private TabHost.TabSpec newTab(String tag, int labelId, int tabContentId){
        Log.d(TAG, "buildTab(): tag=" + tag);

        View indicator = LayoutInflater.from(getActivity()).inflate(
                R.layout.tab,
                (ViewGroup) mRoot.findViewById(android.R.id.tabs), false);
        ((TextView) indicator.findViewById(R.id.text)).setText(labelId);

        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabContentId);
        return tabSpec;
    }

    @Override
    public void onTabChanged(String tabId) {
        Log.d(TAG, "onTabChanged(): tabId=" + tabId);
        if (TAB_IGNORE_APPS.equals(tabId)) {
            updateTab(tabId, R.id.tab_1);
            mCurrentTab = 0;
            return;
        }
        if (TAB_SWITCH_EMAIL.equals(tabId)) {
            updateTab(tabId, R.id.tab_2);
            mCurrentTab = 1;
            return;
        }
        if (TAB_HELP.equals(tabId)) {
            updateTab(tabId, R.id.tab_3);
            mCurrentTab = 2;
            return;
        }
    }

    private void updateTab(String tabId, int placeholder) {
       /* FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag(tabId) == null) {
            fm.beginTransaction()
                    .replace(placeholder, new MyListFragment(tabId), tabId)
                    .commit();
        }*/  //commented out cause MyListFragment is what the example used...not going to create this class
    }
}
