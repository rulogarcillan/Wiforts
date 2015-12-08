package com.r.raul.tools;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


public class MainOpenPorts extends Fragment {


    public static MainOpenPorts newInstance() {
        MainOpenPorts fragment = new MainOpenPorts();
        return fragment;
    }

    public MainOpenPorts() {

    }

    Button btnGo;
    RecyclerView recyclerView;
    TabLayout tabLayout;
  LinearLayout filtros;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.open_ports, container, false);


        btnGo = (Button) rootView.findViewById(R.id.btnGo);

        return rootView;
    }
}