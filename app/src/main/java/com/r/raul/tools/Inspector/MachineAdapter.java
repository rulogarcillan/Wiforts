package com.r.raul.tools.Inspector;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.r.raul.tools.DB.Consultas;
import com.r.raul.tools.R;
import com.r.raul.tools.Utils.Constantes;

import java.util.ArrayList;

/**
 * Created by Rulo on 13/12/2015.
 */


public class MachineAdapter extends RecyclerView.Adapter<MachineAdapter.Holder> {

    ArrayList<Machine> array = new ArrayList<Machine>();
    Activity activity;
    Consultas consultas;

    public MachineAdapter(Activity activity, ArrayList<Machine> array) {

        this.activity = activity;
        this.array = array;
        consultas = new Consultas(activity);
    }

    public ArrayList<Machine> getArray() {
        return array;
    }

    public void setArray(ArrayList<Machine> array) {
        this.array = array;
    }

    public static class Holder extends RecyclerView.ViewHolder {

        public TextView txtIp;
        public TextView txtMac;
        public TextView txtNombre, txtNombreS;
        public ImageView imgDevice;
        public ToggleButton chkState;

        public Holder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            txtIp = (TextView) v.findViewById(R.id.txtIp);
            txtMac = (TextView) v.findViewById(R.id.txtMac);
            txtNombre = (TextView) v.findViewById(R.id.txtNombre);
            txtNombreS = (TextView) v.findViewById(R.id.txtNombreS);
            imgDevice = (ImageView) v.findViewById(R.id.imgDevice);
            chkState = (ToggleButton) v.findViewById(R.id.chkState);

        }
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_machine, parent, false);
        Holder pvh = new Holder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {

        holder.txtIp.setText("" + array.get(position).getIp());
        String ipColorida="";

       String[] splitado= array.get(position).getIp().split("\\.");
        if (splitado.length==4) {
            ipColorida = splitado[0]+"." + splitado[1]+ "." + splitado[2] + "." +"<font color=\"#fa437e\">" +  splitado[3]  + "</font>";
        }else{
            ipColorida =array.get(position).getIp();
        }


        holder.txtIp.setText(Html.fromHtml(ipColorida));


        holder.txtMac.setText("" + array.get(position).getMac());
        holder.chkState.setChecked(array.get(position).isConocido());
        holder.txtNombre.setText("" + array.get(position).getNombre());
        holder.txtNombreS.setText("" + array.get(position).getNombreSoft());


        holder.chkState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (buttonView.isPressed()) {

                    array.get(position).setConocido(isChecked);

                    InspectorTable inspectorTable = new InspectorTable();
                    inspectorTable.setMacdevice(array.get(position).getMac());
                    inspectorTable.setFavorito(isChecked);
                    inspectorTable.setMacpadre(array.get(position).getMacPadre());


                    consultas.upItemInspectorTable(inspectorTable);
                    // notifyItemChanged(position);
                }

            }

        });
       
        

        switch (array.get(position).getTipoImg()) {
            case Constantes.TIPE_GATEWAY:
                holder.imgDevice.setImageResource(R.drawable.icon_router);
                holder.chkState.setEnabled(false);
                break;
            case Constantes.TIPE_DEVICE:
                holder.imgDevice.setImageResource(R.drawable.icon_device);
                holder.chkState.setEnabled(false);
                break;
            case Constantes.TIPE_OTHERS:
                holder.imgDevice.setImageResource(R.drawable.icon_devices);
                holder.chkState.setEnabled(true);
                break;

        }
    }

    @Override
    public int getItemCount() {
        return array.size();
    }


}
