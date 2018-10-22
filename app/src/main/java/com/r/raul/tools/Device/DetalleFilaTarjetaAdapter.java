package com.r.raul.tools.Device;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.r.raul.tools.R;

import java.util.ArrayList;

/**
 * Created by Rulo on 13/12/2015.
 */


public class DetalleFilaTarjetaAdapter extends RecyclerView.Adapter<DetalleFilaTarjetaAdapter.Holder> {

    ArrayList<DetalleFilaTarjeta> array = new ArrayList<DetalleFilaTarjeta >();
    

    public DetalleFilaTarjetaAdapter(ArrayList<DetalleFilaTarjeta > array) {

        this.array = array;
    }

    public ArrayList<DetalleFilaTarjeta > getArray() {
        return array;
    }

    public void setArray(ArrayList<DetalleFilaTarjeta > array) {
        this.array = array;
    }

    public static class Holder extends RecyclerView.ViewHolder {

        public TextView titu_card,conte_card;
		ProgressBar pBar;



        public Holder(View v, int cuantos) {

            super(v);
          
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
			
			if (cuantos>0){
				 titu_card = (TextView) v.findViewById(R.id.titu_card);
				 conte_card = (TextView) v.findViewById(R.id.conte_card);
			}else{				
				pBar = (ProgressBar) v.findViewById(R.id.pBar);
			}

        }
    }


    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
		
		View v;
		Holder pvh;
		if (array.size()>0) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dummy_card_detalle, parent, false);
            pvh = new Holder(v,array.size());
        }else{
			 v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dummy_card_pro, parent, false); //cambiar
			 pvh = new Holder(v,array.size());
		}
        return pvh;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

		if (array.size()>0){
			holder.titu_card.setText(array.get(position).getTitulo());
			holder.conte_card.setText(array.get(position).getContenido());
		}

    }

    @Override
    public int getItemCount() {

        if( array.size() == 0) {
            return 1;
        }else{
            return array.size();
        }
    }


}
