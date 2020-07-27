package com.example.conde_gabriel_tesis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDatos extends SQLiteOpenHelper {
    private static final String nombreBD = "mapa_beacons.db";
    private static final int numeroversionDB = 1;

    public BaseDatos(Context context) {
        super(context, nombreBD, null, numeroversionDB);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String sqlMapa = "create table mapa ( _id integer primary key autoincrement, nombre_mapa text, alto_mapa integer, ancho_mapa integer, recorridoX_mapa integer, recorridoY_mapa integer, calibracion_mapa real, imagen_mapa blob);";
        database.execSQL(sqlMapa);
        /*
         * utiliza_beacon
         * 0 -> nose utiliza
         * 1 -> se utiliza
         * */
        String sqlBeacon = "create table beacon ( _id integer primary key autoincrement, uuid_beacon text, mac_beacon text, utiliza_beacon boolean );";
        database.execSQL(sqlBeacon);
        String sqlMapaBeacon = "create table mapa_beacon ( _id integer primary key autoincrement, id_beacon integer, id_mapa integer, posx_beacon integer, posy_beacon integer);";
        database.execSQL(sqlMapaBeacon);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS mapa");
        database.execSQL("DROP TABLE IF EXISTS beacon");
        database.execSQL("DROP TABLE IF EXISTS mapa_beacon");
        onCreate(database);
    }
}

