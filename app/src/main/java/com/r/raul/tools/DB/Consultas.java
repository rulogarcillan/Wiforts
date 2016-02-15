package com.r.raul.tools.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.r.raul.tools.Inspector.InspectorTable;
import com.r.raul.tools.R;

import java.util.ArrayList;

import static com.r.raul.tools.Utils.LogUtils.LOGE;
import static com.r.raul.tools.Utils.LogUtils.LOGI;

public class Consultas {

    MyDatabase db;
    Context c;
    public static final String LEER = "R";
    public static final String ESCRIBIR = "W";
    private String string;

    public Consultas() {
    }

    public Consultas(Context c) {
        this.c = c;
        this.db = new MyDatabase(c);
    }

    public String insertaDeviceGetNombre(final String macDevice) {

        String sql = "select ifnull(devices.nombre,'-') from devices where devices.mac_device= '" + macDevice + "'";
        Cursor cur = db.query(sql, LEER);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do
            {
                return cur.getString(0);
            }
            while (cur.moveToNext());
        }
        db.close();


        db.close();
        SQLiteDatabase db2;
        //Creamos el registro a insertar como objeto ContentValues
        ContentValues nuevoRegistro = new ContentValues();
        nuevoRegistro.put("mac_device", macDevice);
        //Insertamos el registro en la base de datos
        db2 = db.getWritableDatabase();
        long resultado = db2.insert("devices", null, nuevoRegistro);
        if (resultado == -1) {
            LOGE("Ya estiste el registro: " + macDevice);
        }
        return "-";
    }

    public void upDeviceNombre(final String nombre, final String mac) {
        SQLiteDatabase db2;

        //Actualizar dos registros con update(), utilizando argumentos
        ContentValues valores = new ContentValues();
        valores.put("nombre", !nombre.equals("") ? nombre.toUpperCase():null);

        String[] args = new String[]{mac};
        db2 = db.getWritableDatabase();
        db2.update("devices", valores, "mac_device=?", args);
    }


    public ArrayList getAllInspectorTableFromMacPadre(final String macPadre) {
        String sql = "select inspector.fk_mac_device, inspector.mac_padre, ifnull(devices.nombre,'-'), inspector.favorito from inspector, devices where inspector.fk_mac_device=devices.mac_device and inspector.mac_padre = '" + macPadre + "'";
        LOGI(sql);
        ArrayList<InspectorTable> array = new ArrayList<>();
        Cursor cur = db.query(sql, LEER);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do
            {
                InspectorTable item = new InspectorTable(cur.getString(0), cur.getString(1), cur.getString(2), cur.getInt(3));
                array.add(item);
            }
            while (cur.moveToNext());
        }
        db.close();
        return array;
    }


    public void setItemInspectorTable(final InspectorTable item) {

        SQLiteDatabase db2;

        //Creamos el registro a insertar como objeto ContentValues
        ContentValues nuevoRegistro = new ContentValues();
        nuevoRegistro.put("fk_mac_device", item.getMacdevice());
        nuevoRegistro.put("mac_padre", item.getMacpadre());
        nuevoRegistro.put("favorito", item.getFavorito() ? 1 : 0);

        //Insertamos el registro en la base de datos
        db2 = db.getWritableDatabase();
        db2.insert("inspector", null, nuevoRegistro);
    }

    public void upItemInspectorTable(final InspectorTable item) {
        SQLiteDatabase db2;

        //Actualizar dos registros con update(), utilizando argumentos
        ContentValues valores = new ContentValues();
        valores.put("favorito", item.getFavorito() ? 1 : 0);

        String[] args = new String[]{item.getMacdevice(), item.getMacpadre()};
        db2 = db.getWritableDatabase();
        db2.update("inspector", valores, "fk_mac_device=? AND mac_padre=?", args);
    }


    public String getNameFromMac(final String mac) {

        String[] macSplit = mac.split(":");
        String retorno = "";
        String filtro = string;
        ArrayList<String> macs = new ArrayList<>();
        for (int i = macSplit.length; i >= 1; i--) {
            String mascara = "";

            for (int j = i; j >= 1; j--) {
                mascara = macSplit[j - 1] + mascara;
            }
            macs.add(mascara);
        }

        for (String macFilter : macs) {

            filtro = filtro + ",upper('" + macFilter + "')";
        }

        String sql = "select software.name_l from software where upper(software.mac) in (" + filtro + ")  order by length(mac) desc";
        LOGI(sql);
        retorno = c.getString(R.string.desconocido);
        Cursor cur = db.query(sql, LEER);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do
            {
                if (cur.getString(0) != null) {
                    return cur.getString(0);
                }
            }
            while (cur.moveToNext());
        }
        //db.close();

        return retorno;
    }

   


}
