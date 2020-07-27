package com.example.conde_gabriel_tesis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapaControl extends Activity implements AdapterView.OnItemSelectedListener, SensorEventListener {
    private Plano myPlano;
    private EditText nombreImagen, ancho, alto;
    private TextView tv;
    private Spinner sp, spBeacons;
    private double altoPlano, anchoPlano, altoCanvas, anchoCanvas, centimetrosCuadrante = 50, recorridoPixelAncho, recorridoPixelAlto;
    private int numColumnas, numFilas, anchoCanvasResize = 1000, altoCanvasResize = 1200;
    private LinearLayout ly;
    private Button guardar, medidas, beaconsActualesbtn, beaconsDisponiblesbtn;
    private Cursor cursorListaBeacons;
    private List<String> uuidSeleccionados = new ArrayList<>();
    private List<String> uuidDisponibles = new ArrayList<>();
    private ImageView iv;
    private ConsultaBD consultaBD;
    private float angulo = 0, anguloGridFrente = 0;
    public static String seleccionBeacon = "";
    private SensorManager sensorManager;
    public static HashMap<String, float[]> beacons = new HashMap<>();
    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];
    private final float[] accelerometerReading2 = new float[3];
    private final float[] magnetometerReading2 = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_mapa);

        consultaBD = new ConsultaBD(this);
        iv = findViewById(R.id.iv);
        myPlano = findViewById(R.id.plano);
        alto = findViewById(R.id.txtAlto);
        ancho = findViewById(R.id.txtAncho);
        nombreImagen = findViewById(R.id.txtNombreImagen);
        tv = findViewById(R.id.txtMostrar);
        guardar = findViewById(R.id.btnGuardarPlano);
        beaconsActualesbtn = findViewById(R.id.btnBeaconsActualesCrear);
        beaconsDisponiblesbtn = findViewById(R.id.btnBeaconsDisponiblesCrear);
        sp = findViewById(R.id.spOpcioneGraficos);
        spBeacons = findViewById(R.id.spBeacons);
        medidas = findViewById(R.id.btnDefinirMedidas);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ly = findViewById(R.id.lySpinner);

        ArrayAdapter<CharSequence> adaptador = ArrayAdapter.createFromResource(this, R.array.Dibujo, android.R.layout.simple_spinner_item);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cursorListaBeacons = consultaBD.EjecutarConsultaSQL("select * from beacon where utiliza_beacon=?", new String[]{Integer.toString(0)});
        int total = cursorListaBeacons.getCount();

        for (int i = 0; i < total; i++) {
            cursorListaBeacons.moveToPosition(i);
            uuidDisponibles.add(cursorListaBeacons.getString(1));
        }


        sp.setAdapter(adaptador);
        sp.setOnItemSelectedListener(this);
        sp.setEnabled(false);
        guardar.setEnabled(false);
        nombreImagen.setEnabled(false);
        alto.setEnabled(false);
        ancho.setEnabled(false);
        medidas.setEnabled(false);
        sp.setEnabled(false);

        //myPlano.setVisibility(View.INVISIBLE);
    }


    public void DefinirMedidas(View view) {
        // myPlano.setVisibility(View.VISIBLE);
        //Define ancho del espacio interior
        anchoPlano = Double.parseDouble(ancho.getText().toString());
        anchoPlano = Math.round(anchoPlano);
        anchoPlano *= 100;
        //Define alto del espacio interior
        altoPlano = Double.parseDouble(alto.getText().toString());
        altoPlano = Math.round(altoPlano);
        altoPlano *= 100;
        //Obtiene medidas del canvas
        anchoCanvas = myPlano.getWidth();
        altoCanvas = myPlano.getHeight();
        //Determina a cuanto equivale en Pixeles un numero determinado de centimetros
        recorridoPixelAncho = Math.round((centimetrosCuadrante * anchoCanvas) / anchoPlano);
        recorridoPixelAlto = Math.round((centimetrosCuadrante * altoCanvas) / altoPlano);
        //determina el numero de fila y columnas
        numColumnas = (int) Math.round(anchoCanvas / recorridoPixelAncho);
        numFilas = (int) Math.round(altoCanvas / recorridoPixelAlto);
        //Define las nuevas medidas del canvas
        anchoCanvasResize = (int) recorridoPixelAncho * numColumnas;
        altoCanvasResize = (int) recorridoPixelAlto * numFilas;
        //Redefine las medias del canvas
        ViewGroup.LayoutParams lp = myPlano.getLayoutParams();
        lp.width = anchoCanvasResize;
        lp.height = altoCanvasResize;
        myPlano.requestLayout();

        //Llamado de funciones de graficacion de inicializacion
        myPlano.Inicializar(anchoCanvasResize, altoCanvasResize);
        System.out.println("X: " + recorridoPixelAncho + " Y: " + recorridoPixelAlto);
        myPlano.DibujarGrid((int) recorridoPixelAncho, (int) recorridoPixelAlto, anchoCanvasResize, altoCanvasResize);
        //habilitar opciones de graficacion
        nombreImagen.setEnabled(true);
        sp.setEnabled(true);
        guardar.setEnabled(true);
        myPlano.setEnabled(true);
        beaconsActualesbtn.setEnabled(true);
        beaconsDisponiblesbtn.setEnabled(true);
    }

    public void GuardarPlano(View view) {
        Bitmap bm = Bitmap.createBitmap(myPlano.getWidth(), myPlano.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        myPlano.draw(canvas);
        if (!nombreImagen.getText().toString().equals("") && !uuidSeleccionados.isEmpty() && anguloGridFrente != 0) {
            try {
                byte[] archivoBlob = consultaBD.CrearByteMapa(bm);
                consultaBD.OperacionMapa(0, nombreImagen.getText().toString(), (int) altoPlano, (int) anchoPlano, (int) recorridoPixelAncho, (int) recorridoPixelAlto, anguloGridFrente, archivoBlob, true);
                Cursor cursorTemp = consultaBD.retornarConsulta(0, "mapa", true);
                cursorTemp.moveToLast();
                int idImagen = cursorTemp.getInt(0);
                int total = cursorListaBeacons.getCount();
                for (int i = 0; i < total; i++) {
                    cursorListaBeacons.moveToPosition(i);
                    for (String uuid : uuidSeleccionados) {
                        if (uuid.equals(cursorListaBeacons.getString(1))) {
                            float[] tempPos = beacons.get(uuid);
                            System.out.println("INSERTAR UUID: " + uuidSeleccionados.get(i) + " ID: " + cursorListaBeacons.getInt(0) + " POSX: " + tempPos[0] + " POSY: " + tempPos[1]);
                            consultaBD.OperacionMapaBeacon(0, idImagen, cursorListaBeacons.getInt(0), (int) tempPos[0], (int) tempPos[1], true);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("utiliza_beacon", 1);
                            consultaBD.Actualizar("beacon", contentValues, cursorListaBeacons.getInt(0));
                        }
                    }
                }
                Toast.makeText(this, "Guardado", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Ingrese todos los datos....", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        consultaBD.CerrarBD();
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
        iv.setRotation(angulo - anguloGridFrente);
    }

    public void Calibrar(View view) {
        anguloGridFrente = angulo;
        alto.setEnabled(true);
        ancho.setEnabled(true);
        medidas.setEnabled(true);
    }

    public void MostrarBeaconsActualesCrear(View view) {
        final ArrayList<Integer> posicionSeleccionado = new ArrayList<>();
        final String[] uuidBeacons = new String[uuidSeleccionados.size()];
        uuidSeleccionados.toArray(uuidBeacons);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Beacons Actuales");
        int seleccion = 0;
        builder.setSingleChoiceItems(uuidBeacons, seleccion, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                posicionSeleccionado.clear();
                posicionSeleccionado.add(which);
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Quitar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uuidDisponibles.add(uuidSeleccionados.get(posicionSeleccionado.get(0)));
                beacons.remove(uuidSeleccionados.get(posicionSeleccionado.get(0)));
                uuidSeleccionados.remove((int) posicionSeleccionado.get(0));
                CargarBeacons();
                myPlano.DibujarPunto();
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

    public void MostrarBeaconsCrear(View view) {
        final ArrayList<Integer> numBeacos = new ArrayList<>();
        final String[] uuidBeacons = new String[uuidDisponibles.size()];
        uuidDisponibles.toArray(uuidBeacons);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Beacons Disponibles");
        int seleccion = 0;
        builder.setSingleChoiceItems(uuidBeacons, seleccion, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numBeacos.clear();
                numBeacos.add(which);
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("Seleccionar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uuidSeleccionados.add(uuidBeacons[numBeacos.get(0)]);
                beacons.put(uuidBeacons[numBeacos.get(0)], new float[2]);
                uuidDisponibles.remove((int) numBeacos.get(0));
                CargarBeacons();
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

    public void CargarBeacons() {
        ArrayAdapter<String> adapterBeacon = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, uuidSeleccionados);
        adapterBeacon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBeacons.setAdapter(adapterBeacon);
        spBeacons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                seleccionBeacon = parent.getItemAtPosition(position).toString();
                System.out.println("OIS: " + seleccionBeacon);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }

    public void ActualizarCoordenadasBeacon(float[] puntos) {
        beacons.put(seleccionBeacon, puntos);
    }

    public static HashMap<String, float[]> getBeacons() {
        return beacons;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String seleccion = parent.getItemAtPosition(position).toString();
        spBeacons.setEnabled(false);
        myPlano.setEsBeacon(false);

        switch (seleccion) {
            case "Obstaculo":
                myPlano.Obstaculo(Color.BLACK);
                ly.setBackgroundColor(Color.TRANSPARENT);
                break;
            case "Puerta":
                myPlano.Obstaculo(Color.GREEN);
                ly.setBackgroundColor(Color.GREEN);
                break;
            case "Escalera":
                myPlano.Obstaculo(Color.YELLOW);
                ly.setBackgroundColor(Color.YELLOW);
                break;
            case "Elevador":
                myPlano.Obstaculo(Color.CYAN);
                ly.setBackgroundColor(Color.CYAN);
                break;
            case "Punto a Considerar":
                myPlano.Obstaculo(Color.MAGENTA);
                ly.setBackgroundColor(Color.MAGENTA);
                break;
            case "Beacon":
                //R.color.AzulBeacon;
                spBeacons.setEnabled(true);
                myPlano.setEsBeacon(true);
                ly.setBackgroundColor(getResources().getColor(R.color.AzulBeacon));
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
