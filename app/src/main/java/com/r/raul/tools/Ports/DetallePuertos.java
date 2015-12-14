package com.r.raul.tools.Ports;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.r.raul.tools.BaseActivity;
import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Constantes;
import com.r.raul.tools.Utils.DividerItemDecoration;
import com.r.raul.tools.Utils.LogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rulo on 12/12/2015.
 */
public class DetallePuertos extends BaseActivity {


    private String ip;
    private int timeOut;
    private ProgressBar progressBar;
    private AnalizarPuertos tarea = new AnalizarPuertos();
    private ArrayList<Integer> listaPuertos = new ArrayList<Integer>();
    private String ports;
    private TabLayout tabLayout;
    private ArrayList<Puerto> arrayAbiertos = new ArrayList<>();
    private ArrayList<Puerto> arrayCerrados = new ArrayList<>();
    private ArrayList<Puerto> arrayTimeOut = new ArrayList<>();


    private RecyclerView recdetalle;
    private DetallePuertosAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();

        ports = intent.getStringExtra(Constantes.PORTS);
        ip = intent.getStringExtra(Constantes.IP);
        timeOut = intent.getIntExtra(Constantes.TIMEOUT, 200);


        setContentView(R.layout.puertos_detalle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(ip);

        tabLayout = (TabLayout) findViewById(R.id.appbartabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.p_abiertos).replace("#", Integer.toString(0))));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.p_cerrados).replace("#", Integer.toString(0))));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.p_time).replace("#", Integer.toString(0))));

        adaptador = new DetallePuertosAdapter(this, new ArrayList<Puerto>());
        recdetalle = (RecyclerView) findViewById(R.id.recdetalle);
        recdetalle.setHasFixedSize(true);
        recdetalle.setAdapter(adaptador);
        recdetalle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recdetalle.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        parsea(ports);
        tarea = new AnalizarPuertos(progressBar, this, ip, timeOut);
        tarea.execute(listaPuertos);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 0) {

                    adaptador.setArray(arrayAbiertos);
                    adaptador.notifyDataSetChanged();


                } else if (tab.getPosition() == 1) {

                    adaptador.setArray(arrayCerrados);
                    adaptador.notifyDataSetChanged();

                } else if (tab.getPosition() == 2) {

                    adaptador.setArray(arrayTimeOut);
                    adaptador.notifyDataSetChanged();

                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }


    private class AnalizarPuertos extends AsyncTask<ArrayList<Integer>, Integer, Boolean> {

        final ExecutorService es = Executors.newFixedThreadPool(100);
        Activity ac;
        String ipAsync;
        int timeOutAsync;
        int ab = 0, ce = 0, tot = 0, to = 0;
        int sleep = 0;


        int tamanno;
        ProgressBar progressBar2;

        final ArrayList<Future<Puerto>> futures = new ArrayList<>();

        public AnalizarPuertos(ProgressBar progressBar2, Activity ac, String ipAsync, int timeOutAsync) {
            this.ac = ac;
            this.ipAsync = ipAsync;
            this.timeOutAsync = timeOutAsync;
            this.progressBar2 = progressBar2;

        }

        public AnalizarPuertos() {
        }


        @Override
        protected Boolean doInBackground(ArrayList<Integer>... params) {

            tamanno = params[0].size();
            arrayAbiertos = new ArrayList<>();
            arrayCerrados = new ArrayList<>();

            for (int puerto : params[0]) {
                futures.add(es.submit(new AnalizaPuerto(ipAsync, puerto, timeOutAsync)));
            }
            try {
                es.awaitTermination(timeOutAsync, TimeUnit.MILLISECONDS);
                for (final Future<Puerto> f : futures) {


                    if (f.get().getIsOpen() == 0) {
                        tot++;
                        ab++;
                        publishProgress(tot);

                        arrayAbiertos.add(f.get());
                    } else if (f.get().getIsOpen() == 1) {
                        tot++;
                        ce++;
                        publishProgress(tot);

                        arrayCerrados.add(f.get());
                    } else if (f.get().getIsOpen() == 2) {
                        tot++;
                        to++;
                        publishProgress(tot);

                        arrayTimeOut.add(f.get());

                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar2.setVisibility(View.VISIBLE);
            progressBar2.setMax(100);
            progressBar2.setProgress(0);
            adaptador.setArray(arrayAbiertos);

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressBar2.setVisibility(View.INVISIBLE);

        }


        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values[0]);
            int valor = values[0] * 100 / tamanno;
            progressBar2.setProgress(valor);

            if (timeOutAsync < 1000) {
                sleep++;
                switch (timeOutAsync) {
                    case 100:
                        if (sleep == 10) {
                            sleep = 0;
                            actualizaTabs();
                        }
                        break;
                    case 200:
                        if (sleep == 5) {
                            sleep = 0;
                            actualizaTabs();
                        }
                        break;
                    case 300:
                        if (sleep == 4) {
                            sleep = 0;
                            actualizaTabs();
                        }
                        break;
                    case 500:
                        if (sleep == 2) {
                            sleep = 0;
                            actualizaTabs();
                        }
                        break;
                }

            } else {

                actualizaTabs();
            }


        }


        private void actualizaTabs() {
            if (ac != null) {
                adaptador.notifyDataSetChanged();
                tabLayout.getTabAt(0).setText(ac.getString(R.string.p_abiertos).replace("#", Integer.toString(ab)));
                tabLayout.getTabAt(1).setText(ac.getString(R.string.p_cerrados).replace("#", Integer.toString(ce)));
                tabLayout.getTabAt(2).setText(ac.getString(R.string.p_time).replace("#", Integer.toString(to)));
            }

        }
    }


    public boolean parsea(String lista) {
        listaPuertos = new ArrayList<Integer>();

        Set<Integer> hs = new HashSet<>();
        lista = lista.replaceAll("[^0-9,-]", "");
        lista = lista.replace(" ", "");
        String[] partsComa = lista.split(",");
        if (lista.equals("")) {
            return false;
        }
        for (String portsComa : partsComa) {

            if (portsComa.contains("-")) {
                String[] partsGuion = portsComa.split("\\-");

                if (partsGuion.length == 2) {
                    int ini = Integer.parseInt(partsGuion[0]);
                    int fin = Integer.parseInt(partsGuion[1]);
                    if (fin < ini) {
                        fin = Integer.parseInt(partsGuion[0]);
                        ini = Integer.parseInt(partsGuion[1]);
                    }
                    if (fin > 65535) {
                        fin = 65535;
                    }
                    if (ini == 0) {
                        ini = 1;
                    }

                    for (int i = ini; i <= fin; i++) {

                        listaPuertos.add(i);
                    }

                } else {
                    return false;
                }

            } else {

                if (Integer.parseInt(portsComa) > 0 && Integer.parseInt(portsComa) <= 65535) {
                    listaPuertos.add(Integer.parseInt(portsComa));
                }
            }
        }

        hs.addAll(listaPuertos);
        listaPuertos.clear();
        listaPuertos.addAll(hs);

        if (listaPuertos.size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Write your logic here
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        tarea.cancel(true);
        LogUtils.LOG("PARADO");
    }

}
