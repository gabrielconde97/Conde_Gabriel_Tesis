package com.example.conde_gabriel_tesis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class EliminarBeacon extends Activity {
    private Button btnEliminar;
    private EditText uuids;
    private ConsultaBD consultaBD;
    private Cursor cursorListaBeacons;
    private final ArrayList<Integer> numBeaconsEliminar = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eliminar_beacon);

        btnEliminar = findViewById(R.id.btnEliminarBeacons);
        uuids = findViewById(R.id.etBeaconsEliminar);
        consultaBD = new ConsultaBD(this);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        consultaBD.CerrarBD();
    }

    public void MostrarBeaconsNU(View view) {
        final ArrayList<Integer> numBeacos = new ArrayList<>();
        cursorListaBeacons = consultaBD.EjecutarConsultaSQL("select * from beacon where utiliza_beacon=?", new String[]{String.valueOf(0)});
        int total = cursorListaBeacons.getCount();
        final String[] uuidBeacons = new String[total];
        for (int i = 0; i < total; i++) {
            cursorListaBeacons.moveToPosition(i);
            uuidBeacons[i] = cursorListaBeacons.getString(1);
            System.out.println("ID: "+cursorListaBeacons.getInt(3));
        }
        boolean[] seleccionado = new boolean[uuidBeacons.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Beacons Disponibles");
        builder.setMultiChoiceItems(uuidBeacons, seleccionado, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    System.out.println("W: "+which);
                    numBeacos.add(which);
                } else {
                    numBeacos.remove(Integer.valueOf(which));
                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mostrar = "";
                for (int i = 0; i < numBeacos.size(); i++) {
                    mostrar += "UUID: ";
                    mostrar += uuidBeacons[numBeacos.get(i)];
                    mostrar += "\n";
                    numBeaconsEliminar.add(numBeacos.get(i));
                    System.out.println("NUM: "+numBeacos.get(i));
                }
                System.out.println(mostrar);
                uuids.setText(mostrar);
                btnEliminar.setEnabled(true);
            }
        });
        builder.setNegativeButton("CERRAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void EliminarBeacons(View view) {
        for (int idBeacon : numBeaconsEliminar) {
            System.out.println(idBeacon);
            cursorListaBeacons.moveToPosition(idBeacon);
            int id=cursorListaBeacons.getInt(0);
            consultaBD.Eliminar("beacon",id);
        }
        Toast.makeText(this, "Eliminados", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
