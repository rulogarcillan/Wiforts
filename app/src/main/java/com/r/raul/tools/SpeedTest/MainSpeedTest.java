package com.r.raul.tools.SpeedTest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.r.raul.tools.R;

import java.net.InetAddress;
import java.util.GregorianCalendar;

import fr.bmartel.speedtest.ISpeedTestListener;
import fr.bmartel.speedtest.SpeedTestSocket;

import static com.r.raul.tools.Utils.LogUtils.LOGE;

/**
 * Created by Rulo on 06/03/2016.
 */
public class MainSpeedTest extends Fragment {

    private IconRoundCornerProgressBar barDonw, barUp;
    private Button empezar;
    private Boolean up = false;
    private TextView txtUp, txtDonw;
    private Boolean txtSpeedUp = false, txtSpeedDown = false;
    private String vel;

    public MainSpeedTest() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.speed_test, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        barDonw = (IconRoundCornerProgressBar) rootView.findViewById(R.id.barDonw);
        barUp = (IconRoundCornerProgressBar) rootView.findViewById(R.id.barUp);
        empezar = (Button) rootView.findViewById(R.id.empezar);
        txtDonw = (TextView) rootView.findViewById(R.id.txtDonw);
        txtUp = (TextView) rootView.findViewById(R.id.txtUp);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.test_m);
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        getBestServer();

        empezar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SpeedTestTask() {
                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        super.onProgressUpdate(values);
                        Boolean flag = true;
                        if (txtSpeedUp) {
                            txtUp.setText(vel);
                            txtSpeedUp = false;
                            flag = false;
                        }
                        if (txtSpeedDown) {
                            txtDonw.setText(vel);
                            txtSpeedDown = false;
                            flag = false;
                        }
                        if (flag) {
                            if (!up) {
                                barDonw.setProgress(values[0]);
                            } else {
                                barUp.setProgress(values[0]);
                            }
                        }

                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        barUp.setProgress(1);
                        barDonw.setProgress(1);
                        txtSpeedUp = false;
                        txtSpeedDown = false;
                        up = false;
                    }


                }.execute();
            }
        });


        return rootView;
    }


    public class SpeedTestTask extends AsyncTask<Void, Integer, String> {


        public SpeedTestTask() {
            super();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values[0]);
        }

        @Override
        protected String doInBackground(Void... params) {


            SpeedTestSocket speedTestSocket = new SpeedTestSocket();


            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {


                @Override
                public void onDownloadPacketsReceived(int packetSize,
                                                      float transferRateBitPerSeconds,
                                                      float transferRateOctetPerSeconds) {

                    txtSpeedDown = true;
                    vel = toMbps(transferRateOctetPerSeconds) + " Mbps";
                    publishProgress(100);

                }

                @Override
                public void onDownloadError(int errorCode, String message) {

                }

                @Override
                public void onUploadPacketsReceived(int packetSize,
                                                    float transferRateBitPerSeconds,
                                                    float transferRateOctetPerSeconds) {
                    txtSpeedUp = true;
                    vel = toMbps(transferRateOctetPerSeconds) + " Mbps";
                    publishProgress(100);
                }

                @Override
                public void onUploadError(int errorCode, String message) {

                }

                @Override
                public void onDownloadProgress(int percent) {
                    up = false;
                    publishProgress(percent);

                }

                @Override
                public void onUploadProgress(int percent) {
                    up = true;
                    publishProgress(percent);
                }

            });


            speedTestSocket.startDownload("ipv4.intuxication.testdebit.info", 80, "/fichiers/10Mo.dat");

            speedTestSocket.startUpload("1.testdebit.info", 80, "/", 10000000); //will block until upload is finished


            return null;
        }
    }

    private double toMbps(float Bps) {

        return Math.round(((double) Bps * 0.000008));
    }


    private String getBestServer() {
        try {


            String ipAddress = "ipv4.intuxication.testdebit.info";
            InetAddress inet = InetAddress.getByName(ipAddress);

            System.out.println("Sending Ping Request to " + ipAddress);

            long finish = 0;
            long start = new GregorianCalendar().getTimeInMillis();

            if (inet.isReachable(5000)) {
                finish = new GregorianCalendar().getTimeInMillis();
                LOGE("Ping RTT: " + (finish - start + "ms"));
            } else {
                LOGE(ipAddress + " NOT reachable.");
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }

        return "server";
    }

}




