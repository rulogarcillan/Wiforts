package com.r.raul.tools.SpeedTest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.r.raul.tools.R;

import fr.bmartel.speedtest.ISpeedTestListener;
import fr.bmartel.speedtest.SpeedTestSocket;

/**
 * Created by Rulo on 06/03/2016.
 */
public class MainSpeedTest extends Fragment {

    private IconRoundCornerProgressBar barDonw, barUp;
    private AppCompatButton empezar;

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
        empezar = (AppCompatButton) rootView.findViewById(R.id.empezar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.test_m);
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        empezar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SpeedTestTask() {
                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        super.onProgressUpdate(values);
                        barDonw.setProgress(values[0]);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        barUp.setProgress(1);
                        barDonw.setProgress(1);
                    }

                    @Override
                    protected void finalize() throws Throwable {
                        super.finalize();
                        new SpeedTestTaskUp() {
                            @Override
                            protected void onProgressUpdate(Integer... values) {
                                super.onProgressUpdate(values);
                                barUp.setProgress(values[0]);
                            }
                        }.execute();
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


                    System.out.println("download transfer rate  : " + transferRateBitPerSeconds + " bps");
                    System.out.println("download transfer rate  : " + transferRateOctetPerSeconds + "Bps");
                }

                @Override
                public void onDownloadError(int errorCode, String message) {

                }

                @Override
                public void onUploadPacketsReceived(int packetSize,
                                                    float transferRateBitPerSeconds,
                                                    float transferRateOctetPerSeconds) {



                }

                @Override
                public void onUploadError(int errorCode, String message) {

                }

                @Override
                public void onDownloadProgress(int percent) {

                    publishProgress(percent);

                }

                @Override
                public void onUploadProgress(int percent) {

                }

            });


            speedTestSocket.startDownload("ipv4.intuxication.testdebit.info", 80, "/fichiers/10Mo.dat");

           return null;
        }
    }

    public class SpeedTestTaskUp extends AsyncTask<Void, Integer, String> {


        public SpeedTestTaskUp() {
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


                }

                @Override
                public void onDownloadError(int errorCode, String message) {

                }

                @Override
                public void onUploadPacketsReceived(int packetSize,
                                                    float transferRateBitPerSeconds,
                                                    float transferRateOctetPerSeconds) {


                    System.out.println("download transfer rate  : " + transferRateBitPerSeconds + " bps");
                    System.out.println("download transfer rate  : " + transferRateOctetPerSeconds + "Bps");
                }

                @Override
                public void onUploadError(int errorCode, String message) {

                }

                @Override
                public void onDownloadProgress(int percent) {


                }

                @Override
                public void onUploadProgress(int percent) {
                    publishProgress(percent);
                }

            });


            speedTestSocket.startUpload("1.testdebit.info",
                    80, "/", 10000000); //will block until upload is finished

            return null;
        }
    }
}




