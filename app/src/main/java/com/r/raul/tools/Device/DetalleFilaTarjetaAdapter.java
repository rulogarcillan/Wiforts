package com.r.raul.tools.Ports;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.r.raul.tools.R;

import java.util.ArrayList;

/**
 * Created by Rulo on 13/12/2015.
 */


public class DetalleFilaTarjetaAdapter extends RecyclerView.Adapter<DetalleFilaTarjetaAdapter.Holder> {

    ArrayList<Puerto> array = new ArrayList<Puerto>();
    

    public DetalleFilaTarjetaAdapter(ArrayList<Puerto> array) {

        this.activity = activity;
        this.array = array;
    }

    public ArrayList<Puerto> getArray() {
        return array;
    }

    public void setArray(ArrayList<Puerto> array) {
        this.array = array;
    }

    public static class Holder extends RecyclerView.ViewHolder {

        public TextView txtPuerto;

        public Holder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            txtPuerto = (TextView) v.findViewById(R.id.txtPuerto);


        }
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_port, parent, false);
        Holder pvh = new Holder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        holder.txtPuerto.setText("" + array.get(position).getPuerto() + " #");

    }

    @Override
    public int getItemCount() {
        return array.size();
    }


}
