package com.example.akashseth.pedal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;


abstract class BaseActivity extends AppCompatActivity {
    ListView mDrawerList;
    RelativeLayout mDrawerRelative;
    DrawerItemCustomAdapter mAdapter;
    ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;
    Toolbar myToolbar;

    SessionManagement sessionManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        sessionManagement = new SessionManagement(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

       /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("Error" + Thread.currentThread().getStackTrace()[2], paramThrowable.getLocalizedMessage());
            }
        });*/

    }

    //Add drawer Items
    protected void addDrawerItems() {

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[4];
        drawerItem[0] = new ObjectDrawerItem(R.drawable.activity, getString(R.string.Activity));
        drawerItem[1] = new ObjectDrawerItem(R.drawable.history, getString(R.string.History));
        if (sessionManagement.isLoggedIn()) {
            drawerItem[2] = new ObjectDrawerItem(R.drawable.logout, getString(R.string.Logout));
        } else {
            drawerItem[2] = new ObjectDrawerItem(R.drawable.login, getString(R.string.Login));
        }
        drawerItem[3] = new ObjectDrawerItem(R.drawable.faq, getString(R.string.Faqs));


        mAdapter = new DrawerItemCustomAdapter(this, R.layout.drawer_list_item, drawerItem);

        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0: {
                        mDrawerLayout.closeDrawers();
                        Intent startActivityIntent = new Intent(getApplicationContext(), StartActivity.class);
                        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startActivityIntent);
                        return;
                    }
                    case 1: {
                        Intent historyIntent = new Intent(getApplicationContext(), History.class);
                        historyIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(historyIntent);
                        return;
                    }
                    case 2: {

                        if (sessionManagement.isLoggedIn()) {
                            mDrawerLayout.closeDrawers();
                            alertBeforeLogout();
                        } else {
                            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(loginIntent);
                        }
                        return;
                    }
                    case 3: {

                        Intent faqIntent = new Intent(getApplicationContext(), Help.class);
                        faqIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(faqIntent);
                        return;
                    }


                }
            }
        });
    }

    //Setup drawer
    protected void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, myToolbar, R.string.open, R.string.close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //Modify menu items before they are appeared
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem logout = menu.findItem(R.id.logout);
        MenuItem settings = menu.findItem(R.id.action_settings);
        if (!sessionManagement.isLoggedIn()) {
            logout.setVisible(false);
            settings.setVisible(false);

        } else {
            logout.setVisible(true);
            settings.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                alertBeforeLogout();
                return true;
            case R.id.action_settings:
                // Settings option clicked.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Function for alerting before logout
    public void alertBeforeLogout() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(BaseActivity.this, R.style.AppCompatAlertDialogStyle);
        dialog.setTitle(getString(R.string.Logout));
        dialog.setMessage(R.string.LogoutMessage);
        dialog.setPositiveButton(getString(R.string.Logout), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                sessionManagement.logoutUser();
                // After logout redirect user to Login Activity
                Intent i = new Intent(getApplicationContext(), StartActivity.class);
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                // Starting Login Activity
                startActivity(i);
            }
        });
        dialog.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }

    //Function for get zipcode of any country
    public String GetCountryZipCode() {
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    //Function for configuring drawer
    public void commonDrawerConfigCall() {
        myToolbar = (Toolbar) findViewById(R.id.my_tools);
        setSupportActionBar(myToolbar);

        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerRelative = (RelativeLayout) findViewById(R.id.left_drawer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    public void editProfile(View view) {
        Intent editProfileIntent = new Intent(getApplicationContext(), EditProfile.class);
        startActivity(editProfileIntent);
    }


}


