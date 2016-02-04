package com.r.raul.tools.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.r.raul.tools.Inspector.InspectorTable;
import com.r.raul.tools.R;

import java.util.ArrayList;

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
        db = new MyDatabase(c);
    }

    public ArrayList getAllInspectorTableFromMacPadre(final String macPadre) {
        String sql = "select inspector.* from inspector where inspector.mac_padre = '" + macPadre + "'";
        LOGI(sql);
        ArrayList<InspectorTable> array = new ArrayList<>();
        Cursor cur = db.query(sql, LEER);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do {
                InspectorTable item = new InspectorTable(cur.getString(0), cur.getString(1), cur.getString(2), cur.getInt(3));
                array.add(item);
            } while (cur.moveToNext());
        }
        db.close();
        return array;
    }


    public void setItemInspectorTable(final InspectorTable item) {

        SQLiteDatabase db2;


        //Creamos el registro a insertar como objeto ContentValues
        ContentValues nuevoRegistro = new ContentValues();
        nuevoRegistro.put("mac_device", item.getMacdevice());
        nuevoRegistro.put("mac_padre", item.getMacpadre());
        nuevoRegistro.put("nombre", item.getNombre());
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
        db2.update("inspector", valores, "mac_device=? AND mac_padre=?", args);
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
            do {
                if (cur.getString(0) != null) {
                    return cur.getString(0);
                }
            } while (cur.moveToNext());
        }
        //db.close();

    return retorno;
}

   /* public static String remove(String input) {

        String original = "'";
        String ascii = "´";
        String output = input;
        for (int i = 0; i < original.length(); i++) {

            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        return output;
    }



    public Integer genKeyIdTablaDia() {
        Cursor cur = null;
        String query;

        query = "select ifnull(max(clave),0) + 1 from diarios";
        cur = db.query(query, LEER);
        LOGI("genKeyIdTabla", query);

        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do {

                return (cur.getInt(0));

            } while (cur.moveToNext());
        }
        db.close();
        return null;

    }

    public Integer genKeyIdTablaReg() {


        Cursor cur = null;
        String query;

        query = "select ifnull(max(clave_r),0) + 1 from registros";
        cur = db.query(query, LEER);
        LOGI("genKeyIdTabla", query);

        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do {

                return (cur.getInt(0));

            } while (cur.moveToNext());
        }
        db.close();
        return null;

    }


    public ArrayList getDiarios() {

        String sql;
        sql = "select diarios.*, fecha.f from diarios LEFT OUTER JOIN  (select  diarios_clave, substr(fecha ,7,4)|| substr(fecha ,4,2) || substr(fecha ,1,2)  as f from registros group by diarios_clave) as  fecha  ON  diarios.clave =  fecha.diarios_clave";
        LOGI("getDiarios", sql);
        ArrayList<Diarios> array = new ArrayList<>();
        Cursor cur = db.query(sql, LEER);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do {

                Diarios item = new Diarios(cur.getInt(0), cur.getString(1), cur.getString(2));
                array.add(item);

            } while (cur.moveToNext());
        }

        db.close();
        return array;
    }

    public String addDiario(Diarios datos) {

        String sql;
        sql = "Insert into diarios (nombre) values ('" + remove(datos.getNombre()) + "')";
        LOGI("addDiario", sql);

        Cursor cur = db.query(sql, ESCRIBIR);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do {
                return (cur.getString(0));

            } while (cur.moveToNext());
        }
        db.close();
        return "0";

    }


    public void deleteDiario(int clave) {


        Cursor cur = null;
        String query, query2;

        query = "delete from diarios where clave = " + clave;
        query2 = "delete from registros where diarios_clave = " + clave;
        LOGI("deleteDiario", query2);
        cur = db.query(query2, ESCRIBIR); //Borra
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }

        LOGI("deleteDiario", query);
        cur = db.query(query, ESCRIBIR); //Borra proyectos
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }

        db.close();

    }

    public void editDiario(int clave, String titu) {


        Cursor cur = null;
        String query;

        query = "update diarios set nombre='" + remove(titu) + "' where clave = " + clave;

        LOGI("editDiario", query);
        cur = db.query(query, ESCRIBIR); //Borra
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }


        db.close();

    }


    public String addRegistros(Logs datos) {

        String sql;
        sql = "Insert into registros (fecha, intensidad, notas, diarios_clave) values ('" + datos.getFecha() + "'," + datos.getIntensidad() + ",'" + remove(datos.getNotas()) + "'," + datos.getClave_d() + ")";
        LOGI("addRegistros", sql);

        Cursor cur = db.query(sql, ESCRIBIR);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do {
                return (cur.getString(0));

            } while (cur.moveToNext());
        }
        db.close();
        return "0";

    }


    public ArrayList getLogs(int clave) {

        String sql;
        sql = "select * from registros where diarios_clave = " + clave + " order by fecha desc";
        LOGI("getLogs", sql);
        ArrayList<Logs> array = new ArrayList<>();
        Cursor cur = db.query(sql, LEER);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do {

                Logs item = new Logs(cur.getInt(0), cur.getString(1), cur.getInt(2), cur.getString(3), cur.getInt(4));
                array.add(item);

            } while (cur.moveToNext());
        }

        db.close();
        Collections.sort(array, new Ordena());
        return array;
    }


    public Logs getOneLog(int clave, int clave_d) {

        String sql;
        sql = "select * from registros where diarios_clave = " + clave_d + " and clave_r = " + clave;
        LOGI("getLogs", sql);
        Logs item = new Logs();
        Cursor cur = db.query(sql, LEER);
        if (cur.moveToFirst()) {
            // Recorremos el cursor hasta que no haya más registros
            do {

                item = new Logs(cur.getInt(0), cur.getString(1), cur.getInt(2), cur.getString(3), cur.getInt(4));


            } while (cur.moveToNext());
        }

        db.close();

        return item;
    }

    public void deleteLog(int clave, int clave_d) {

        Cursor cur = null;
        String query;

        query = "delete from registros where diarios_clave = " + clave_d + " and clave_r = " + clave;
        LOGI("deleteLog", query);
        cur = db.query(query, ESCRIBIR); //Borra
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }

        db.close();

    }


    public void editlog(Logs item) {

        Cursor cur = null;
        String query;

        query = "update registros set notas='" + remove(item.getNotas()) + "', fecha = '" + item.getFecha() + "', intensidad = " + item.getIntensidad() + " where diarios_clave = " + item.getClave_d() + " and clave_r = " + item.getClave();

        LOGI("editlog", query);
        cur = db.query(query, ESCRIBIR); //Borra
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }


        db.close();

    }


    public void deleteBBDD() {


        Cursor cur = null;
        String query, query2;

        query = "delete from diarios";
        query2 = "delete from registros";
        LOGI("deleteRegistros", query2);



        cur = db.query(query2, ESCRIBIR); //Borra
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }

        LOGI("deleteDiarios", query);
        cur = db.query(query, ESCRIBIR); //Borra proyectos
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }

        query = "DeLETE FROM sqlite_sequence WHERE name='diarios'";
        query2 = "DeLETE FROM sqlite_sequence WHERE name='registros'";

        LOGI("deleteSeqDiarios", query);
        cur = db.query(query2, ESCRIBIR); //Borra
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }

        LOGI("deleteSeqDiarios", query);
        cur = db.query(query, ESCRIBIR); //Borra proyectos
        if (cur.moveToFirst()) {
            do {

            } while (cur.moveToNext());
        }


        db.close();

    }*/


}
