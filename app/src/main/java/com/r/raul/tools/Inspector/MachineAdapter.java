package com.r.raul.tools.Inspector;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Constantes;

import java.util.ArrayList;

/**
 * Created by Rulo on 13/12/2015.
 */


public class MachineAdapter extends RecyclerView.Adapter<MachineAdapter.Holder> {

    ArrayList<Machine> array = new ArrayList<Machine>();
    Activity activity;

    public MachineAdapter(Activity activity, ArrayList<Machine> array) {

        this.activity = activity;
        this.array = array;
    }

    public ArrayList<Machine> getArray() {
        return array;
    }

    public void setArray(ArrayList<Machine> array) {
        this.array = array;
    }

    public static class Holder extends RecyclerView.ViewHolder {

        public TextView txtIp;
        public ImageView imgDevice;

        public Holder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            txtIp = (TextView) v.findViewById(R.id.txtIp);
            imgDevice = (ImageView) v.findViewById(R.id.imgDevice);

        }
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_machine, parent, false);
        Holder pvh = new Holder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        holder.txtIp.setText("" + array.get(position).getIp());

        switch (array.get(position).getTipoImg()){
            case Constantes.TIPE_GATEWAY:
                holder.imgDevice.setImageResource(R.drawable.ic_router);
                break;
            case Constantes.TIPE_OTHERS:

                holder.imgDevice.setImageResource(R.drawable.ic_devices);
                break;
        }


    }

    @Override
    public int getItemCount() {
        return array.size();
    }


}
