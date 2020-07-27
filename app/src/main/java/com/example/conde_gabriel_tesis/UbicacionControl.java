package com.example.conde_gabriel_tesis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UbicacionControl extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];
    private final float[] accelerometerReading2 = new float[3];
    private final float[] magnetometerReading2 = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private Imagen myImagen;
    private ConsultaBD consultaBD;
    private BluetoothLE bluetoothLE;
    private EditText ancho, alto, txtDistancia;
    private TextView tv;
    private String texto = null;
    private double altoPlano, anchoPlano, recorridoPixelAncho, recorridoPixelAlto;
    float angulo = 0, anguloGridFrente = 0;
    private int anchoCanvasResize = 1000, altoCanvasResize = 1200;
    private Uri imageUri;
    private ImageView imagen;
    private List<double[]> posiconesBeacons = new ArrayList<>();
    private List<String> uuidBeacons = new ArrayList<>();
    private boolean comprobarEscaneo=false;
    //private GestureDetector mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_ubicacion);
        myImagen = findViewById(R.id.imagen);
        consultaBD = new ConsultaBD(this);
        bluetoothLE = new BluetoothLE(this);
        alto = findViewById(R.id.txtAlto);
        ancho = findViewById(R.id.txtAncho);
        //txtDistancia = findViewById(R.id.txtDistancia);
        tv = findViewById(R.id.txtMostrar);
        imagen = findViewById(R.id.ivIcon);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        alto.setEnabled(false);
        ancho.setEnabled(false);
        bluetoothLE.EscanerDispositivoBLECercano();
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
            }
        }, 21000);

        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                Cursor beacon = consultaBD.EjecutarConsultaSQL("select * from beacon where uuid_beacon=?", new String[]{bluetoothLE.getUuidCercano()});
                if (beacon.moveToFirst()) {
                    beacon.moveToFirst();
                    int idBeacon = beacon.getInt(0);
                    Cursor mapa_beacon = consultaBD.EjecutarConsultaSQL("select * from mapa_beacon where id_beacon=?", new String[]{Integer.toString(idBeacon)});
                    if (mapa_beacon.moveToFirst()) {
                        mapa_beacon.moveToFirst();
                        int idMapa = mapa_beacon.getInt(mapa_beacon.getColumnIndex("id_mapa"));
                        Cursor mapa = consultaBD.retornarConsulta(idMapa, "mapa", false);
                        if (mapa.moveToFirst()) {
                            mapa.moveToFirst();
                            int id = mapa.getInt(0);
                            String nombre = mapa.getString(1);
                            System.out.println("MAPA: " + nombre);
                            int alto = mapa.getInt(2);
                            int ancho = mapa.getInt(3);
                            int x = mapa.getInt(4);
                            int y = mapa.getInt(5);
                            float calibracion = mapa.getFloat(6);
                            byte[] imagen = mapa.getBlob(7);
                            CargaMapaBeacons(id, nombre, alto, ancho, x, y, calibracion, imagen);
                        } else {
                            //Toast.makeText(getApplicationContext(), "No se encontro el mapa", Toast.LENGTH_LONG).show();
                            RegresarPrincipal("No se encontro el mapa");
                        }
                    } else {
                        //Toast.makeText(getApplicationContext(), "Beacon mas cercano no registrado", Toast.LENGTH_LONG).show();
                        RegresarPrincipal("Beacon mas cercano no registrado");
                    }
                } else {
                    //Toast.makeText(getApplicationContext(), "No hay beacons registrados", Toast.LENGTH_LONG).show();
                    RegresarPrincipal("No hay beacons registrados");
                }
            }
        }, 20000);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                float angulodiagonal = anguloGridFrente + 45;
                float distanciaAngulo = Math.abs(angulo - angulodiagonal);
                if (angulo >= 24 && angulo <= 68)
                    texto = "NORT-ESTE: " + angulo + " D: " + distanciaAngulo;
                if (angulo >= 64 && angulo <= 113)
                    texto = "ESTE " + angulo + " D: " + distanciaAngulo;
                if (angulo >= 114 && angulo <= 158)
                    texto = "SUR-ESTE " + angulo + " D: " + distanciaAngulo;
                if (angulo >= 159 && angulo <= 203)
                    texto = "SUR: " + angulo + " D: " + distanciaAngulo;
                if (angulo >= 204 && angulo <= 248)
                    texto = "SUR-OESTE: " + angulo + " D: " + distanciaAngulo;
                if (angulo >= 249 && angulo <= 293)
                    texto = "OESTE: " + angulo + " D: " + distanciaAngulo;
                if (angulo >= 294 && angulo <= 338)
                    texto = "NOR-OESTE: " + angulo + " D: " + distanciaAngulo;
                if (angulo >= 339 || angulo <= 23)
                    texto = "NORTE: " + angulo + " D: " + distanciaAngulo;
            }
        }, 0, 500);
    }

    private void RegresarPrincipal(String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        builder.setPositiveButton("Página Principal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MostrarPrincipal();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void MostrarPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor typeRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (typeRotationVector != null) {
            sensorManager.registerListener(this, typeRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myImagen.mp != null) {
            myImagen.mp.stop();
        }
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        consultaBD.CerrarBD();
        myImagen.FinalizarVista();
        bluetoothLE.DetenerEscaneoTrilateracion(comprobarEscaneo);
    }

    public double Transformar(double posicion, double sizePix, double sizeCM) {
        double resultado;
        double sizeM = sizeCM / 100;
        resultado = (posicion * sizeM) / (sizePix);
        return resultado;
    }

    public void CargaMapaBeacons(int id, String nombre, int alto, int ancho, int recorridoX, int recorridoY, float calibracion, byte[] imagen) {
        altoPlano = alto;
        System.out.println("Y: " + altoPlano);
        anchoPlano = ancho;
        System.out.println("X: " + anchoPlano);
        recorridoPixelAncho = recorridoX;
        recorridoPixelAlto = recorridoY;
        anguloGridFrente = calibracion;
        Bitmap imagenbm = consultaBD.RecuperarMapaByte(imagen);
        altoCanvasResize = imagenbm.getHeight();
        anchoCanvasResize = imagenbm.getWidth();
        ViewGroup.LayoutParams lp = myImagen.getLayoutParams();
        lp.width = anchoCanvasResize;
        lp.height = altoCanvasResize;
        myImagen.requestLayout();
        this.alto.setText(Integer.toString(altoCanvasResize));
        this.ancho.setText(Integer.toString(anchoCanvasResize));
        myImagen.DibujarImagen(imagenbm);
        myImagen.DefinirDivisionGridPixel((int) recorridoPixelAncho, (int) recorridoPixelAlto, anchoCanvasResize, altoCanvasResize, bluetoothLE);
        Cursor cursor = consultaBD.EjecutarConsultaSQL("select * from mapa_beacon where id_mapa=?", new String[]{String.valueOf(id)});
        int total = cursor.getCount();
        for (int i = 0; i < total; i++) {
            cursor.moveToPosition(i);
            posiconesBeacons.add(new double[]{Transformar(cursor.getInt(3), anchoCanvasResize, anchoPlano), Transformar(cursor.getInt(4), altoCanvasResize, altoPlano)});
            Cursor beaconCursor = consultaBD.EjecutarConsultaSQL("select uuid_beacon from beacon where _id=?", new String[]{String.valueOf(cursor.getInt(1))});
            beaconCursor.moveToFirst();
            uuidBeacons.add(beaconCursor.getString(0));
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading2,
                    0, accelerometerReading2.length);
            accelerometerReading = Filtro(accelerometerReading2, accelerometerReading);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
            magnetometerReading = Filtro(magnetometerReading2, magnetometerReading);
        }
        DetectarRotacion();
    }

    public float[] Filtro(float[] entrada, float[] salida) {
        if (salida == null) return entrada;
        for (int i = 0; i < entrada.length; i++) {
            //salida[i] = salida[i] + 0.25f * (entrada[i] - salida[i]);
            salida[i] = 0.75f * entrada[i] + (1 - 0.75f) * salida[i];
        }
        return salida;
    }

    public void DetectarRotacion() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        angulo = (float) ((Math.toDegrees(orientationAngles[0])) + 360) % 360;
        myImagen.Rotar(angulo, anguloGridFrente, angulo - anguloGridFrente);
        imagen.setRotation(angulo);
        tv.setText(texto);
    }

    public void CalibrarExponente(View view) {
        if (!txtDistancia.getText().toString().equals(""))
            bluetoothLE.EscanearDispositivosBLECalibracion(uuidBeacons, Double.parseDouble(txtDistancia.getText().toString()));
        else
            Toast.makeText(this, "Ingrese la Distancia para calibrar", Toast.LENGTH_LONG).show();

    }

    public void Iniciar(View view) {
        myImagen.RestaurarVista();
        comprobarEscaneo=true;
        myImagen.FinalizarVista();
        bluetoothLE.EscanearDispositivosBLETrilateracion(uuidBeacons, posiconesBeacons, myImagen, anchoCanvasResize, anchoPlano, altoCanvasResize, altoPlano);
        for (int i = 0; i < uuidBeacons.size(); i++) {
            System.out.println("UUID: " + i + " : " + uuidBeacons.get(i));
        }
    }

}
