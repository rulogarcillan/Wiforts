package com.r.raul.tools;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.r.raul.tools.Device.MainDeviceInfo;
import com.r.raul.tools.Ports.MainOpenPorts;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment;
    int ItemAnterior = R.id.nav_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
            LanzarDeviceInfo();

        } else {
            String tag = fragmentManager.findFragmentById(R.id.container).getTag();
            fragment =  fragmentManager.getFragment(savedInstanceState, "frag");
            fragmentManager.beginTransaction().replace(R.id.container, fragment,tag)
                    //.addToBackStack("LISTADO")
                    .commit();
        }

      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/



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



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.



        int id = item.getItemId();

        if (id == R.id.nav_device && id !=ItemAnterior) {
            LanzarDeviceInfo();

        } else if (id == R.id.nav_wifi_inspector && id !=ItemAnterior) {

        }
        else if (id == R.id.nav_wifi && id !=ItemAnterior) {

        } else if (id == R.id.nav_ports && id !=ItemAnterior) {
            LanzarDeviceOpenPorts();
        } else if (id == R.id.nav_test && id !=ItemAnterior) {

        } else if (id == R.id.nav_license && id !=ItemAnterior) {

        } else if (id == R.id.nav_changelog && id !=ItemAnterior) {

        }

        ItemAnterior = id;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void LanzarDeviceInfo() {
        fragment = new MainDeviceInfo();
        fragmentManager.beginTransaction().replace(R.id.container, fragment, "deviceInfo")
                //.addToBackStack("LISTADO")
                .commit();
    }

    private void LanzarDeviceOpenPorts() {
        fragment = new MainOpenPorts();
        fragmentManager.beginTransaction().replace(R.id.container, fragment, "deviceOpenPorts")
                //.addToBackStack("LISTADO")
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        fragmentManager.putFragment(outState, "frag", fragment);


    }


}
