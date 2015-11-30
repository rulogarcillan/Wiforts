package com.r.raul.tools;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager = getSupportFragmentManager();

    MainDeviceInfo fragmentD;
    MainOpenPorts fragmentP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //code

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        permission.ACCESS_COARSE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{permission.ACCESS_COARSE_LOCATION},
                            0);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }


        }


        if (savedInstanceState == null) {

            fragmentD = new MainDeviceInfo();
            fragmentP = new MainOpenPorts();
            LanzarDeviceInfo();
            LogUtils.LOG("NO");

        } else {
            String tag = fragmentManager.findFragmentById(R.id.container).getTag();
            if (tag == "deviceInfo") {
                fragmentD = (MainDeviceInfo) fragmentManager.getFragment(savedInstanceState, "deviceInfo");
                fragmentP = new MainOpenPorts();
            } else if (tag == "deviceOpenPorts") {
                fragmentP = (MainOpenPorts) fragmentManager.getFragment(savedInstanceState, "deviceOpenPorts");
                fragmentD = new MainDeviceInfo();
                LogUtils.LOG("SI");
            }
        }



      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_device);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_device) {
            fragmentD.flag=false;
            LanzarDeviceInfo();
        } else if (id == R.id.nav_wifi) {

        } else if (id == R.id.nav_ports) {
            LanzarDeviceOpenPorts();
        } else if (id == R.id.nav_test) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void LanzarDeviceInfo() {
        //fragmentD = new MainDeviceInfo();
        fragmentManager.beginTransaction().replace(R.id.container, fragmentD, "deviceInfo")
                //.addToBackStack("LISTADO")
                .commit();

    }

    private void LanzarDeviceOpenPorts() {
        // fragmentP = new MainOpenPorts();
        fragmentManager.beginTransaction().replace(R.id.container, fragmentP, "deviceOpenPorts")
                //.addToBackStack("LISTADO")
                .commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        String tag = fragmentManager.findFragmentById(R.id.container).getTag();

        if (tag == "deviceInfo") {
            fragmentManager.putFragment(outState, "deviceInfo", fragmentD);
        } else if (tag == "deviceOpenPorts") {
            fragmentManager.putFragment(outState, "deviceOpenPorts", fragmentP);
            LogUtils.LOG("SI2");
        } else {
            fragmentManager.putFragment(outState, "deviceInfo", fragmentD);
        }


    }


}
