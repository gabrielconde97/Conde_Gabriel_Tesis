package com.example.conde_gabriel_tesis;


import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Permisos();
        BluetoothLE bluetoothLE = new BluetoothLE(this);
    }
    public void MostrarControlMapa(View view){
        Intent intent = new Intent(this,MapaControl.class);
        startActivity(intent);
    }
    public void MostrarControlUbicacion(View view){
        Intent intent = new Intent(this,UbicacionControl.class);
        startActivity(intent);
    }
    public void MostrarEditarMapa(View view){
        Intent intent = new Intent(this,EditarMapa.class);
        startActivity(intent);
    }
    public void MostrarEliminarMapa(View view){
        Intent intent = new Intent(this,EliminarMapa.class);
        startActivity(intent);
    }
    public void MostrarAgregarBeacon(View view){
        Intent intent = new Intent(this,AgregarBeacon.class);
        startActivity(intent);
    }
    public void MostrarEliminarBeacon(View view){
        Intent intent = new Intent(this,EliminarBeacon.class);
        startActivity(intent);
    }
    public void Permisos() {
        String[] permisos = { Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(this, permisos, 1);
    }


}
