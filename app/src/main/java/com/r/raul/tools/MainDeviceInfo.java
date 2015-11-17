package com.r.raul.tools;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Rulo on 15/11/2015.
 */
public class MainDeviceInfo extends Fragment {

    TextView txtInfo, txtInfo2;
    Connectivity  con;
    FloatingActionButton fab;

    public static MainDeviceInfo newInstance() {
        MainDeviceInfo fragment = new MainDeviceInfo();
        return fragment;
    }

    public MainDeviceInfo() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.info_device, container, false);

        TextView txtInfo = (TextView) rootView.findViewById(R.id.txtInfo);
        TextView txtInfo2 = (TextView) rootView.findViewById(R.id.txtInfo2);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        NetworkInfo info = Connectivity.getNetworkInfo(getContext());

        TelephonyManager tlfMan = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        if (con.isConnectedWifi(getContext())){
            fab.setImageResource(R.drawable.ic_wifi_ac);
            txtInfo.setText(info.getExtraInfo());
        }
        if (con.isConnectedMobile(getContext())){
            fab.setImageResource(R.drawable.ic_antenna_ac);
            txtInfo.setText(tlfMan.getNetworkOperatorName());
        }

        txtInfo2.setText(con.getType(info.getType(), info.getSubtype()));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (con.isConnectedWifi(getContext())) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
                if (con.isConnectedMobile(getContext())) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }
        });







        return rootView;
        

    }

}
