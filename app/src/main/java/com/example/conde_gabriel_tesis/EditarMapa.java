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

public class EditarMapa extends Activity implements AdapterView.OnItemSelectedListener, SensorEventListener {

    private Plano myPlano;
    private EditText nombreImagen, ancho, alto;
    private TextView tv;
    private Spinner sp, spBeacons;
    private double altoPlano, anchoPlano, recorridoPixelAncho, recorridoPixelAlto;
    private int anchoCanvasResize = 1000, altoCanvasResize = 1200, idImagen;
    private LinearLayout ly;
    private Button actualizar, calibrar, mostrarBeacons, mostrarBeaconActuales;
    private Cursor cursorListaBeacons, cursorListaMapas;
    private List<String> uuidSeleccionados = new ArrayList<>();
    private List<String> uuidDisponibles = new ArrayList<>();
    private List<Integer> idBeacons = new ArrayList<>();
    private List<Integer> idMapaBeacon = new ArrayList<>();
    private Cursor cursorBeacons;
    private ImageView iv;
    private ConsultaBD consultaBD;
    private float angulo = 0, anguloGridFrente = 0;
    private SensorManager sensorManager;
    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];
    private final float[] accelerometerReading2 = new float[3];
    private final float[] magnetometerReading2 = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    public static HashMap<String, float[]> beacons = new HashMap<>();
    public static String seleccionBeacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_mapa);
        consultaBD = new ConsultaBD(this);
        iv = findViewById(R.id.ivEditar);
        myPlano = findViewById(R.id.planoEditar);
        alto = findViewById(R.id.txtAltoEditar);
        ancho = findViewById(R.id.txtAnchoEditar);
        nombreImagen = findViewById(R.id.txtNombreImagenEditar);
        tv = findViewById(R.id.txtMostrarEditar);


        actualizar = findViewById(R.id.btnGuardarPlanoEditar);
        calibrar = findViewById(R.id.btnCalibrarEditar);
        mostrarBeacons = findViewById(R.id.btnBeaconsEditar);
        mostrarBeaconActuales= findViewById(R.id.btnBeaconsActuales);

        sp = findViewById(R.id.spOpcioneGraficosEditar);
        spBeacons = findViewById(R.id.spBeaconsEditar);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        ly = findViewById(R.id.lySpinnerEditar);

        ArrayAdapter<CharSequence> adaptador = ArrayAdapter.createFromResource(this, R.array.Dibujo, android.R.layout.simple_spinner_item);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adaptador);
        sp.setOnItemSelectedListener(this);

        sp.setEnabled(false);
        actualizar.setEnabled(false);
        calibrar.setEnabled(false);
        mostrarBeacons.setEnabled(false);
        mostrarBeaconActuales.setEnabled(false);
        nombreImagen.setEnabled(false);
        alto.setEnabled(false);
        ancho.setEnabled(false);

        cursorListaBeacons = consultaBD.EjecutarConsultaSQL("select * from beacon where utiliza_beacon=?", new String[]{String.valueOf(0)});
        int total = cursorListaBeacons.getCount();

        for (int i = 0; i < total; i++) {
            cursorListaBeacons.moveToPosition(i);
            uuidDisponibles.add(cursorListaBeacons.getString(1));
        }

    }


    public void MostrarMapas(View view) {
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
                int id = cursorListaMapas.getInt(0);
                String nombre = cursorListaMapas.getString(1);
                int alto = cursorListaMapas.getInt(2);
                int ancho = cursorListaMapas.getInt(3);
                int x = cursorListaMapas.getInt(4);
                int y = cursorListaMapas.getInt(5);
                float calibracion = cursorListaMapas.getFloat(6);
                byte[] imagen = cursorListaMapas.getBlob(7);
                InicioVariables(id, nombre, alto, ancho, x, y, calibracion, imagen);
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

    public void InicioVariables(int id, String nombre, int alto, int ancho, int recorridoX, int recorridoY, float calibracion, byte[] imagen) {
        idImagen = id;
        nombreImagen.setText(nombre);
        altoPlano = alto;
        anchoPlano = ancho;
        recorridoPixelAncho = recorridoX;
        recorridoPixelAlto = recorridoY;
        anguloGridFrente = calibracion;
        Bitmap imagenbm = consultaBD.RecuperarMapaByte(imagen);
        altoCanvasResize = imagenbm.getHeight();
        anchoCanvasResize = imagenbm.getWidth();
        ViewGroup.LayoutParams lp = myPlano.getLayoutParams();
        lp.width = anchoCanvasResize;
        lp.height = altoCanvasResize;
        myPlano.requestLayout();
        Cursor cursor = consultaBD.EjecutarConsultaSQL("select * from mapa_beacon where id_mapa=?", new String[]{String.valueOf(idImagen)});
        int total = cursor.getCount();
        List<float[]> tempPost = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            cursor.moveToPosition(i);
            idBeacons.add(cursor.getInt(1));
            idMapaBeacon.add(cursor.getInt(0));
            //beacons.put()
            tempPost.add(new float[]{cursor.getInt(3), cursor.getInt(4)});
            System.out.println(" INICIO ID: " + cursor.getInt(1) + " POSX: " + cursor.getInt(3) + " POSY: " + cursor.getInt(4));
        }

        String[] parametros = new String[idBeacons.size()];
        String condicion = "_id in(";
        for (int i = 0; i < idBeacons.size(); i++) {
            parametros[i] = String.valueOf(idBeacons.get(i));
            if (i == idBeacons.size() - 1) condicion += "?";
            else condicion += "?,";
        }
        condicion += ")";
        cursorBeacons = consultaBD.EjecutarConsultaSQL("select * from beacon where " + condicion, parametros);
        int totalBeacons = cursorBeacons.getCount();
        for (int i = 0; i < totalBeacons; i++) {
            cursorBeacons.moveToPosition(i);
            uuidSeleccionados.add(cursorBeacons.getString(1));//carga de beacons actuales del mapa
            beacons.put(cursorBeacons.getString(1), tempPost.get(i));
        }
        CargarBeacons();
        sp.setEnabled(true);
        actualizar.setEnabled(true);
        // medidas.setEnabled(false);
        calibrar.setEnabled(true);
        mostrarBeacons.setEnabled(true);
        mostrarBeaconActuales.setEnabled(true);
        nombreImagen.setEnabled(true);
        this.alto.setText(Integer.toString(altoCanvasResize));
        this.ancho.setText(Integer.toString(anchoCanvasResize));
        myPlano.InicializarEditar(anchoCanvasResize, altoCanvasResize, (int) recorridoPixelAncho, (int) recorridoPixelAlto, imagenbm, tempPost);
    }

    public void ActualizarMapa(View view) {
        Bitmap bm = Bitmap.createBitmap(myPlano.getWidth(), myPlano.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        myPlano.draw(canvas);
        if (!nombreImagen.getText().toString().equals("") && !uuidSeleccionados.isEmpty() && anguloGridFrente != 0) {
            try {
                for (int i = 0; i < idMapaBeacon.size(); i++) {
                    consultaBD.Eliminar("mapa_beacon", idMapaBeacon.get(i));
                }
                byte[] archivoBlob = consultaBD.CrearByteMapa(bm);
                consultaBD.OperacionMapa(idImagen, nombreImagen.getText().toString(), (int) altoPlano, (int) anchoPlano, (int) recorridoPixelAncho, (int) recorridoPixelAlto, anguloGridFrente, archivoBlob, false);
                List<Integer> idBeaconsInsertar = new ArrayList<>();
                Cursor beaconsTemp = consultaBD.EjecutarConsultaSQL("select * from beacon", null);
                int total = beaconsTemp.getCount();
                for (int i = 0; i < total; i++) {
                    beaconsTemp.moveToPosition(i);
                    for (String uuidSelec : uuidSeleccionados) {
                        if (uuidSelec.equals(beaconsTemp.getString(1))) {
                            ContentValues cv = new ContentValues();
                            cv.put("utiliza_beacon", 1);
                            idBeaconsInsertar.add(beaconsTemp.getInt(0));
                            consultaBD.Actualizar("beacon", cv, beaconsTemp.getInt(0));
                        }
                    }
                    for (String uuidDisp : uuidDisponibles) {
                        if (uuidDisp.equals(beaconsTemp.getString(1))) {
                            ContentValues cv = new ContentValues();
                            cv.put("utiliza_beacon", 0);
                            consultaBD.Actualizar("beacon", cv, beaconsTemp.getInt(0));
                        }
                    }
                }

                for (int i = 0; i < uuidSeleccionados.size(); i++) {
                    float[] temp = beacons.get(uuidSeleccionados.get(i));
                    System.out.println("UUID: " + uuidSeleccionados.get(i) + " ID: " + idBeaconsInsertar.get(i) + " POSX: " + temp[0] + " POSY: " + temp[1]);
                    consultaBD.OperacionMapaBeacon(0, idImagen, idBeaconsInsertar.get(i), (int) temp[0], (int) temp[1], true);
                }
                Toast.makeText(this, "Actualizado", Toast.LENGTH_LONG).show();
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
            //sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor typeRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (typeRotationVector != null) {
            sensorManager.registerListener(this, typeRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.f
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


    public void CalibrarEditar(View view) {
        anguloGridFrente = angulo;
    }

    public void MostrarBeaconsActuales(View view) {
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

    public void MostrarBeaconsEditar(View view) {
        final ArrayList<Integer> numBeacos = new ArrayList<>();
        //cursorListaBeacons = consultaBD.retornarConsulta(0, "beacon", true);
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
