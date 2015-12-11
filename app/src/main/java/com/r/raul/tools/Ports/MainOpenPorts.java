package com.r.raul.tools.Ports;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.r.raul.tools.R;
import com.r.raul.tools.Utils.LogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainOpenPorts extends Fragment {


    public static MainOpenPorts newInstance() {
        MainOpenPorts fragment = new MainOpenPorts();
        return fragment;
    }

    public MainOpenPorts() {

    }


    RecyclerView botoneraRecycler;
    TextView txtPorst, txtIpHost;
    Spinner spinner;
    Button btnAceptar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.open_ports, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        txtIpHost = (TextView) rootView.findViewById(R.id.txtIpHost);

        btnAceptar = (Button) rootView.findViewById(R.id.btnAceptar);
        spinner = (Spinner) rootView.findViewById(R.id.spinner);
        txtPorst = (TextView) rootView.findViewById(R.id.txtPorst);
        botoneraRecycler = (RecyclerView) rootView.findViewById(R.id.botoneraRecycler);
        botoneraRecycler.setHasFixedSize(true);

        //la ultima para que este todo inicializado
        setupUI(rootView.findViewById(R.id.contenedor));


        final BotoneraAdapter adaptador = new BotoneraAdapter(getActivity()) {
            @Override
            public void onBindViewHolder(Holder holder, final int position) {
                super.onBindViewHolder(holder, position);
                holder.btnPorts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        txtPorst.setText(array.get(position).getRangoPuertos());
                        parsea(array.get(position).getRangoPuertos());
                    }
                });

            }


        };
        botoneraRecycler.setAdapter(adaptador);
        botoneraRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        loadSnniper();
        txtPorst.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                txtPorst.setSingleLine(!hasFocus);
            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parsea(txtPorst.getText().toString());
            }
        });

        return rootView;
    }


    public boolean parsea(String lista) {

        ArrayList<Integer> listaPuertos = new ArrayList<Integer>();


// add elements to al, including duplicates
        Set<Integer> hs = new HashSet<>();




        LogUtils.LOG(lista);
        lista = lista.replaceAll("[^0-9,-]", "");
        lista = lista.replace(" ", "");
        LogUtils.LOG(lista);
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

                    for (int i = ini; i <= fin; i++) {

                        listaPuertos.add(i);
                    }

                } else {
                    return false;
                }

            } else {
                listaPuertos.add(Integer.parseInt(portsComa));
            }
        }

        hs.addAll(listaPuertos);
        listaPuertos.clear();
        listaPuertos.addAll(hs);

        LogUtils.LOGE(listaPuertos.size() + "");

        if (listaPuertos.size() == 0){
            return false;
        }
        return true;
    }

    private void loadSnniper() {


        List<String> categories = new ArrayList<String>();
        categories.add("100");
        categories.add("200");
        categories.add("300");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }

    }


}