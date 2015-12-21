package com.r.raul.tools;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.ApplicationInfo;
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
import android.view.ContextThemeWrapper;
import android.view.MenuItem;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.r.raul.tools.Device.MainDeviceInfo;
import com.r.raul.tools.Ports.MainOpenPorts;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.cketti.library.changelog.ChangeLog;

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
            fragment = fragmentManager.getFragment(savedInstanceState, "frag");
            fragmentManager.beginTransaction().replace(R.id.container, fragment, tag)
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

        if (id == R.id.nav_device && id != ItemAnterior) {
            LanzarDeviceInfo();

        } else if (id == R.id.nav_wifi_inspector && id != ItemAnterior) {

        } else if (id == R.id.nav_wifi && id != ItemAnterior) {

        } else if (id == R.id.nav_ports && id != ItemAnterior) {
            LanzarDeviceOpenPorts();
        } else if (id == R.id.nav_test && id != ItemAnterior) {

            // } else if (id == R.id.nav_opciones) {

        } else if (id == R.id.nav_info) {

            lanzaInfo();

        } else if (id == R.id.nav_changelog) {
            new LanzaChangelog(this).getFullLogDialog().show();

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

    private void lanzaInfo() {
        String s = "";
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            SimpleDateFormat formatter1 = new SimpleDateFormat("DD/mm/yyyy");
            s = formatter1.getInstance().format(new java.util.Date(time));
            s = formatter1.format(s);


        } catch (Exception e) {

        }

        new LibsBuilder()
                //Pass the fields of your application to the lib so it can find all external lib information
                .withFields(R.string.class.getFields())
                .withVersionShown(true)
                .withLicenseShown(true)
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAutoDetect(true)
                        //  .withLibraries("DiscreteSeekBar", "CircleIndicator")
                .withActivityTitle(getResources().getString(R.string.license))
                .withAboutAppName(getResources().getString(R.string.app_name))
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)

                .withAboutDescription("<b>" + getResources().getString(R.string.compilacion) + ": <i>" + s + "</i></b>")
                        // .withAboutDescription(getResources().getString(R.string.escrita) + "<br/><br/><b>License GNU GPL V3.0</b><br/><br/><a href=\"https://github.com/rulogarcillan/Cadence\">Project in Github</a>")
                        //     .withActivityTheme(R.style.AppTheme)
                        //start the activity
                .withActivityTheme(R.style.AppTheme2)
                .start(this);
    }


    public static class LanzaChangelog extends ChangeLog {


        public static final String DEFAULT_CSS =

                "body {                                                           " + "	font-family: Verdana, Helvetica, Arial, sans-serif;   " + "	font-size: 11px;                                      " + "	color: #000000;                                       " + "	background-color: #ffffff;                            " + "	margin: 0px;                                          " + "	padding: 0px;                                         " + "}                                                        "
                        + "h1 {                                                     " + "	font-size: 14px;                                      " + "	font-weight: bold;                                    " + "	text-transform: uppercase;                            " + "	color: #000000;                                       " + "	margin: 0px;                                          " + "	padding: 10px 0px 0px 8px;                            " + "}                                                        "
                        + "h2 {                                                     " + "	font-size: 10px;                                      " + "	color: #999999;                                       " + "	font-weight: normal;                                  " + "	margin: 0px 0px 0px 8px;                              " + "	padding: 0px;                                         " + "}                                                        " + "ul {                                                     "
                        + "	margin: 0px 0px 10px 15px;                            " + "	padding-left: 15px;                                " + "	padding-top: 8px;                                     " + "	list-style-type: square;                              " + "	color: #999999;                                       " + "}";

        public LanzaChangelog(Context context) {
            super(new ContextThemeWrapper(context, R.style.AppTheme), DEFAULT_CSS);
        }
    }


}
