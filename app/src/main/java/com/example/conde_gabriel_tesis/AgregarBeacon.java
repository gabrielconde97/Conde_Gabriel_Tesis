package com.example.conde_gabriel_tesis;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgregarBeacon extends Activity {
    private EditText uuids;
    private BluetoothLE bluetoothLE;
    private ConsultaBD consultaBD;
    private final ArrayList<Integer> numBeacos = new ArrayList<>();
    private String[] uuidBeacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agregar_beacon);
        uuids = findViewById(R.id.etBeacons);
        bluetoothLE = new BluetoothLE(this);
        consultaBD = new ConsultaBD(this);
    }

    public void BuscarBeacons(View view) {
        bluetoothLE.EscanearDispositivosBLE();
        final Button mostrarBeacon = findViewById(R.id.btnMostarBeacons);
        final Button guardarBeacon = findViewById(R.id.btnGuardarBeacons);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Buscando Beacons!!!!!!!!!");
        builder.setMessage("BUSCANDO..........");
        builder.setCancelable(false);
        final AlertDialog alert = builder.create();
        alert.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alert.setMessage("Termine...");
                alert.dismiss();
                mostrarBeacon.setEnabled(true);
                guardarBeacon.setEnabled(true);
            }
        }, 21000);
    }

    public void MostrarBeacons(View view) {
        List<String> beaconsFinales = new ArrayList<>();
        Cursor cursor = consultaBD.retornarConsulta(0, "beacon", true);
        int total = cursor.getCount();
        for (String uuidTemp : bluetoothLE.getUuids()) {
            boolean comprobarBD=false;
            for (int i = 0; i < total; i++) {
                System.out.println("ESTOY FOR");
                cursor.moveToPosition(i);

                if(uuidTemp.equals(cursor.getString(1))){
                    System.out.println("true: "+cursor.getString(1) );
                    comprobarBD=true;
                    break;
                }
            }
            if(!comprobarBD) beaconsFinales.add(uuidTemp);
        }


        //uuidBeacons = new String[bluetoothLE.getUuids().size()];
        //bluetoothLE.getUuids().toArray(uuidBeacons);
        uuidBeacons = new String[beaconsFinales.size()];
        beaconsFinales.toArray(uuidBeacons);
        boolean[] seleccionado = new boolean[uuidBeacons.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("BEACONS ENCONTRADAS!!!!!!!!!");
        builder.setMultiChoiceItems(uuidBeacons, seleccionado, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    numBeacos.add(which);
                } else {
                    numBeacos.remove(Integer.valueOf(which));
                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mostrar = "";
                for (int i = 0; i < numBeacos.size(); i++) {
                    mostrar += "UUID: ";
                    mostrar += uuidBeacons[numBeacos.get(i)];
                    mostrar += "\n";
                }
                System.out.println(mostrar);
                uuids.setText(mostrar);
            }
        });
        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void GuardarBeacons(View view) {
        for (Map.Entry<String, BluetoothDevice> device : bluetoothLE.getDispositvosBluetooth().entrySet()) {
            for (int i = 0; i < numBeacos.size(); i++) {
                if (device.getKey().equals(uuidBeacons[numBeacos.get(i)])) {
                    consultaBD.OperacionBeacon(0, device.getKey(), device.getValue().getAddress(), 0, true);
                }
            }
        }
        Toast.makeText(this, "Guardado", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        consultaBD.CerrarBD();
    }

}
