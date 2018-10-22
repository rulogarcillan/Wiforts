package com.r.raul.tools.Ports;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Constantes;

import java.util.ArrayList;

/**
 * Created by Rulo on 11/12/2015.
 */
public class BotoneraAdapter extends RecyclerView.Adapter<BotoneraAdapter.Holder> {

    ArrayList<RangosPuertos> array = new ArrayList<RangosPuertos>();
    Activity activity;

    public BotoneraAdapter(Activity activity) {

        array.add(new RangosPuertos(R.string.custom, ""));
        array.add(new RangosPuertos(R.string.common, Constantes.RANGO1));
        array.add(new RangosPuertos(R.string.trojans, Constantes.RANGO2));
        array.add(new RangosPuertos(R.string.games, Constantes.RANGO3));
        array.add(new RangosPuertos(R.string.others, Constantes.RANGO4));
        array.add(new RangosPuertos(R.string.alls, Constantes.RANGO5));
        this.activity = activity;
    }



    public static class Holder extends RecyclerView.ViewHolder {

        public Button btnPorts;

        public Holder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            btnPorts = (Button) v.findViewById(R.id.btnPorts);


        }
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.boton_ports, parent, false);
        Holder pvh = new Holder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        holder.btnPorts.setText(activity.getResources().getString(array.get(position).getIdNombre()));

    }

    @Override
    public int getItemCount() {
        return array.size();
    }
}
