package com.example.conde_gabriel_tesis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class EliminarMapa extends Activity {
    private ConsultaBD consultaBD;
    private Cursor cursorListaMapas;
    private ImageView iv;
    private Button btnEliminar;
    private EditText etNombre;
    private int posicionCursorEliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eliminar_mapa);
        consultaBD = new ConsultaBD(this);
        iv = findViewById(R.id.ivEliminar);
        btnEliminar = findViewById(R.id.btnEliminarMapa);
        etNombre = findViewById(R.id.txtNombreImagenEliminar);
    }

    public void MostrarMapasEliminar(View view) {
        cursorListaMapas = consultaBD.retornarConsulta(0, "mapa", true);
        int totalMapas = cursorListaMapas.getCount();
        final String[] nombresMapas = new String[totalMapas];
        for (int i = 0; i < totalMapas; i++) {
            cursorListaMapas.moveToPosition(i);
            nombresMapas[i] = cursorListaMapas.getString(1);
        }
        final ArrayList<Integer> posicionSeleccionado = new ArrayList<>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mapas Disponibles");
        int seleccion = 0;
        builder.setSingleChoiceItems(cursorListaMapas, seleccion, "nombre_mapa", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                posicionSeleccionado.clear();
                posicionSeleccionado.add(which);
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Cargar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cursorListaMapas.moveToPosition(posicionSeleccionado.get(0));
                String nombre = cursorListaMapas.getString(1);
                byte[] imagen = cursorListaMapas.getBlob(7);
                Bitmap imagenbm = consultaBD.RecuperarMapaByte(imagen);
                iv.setImageBitmap(imagenbm);
                btnEliminar.setEnabled(true);
                etNombre.setText(nombre);
                posicionCursorEliminar = posicionSeleccionado.get(0);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                posicionSeleccionado.clear();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void EliminarMapaBD(View view) {
        cursorListaMapas.moveToPosition(posicionCursorEliminar);
        int idMapa = cursorListaMapas.getInt(0);
        Cursor cursorListaBeacon = consultaBD.EjecutarConsultaSQL("select * from mapa_beacon where id_mapa=?", new String[]{String.valueOf(idMapa)});
        int total = cursorListaBeacon.getCount();
        for (int i = 0; i < total; i++) {
            cursorListaBeacon.moveToPosition(i);
            int idBeacon=cursorListaBeacon.getInt(1);
            ContentValues cv = new ContentValues();
            cv.put("utiliza_beacon",0);
            consultaBD.Actualizar("beacon",cv,idBeacon);
        }
        for (int i = 0; i < total; i++) {
            cursorListaBeacon.moveToPosition(i);
            int idMapaBeacon=cursorListaBeacon.getInt(0);
            consultaBD.Eliminar("mapa_beacon",idMapaBeacon);
        }
        consultaBD.Eliminar("mapa",idMapa);
        Toast.makeText(this,"Eliminado",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
