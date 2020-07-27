package com.example.conde_gabriel_tesis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;

public class BluetoothLE {
    private Context ctx;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private ScanCallback leScanCallback;
    private Handler handler;
    private int tiempoScanBLE = 20000;
    private BluetoothLeScanner bluetoothLeScanner;
    private HashMap<String, BluetoothDevice> dispositvosBluetooth = new HashMap<>();
    private HashMap<Beacon, Integer> beacons = new HashMap<>();
    private List<String> uuids = new ArrayList<>();

    private String[] beaconsLayout = {"m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24",//Encabezado iBeacon
                                    BeaconParser.EDDYSTONE_URL_LAYOUT,
                                    BeaconParser.EDDYSTONE_UID_LAYOUT,
                                    BeaconParser.EDDYSTONE_TLM_LAYOUT,
                                    BeaconParser.ALTBEACON_LAYOUT,
                                    BeaconParser.URI_BEACON_LAYOUT};

    private List<BeaconParser> beaconParserList = new ArrayList<>();
    private String uuidCercano = "";
    private static double nTrilateracion;
    private Trilateracion trilateracion = new Trilateracion();
    private final double[] lecturas = new double[2];

    public BluetoothLE(final Context context) {
        ctx = context;
        handler = new Handler();
        bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ctx.startActivity(enableBtIntent);

        }
        for (String bp : beaconsLayout) {
            beaconParserList.add(new BeaconParser().setBeaconLayout(bp));
        }
    }


    public void EscanearDispositivosBLE() {
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                for (BeaconParser parametro : beaconParserList) {
                    Beacon beacon = parametro.fromScanData(result.getScanRecord().getBytes(), result.getRssi(), result.getDevice(), 10);
                    if (beacon != null) {
                        if (!dispositvosBluetooth.containsKey(beacon.getId1().toString())) {
                            dispositvosBluetooth.put(beacon.getId1().toString(), result.getDevice());
                            System.out.println("UUID: " + beacon.getId1().toString());
                            System.out.println("MAC: " + result.getDevice().getAddress());
                            String msg = "Datos Enviados = ";
                            for (byte b : result.getScanRecord().getBytes())
                                msg += String.format("%02x ", b);
                            System.out.println(msg);
                            break;
                        }
                    }
                }
            }
        };
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("TERMINE");
                bluetoothLeScanner.stopScan(leScanCallback);
                Handler handler = new Handler(ctx.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MostrarDispositivosBLE();
                    }
                }, 600);
            }
        }, tiempoScanBLE);
        bluetoothLeScanner.startScan(leScanCallback);
    }

    public void EscanerDispositivoBLECercano() {
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        final List<Integer> prueba = new ArrayList<>();
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                prueba.add(1);
                for (BeaconParser parametro : beaconParserList) {
                    //BeaconParser beaconParser1=beaconParser.setBeaconLayout(parametro);
                    Beacon beacon = parametro.fromScanData(result.getScanRecord().getBytes(), result.getRssi(), result.getDevice(), 10);
                    if (beacon != null) {
                        //System.out.println("Hola");
                        //beacon.getTxPower()-result.getRssi())/(10f*2f)
                        //double distancia=10*Math.log10((10*beacon.getTxPower())/(result.getRssi()));
                        double distancia = Math.pow(10, (-result.getRssi() + beacon.getTxPower()) / (10f * 4f));
                        //System.out.println("POWER: "+beacon.getTxPower());
                        System.out.println("UUID: " + beacon.getId1().toString() + " RSSI: " + result.getRssi() + " DIST: " + Double.parseDouble(Double.toString(distancia)));

                        if (!beacons.containsKey(beacon)) {
                            beacons.put(beacon, result.getRssi());
                        } else {
                            int rssi = result.getRssi();
                            if (rssi > beacons.get(beacon)) {
                                //System.out.println("Actualizo: " + rssi);
                                beacons.put(beacon, rssi);

                            }
                            //
                        }
                    }
                }
            }
        };
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(leScanCallback);
                System.out.println("TERMINE: " + prueba.size() + " - " + beacons.size());
                if(!beacons.isEmpty()){
                    Map.Entry<Beacon, Integer> maxMap = beacons.entrySet().iterator().next();
                    for (Map.Entry<Beacon, Integer> tempMap : beacons.entrySet()) {
                        if (maxMap.getValue() < tempMap.getValue()) {
                            maxMap = tempMap;
                        }
                    }
                    uuidCercano = maxMap.getKey().getId1().toString();
                }

                //beacons.entrySet().iterator().next();
                //System.out.println("UUID: " + );
                //System.out.println("RSSI: " + maxMap.getValue());


                Handler handler = new Handler(ctx.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //       System.out.println("HAGO ESTO");
                        MostrarDispositivosBLE();
                    }
                }, 600);
            }
        }, tiempoScanBLE);

        bluetoothLeScanner.startScan(leScanCallback);
    }

    public void EscanearDispositivosBLECalibracion(final List<String> uuids, final double distancia) {
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        final List<Integer> comprobar = new ArrayList<>();
        final double[] listaExponente = new double[uuids.size()];
        final double[] contadorTotal = new double[uuids.size()];
        final double[] contador2 = new double[uuids.size()];
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                for (BeaconParser parametro : beaconParserList) {
                    boolean terminar = false;
                    Beacon beacon = parametro.fromScanData(result.getScanRecord().getBytes(), result.getRssi(), result.getDevice(), 10);
                    if (beacon != null) {
                        for (int i = 0; i < uuids.size(); i++) {
                            //System.out.println("Entro: "+beacon.getId1().toString()+" otro: "+uuids.get(i));
                            if (contador2[i] < 20) {
                                if (beacon.getId1().toString().equals(uuids.get(i))) {
                                    contador2[i]++;
                                    listaExponente[i] += ObtenerExponenteN(beacon.getTxPower(), result.getRssi(), distancia);
                                    System.out.println("i: " + i + " - " + beacon.getId1().toString() + " -> " + contador2[i]);
                                    break;
                                }
                            } else {
                                contadorTotal[i] = 1;
                            }
                            int contador = 0;
                            for (int k = 0; k < contadorTotal.length; k++) {
                                if (contadorTotal[k] == 1) {
                                    contador++;
                                }
                            }
                            if (contador == contadorTotal.length) {
                                bluetoothLeScanner.stopScan(leScanCallback);
                                System.out.println("DETUVE");
                                //System.out.println(contador2[0]);
                                //System.out.println(contador2[1]);
                                double cont = 0;
                                for (int j = 0; j < listaExponente.length; j++) {
                                    System.out.println("SUMA : " + j + " -> " + listaExponente[j] / contador2[j]);
                                    cont += listaExponente[j] / contador2[j];
                                }
                                System.out.println("T: " + cont / 2);
                                setnTrilateracion(cont / listaExponente.length);
                                terminar = true;
                                break;
                            }
                        }
                    }
                    if (terminar) break;
                }
            }
        };
        bluetoothLeScanner.startScan(leScanCallback);
    }


    public void EscanearDispositivosBLETrilateracion(final List<String> uuids, final List<double[]> posiciones, final Imagen imagen, final double sizePixX, final double sizeCMX, final double sizePixY, final double sizeCMY) {
        final double[] rssi = new double[uuids.size()];
        final double[] txpower = new double[uuids.size()];
        final List<Integer> contadorLecturas = new ArrayList<>();

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                for (BeaconParser parametro : beaconParserList) {
                    Beacon beacon = parametro.fromScanData(result.getScanRecord().getBytes(), result.getRssi(), result.getDevice(), 10);
                    if (beacon != null) {
                        for (int i = 0; i < uuids.size(); i++) {
                            if (beacon.getId1().toString().equals(uuids.get(i))) {
                                rssi[i] = result.getRssi();
                                txpower[i] = beacon.getTxPower();
                                double[] solucion= trilateracion.TrilateracionSolve(posiciones, rssi, txpower, getnTrilateracion());
                                contadorLecturas.add(1);
                               // System.out.println("TOTAL DE LECTURAS: "+contadorLecturas.size());
                                lecturas[0]=solucion[0];
                                lecturas[1]=solucion[1];
                                imagen.DibujarPunto((float) TransformarPX(lecturas[0], sizePixX, sizeCMX), (float) TransformarPX(lecturas[1], sizePixY, sizeCMY));
                                //double[] solucion = trilateracion.TrilateracionSolve2(posiciones, rssi, txpower);
                                // imagen.DibujarPunto((float)solucion[0],(float) solucion[1]);
                                //imagen.DibujarPunto((float) TransformarPX(solucion[0], sizePixX, sizeCMX), (float) TransformarPX(solucion[1], sizePixY, sizeCMY));

                                //setPos(solucion);
                                //System.out.println("X: " + solucion[0] + " Y: " + solucion[1]);
                                break;
                            }
                        }
                    }
                }
            }
        };
        bluetoothLeScanner.startScan(leScanCallback);
        /*
        hiloLectura = new Thread(){
            @Override
            public void run() {
                super.run();
                try{
                    imagen.DibujarPunto((float) TransformarPX(lecturas[0], sizePixX, sizeCMX), (float) TransformarPX(lecturas[1], sizePixY, sizeCMY));
                    System.out.println("DIBUJO");
                    Thread.sleep(3000);
                }catch (Exception e){
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }

            }
        };
        hiloLectura.start();
        */
    }
    public void DetenerEscaneoTrilateracion(boolean comprobar){
        if(comprobar) bluetoothLeScanner.stopScan(leScanCallback);
    }

    private double ObtenerExponenteN(double txpower, double rssi, double distancia) {
        double respuesta;
        respuesta = (txpower - rssi) / (10 * Math.log10(distancia));
        return respuesta;
    }

    public double TransformarPX(double posicion, double sizePix, double sizeCM) {
        double resultado;
        double sizeM = sizeCM / 100;
        resultado = (posicion * sizePix) / (sizeM);
        return resultado;
    }


    public static double getnTrilateracion() {
        return nTrilateracion;
    }

    public static void setnTrilateracion(double nTrilateracion) {
        BluetoothLE.nTrilateracion = nTrilateracion;
    }

    public void MostrarDispositivosBLE() {
        uuids.clear();
        System.out.println("Hola: " + dispositvosBluetooth.size());
        for (String uuid : dispositvosBluetooth.keySet()) {
            System.out.println(uuid);
            uuids.add(uuid);
        }
    }

    public HashMap<String, BluetoothDevice> getDispositvosBluetooth() {
        return dispositvosBluetooth;
    }


    public List<String> getUuids() {
        return uuids;
    }

    public String getUuidCercano() {
        return uuidCercano;
    }
}
