/**
 * Superuser
 * Copyright (C) 2016 Pierre-Hugues Husson (phhusson)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.koushikdutta.superuser;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.koushikdutta.superuser.db.LogEntry;
import com.koushikdutta.superuser.db.SuDatabaseHelper;
import com.koushikdutta.superuser.db.SuperuserDatabaseHelper;
import com.koushikdutta.superuser.db.UidPolicy;
import com.koushikdutta.superuser.util.ATHUtil;
import com.koushikdutta.superuser.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.koushikdutta.superuser.FragmentMain.SHOULD_RELOAD;

public class MainActivity extends AppCompatActivity
        implements FragmentLog.LogCallback, FragmentMain.MainCallback {

//    public static final String PREF_THEME = "theme";
//    public static final String PREF_BLACK_THEME = "black_theme";
//    public static final String PREF_DARK_THEME = "dark_theme";
//    public static final String PREF_LIGHT_THEME = "light_theme";


    Toolbar toolbar;

    private PagerAdapter pagerAdapter;
    //FloatingActionButton fab;


    static List<ListItem> data;

    static HashMap<String, String> logCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEdit = pref.edit();

        loadData();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setSelectedTabIndicatorColor(pref.getInt("tab_indicator", ATHUtil.resolveColor(this, R.attr.tabIndicatorAccent)));

    }


    private void loadData() {
        data = new ArrayList<>();

        logCount = new HashMap<>();

        final ArrayList<UidPolicy> policies = SuDatabaseHelper.getPolicies(this);

        SQLiteDatabase db = new SuperuserDatabaseHelper(this).getReadableDatabase();

        try {
            //java.text.DateFormat df = DateFormat.getLongDateFormat(getActivity());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());

            Calendar calendar = GregorianCalendar.getInstance();
            Calendar logCalendar = GregorianCalendar.getInstance();

            for (UidPolicy up : policies) {
                Date date;
                int allowedCount = 0, deniedCount = 0;
                String s = null;

                ArrayList<LogEntry> logs = SuperuserDatabaseHelper.getLogs(db, up, -1);

                if (logs.size() > 0) {
                    date = logs.get(0).getDate();

                    for (LogEntry log : logs) {
                        if (log.action.equalsIgnoreCase(UidPolicy.ALLOW)) allowedCount++;
                        else if (log.action.equalsIgnoreCase(UidPolicy.DENY)) deniedCount++;
                    }

                    logCount.put(up.packageName, String.valueOf(allowedCount) + "+" + String.valueOf(deniedCount));

                    s = sdf.format(date);

                    logCalendar.setTime(date);

                    if (calendar.get(Calendar.YEAR) == logCalendar.get(Calendar.YEAR) &&
                            calendar.get(Calendar.DAY_OF_YEAR) == logCalendar.get(Calendar.DAY_OF_YEAR)) s = getString(R.string.today);
                }

                ListItem item = new ListItem(up, up.getPolicy(), up.getName(), s, Util.loadPackageIcon(this, up.packageName));

                if (!data.contains(item)) data.add(item);
            }

            Collections.sort(data, new Comparator<ListItem>() {
                @Override
                public int compare(ListItem listItem, ListItem t1) {
                    return listItem.getItem2().compareToIgnoreCase(t1.getItem2());
                }
            });

        } finally {
            db.close();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (SHOULD_RELOAD) {
            onLogCleared();
            SHOULD_RELOAD = false;
        }
    }


    @Override
    public void onLogCleared() {
        loadData();
        ((FragmentMain)pagerAdapter.getRegisteredFragment(1)).setData();
        ((FragmentMain)pagerAdapter.getRegisteredFragment(2)).setData();
    }


    @Override
    public void onListChanged(int which) {
        switch (which) {
            case FragmentMain.FRAGMENT_ALLOWED:
                ((FragmentMain)pagerAdapter.getRegisteredFragment(2)).setData();
                break;

            case FragmentMain.FRAGMENT_DENIED:
                ((FragmentMain)pagerAdapter.getRegisteredFragment(1)).setData();
                break;
        }
    }


    @Override
    public void onGridSpanChanged(int which, int val) {
        switch (which) {

            case FragmentMain.FRAGMENT_ALLOWED:
                ((FragmentMain) pagerAdapter.getRegisteredFragment(2)).setSpan(val);
                break;

            case FragmentMain.FRAGMENT_DENIED:
                ((FragmentMain) pagerAdapter.getRegisteredFragment(1)).setSpan(val);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        data = null;
        logCount = null;
    }


    public class PagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0: return new FragmentLog();

                case 1: return FragmentMain.newInstance(FragmentMain.FRAGMENT_ALLOWED);

                case 2: return FragmentMain.newInstance(FragmentMain.FRAGMENT_DENIED);
            }

            return null;
        }


        @Override
        public int getCount() {
            return 3;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.logs);
                case 1: return getString(R.string.allowed);
                case 2: return getString(R.string.denied);
            }
            return null;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}