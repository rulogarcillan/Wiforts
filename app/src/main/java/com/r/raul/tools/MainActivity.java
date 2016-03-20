package com.r.raul.tools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.r.raul.tools.DB.MyDatabase;
import com.r.raul.tools.Device.MainDeviceInfo;
import com.r.raul.tools.Inspector.MainInspector;
import com.r.raul.tools.Ports.MainOpenPorts;
import com.r.raul.tools.SpeedTest.MainSpeedTest;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.cketti.library.changelog.ChangeLog;

import static com.r.raul.tools.Utils.LogUtils.copybd;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment;
    int ItemAnterior = 0;
    public static AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //se crea la base de datos si no existe.
        new MyDatabase(this);

        //copia debug
        copybd();

        /***PUBLI**/
        mAdView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
               // mAdView.setVisibility(View.VISIBLE);
            }
        });

        if (savedInstanceState == null) {
            LanzarDeviceInfo(R.id.nav_device);

        } else {
            String tag = fragmentManager.findFragmentById(R.id.container).getTag();
            fragment = fragmentManager.getFragment(savedInstanceState, "frag");
            fragmentManager.beginTransaction().replace(R.id.container, fragment, tag)
                    //.addToBackStack("LISTADO")
                    .commit();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_device);

        ImageButton navShare = (ImageButton) navigationView.getHeaderView(0).findViewById(R.id.navShare);
        navShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compartirApp();
            }
        });

        TextView mailCabecera = (TextView) navigationView.getHeaderView(0).findViewById(R.id.mailCabecera);
        mailCabecera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactApp();
            }
        });

        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
            new LanzaChangelog(this).getLogDialog().show();
        }
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
            LanzarDeviceInfo(id);


        } else if (id == R.id.nav_wifi_inspector && id != ItemAnterior) {
            LanzarInspector(id);
       /* } else if (id == R.id.nav_wifi && id != ItemAnterior) {*/

        } else if (id == R.id.nav_ports && id != ItemAnterior) {
            LanzarDeviceOpenPorts(id);
      /*  } else if (id == R.id.nav_test && id != ItemAnterior) {*/

        } else if (id == R.id.nav_test && id != ItemAnterior) {

            LanzarSpeedTest(id);

        } else if (id == R.id.nav_info) {

            lanzaInfo();

        } else if (id == R.id.nav_changelog) {

            new LanzaChangelog(this).getFullLogDialog().show();

        } else if (id == R.id.nav_lang) {
            msgTraductor();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void LanzarDeviceInfo(int id) {

        ItemAnterior = id;
        fragment = new MainDeviceInfo();
        fragmentManager.beginTransaction().replace(R.id.container, fragment, "deviceInfo")
                //.addToBackStack("LISTADO")
                .commit();
    }

    private void LanzarInspector(int id) {
        ItemAnterior = id;
        fragment = new MainInspector();
        fragmentManager.beginTransaction().replace(R.id.container, fragment, "inspector")
                //.addToBackStack("LISTADO")
                .commit();
    }

    private void LanzarDeviceOpenPorts(int id) {
        ItemAnterior = id;
        fragment = new MainOpenPorts();
        fragmentManager.beginTransaction().replace(R.id.container, fragment, "deviceOpenPorts")
                //.addToBackStack("LISTADO")
                .commit();
    }

    private void LanzarSpeedTest(int id) {
        ItemAnterior = id;
        fragment = new MainSpeedTest();
        fragmentManager.beginTransaction().replace(R.id.container, fragment, "speedTest")
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
                .withLibraries("jcifs","sqliteassethelper","commonsNet","commonsValidator")
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

    private void msgTraductor() {
   
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_idiomas, null);
        dialogBuilder.setView(dialogView);

        final TextView txt0 = (TextView) dialogView.findViewById(R.id.titu0);
        final TextView txt1 = (TextView) dialogView.findViewById(R.id.titu1);
        final TextView txt2 = (TextView) dialogView.findViewById(R.id.titu2);

        String urlM = "<a href=" + getString(R.string.url1_idiomas) + ">" + getString(R.string.p2_idiomas) +"</a>";
        String urlD = "<a href='" + getString(R.string.url2_idiomas) + "'>" + getString(R.string.p3_idiomas) +"</a>";

        dialogBuilder.setTitle(R.string.titu_idiomas); //Idiomas Languages
        txt0.setText(R.string.p1_idiomas);
        txt1.setMovementMethod(LinkMovementMethod.getInstance());
        txt2.setMovementMethod(LinkMovementMethod.getInstance());
        txt1.setText(Html.fromHtml(urlM));
        txt2.setText(Html.fromHtml(urlD));
        

        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }


}
