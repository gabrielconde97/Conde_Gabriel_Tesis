package com.example.conde_gabriel_tesis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class ConsultaBD {

    private BaseDatos baseDatos;
    private SQLiteDatabase bd;

    public ConsultaBD(Context context) {
        baseDatos = new BaseDatos(context);
        bd = baseDatos.getWritableDatabase();
    }

    /*
     * TRUE ---> INGRESAR
     * FALSE---> ACTUALIZAR
     * SI ES INGRESAR ID == 0
     * SINO INGRESAR EL ID DE LO QUE SE DESEA ACTUALIZAR
     * */
    public void OperacionMapa(int id, String nombreImagen, int altoCanvasResize, int anchoCanvasResize, int recorridoX, int recorridoY, float calibracionBrujula, byte[] byteImagen, boolean esInsertar) {
        String sql;
        if (esInsertar)
            sql = "insert into mapa(nombre_mapa, alto_mapa, ancho_mapa, recorridoX_mapa, recorridoY_mapa, calibracion_mapa, imagen_mapa) values(?,?,?,?,?,?,?)";
        else
            sql = "update mapa set nombre_mapa=?, alto_mapa=?, ancho_mapa=?, recorridoX_mapa=?, recorridoY_mapa=?, calibracion_mapa=?, imagen_mapa=? where _id=" + id;
        SQLiteStatement sqLiteStatement = bd.compileStatement(sql);
        sqLiteStatement.clearBindings();
        sqLiteStatement.bindString(1, nombreImagen);
        sqLiteStatement.bindDouble(2, altoCanvasResize);
        sqLiteStatement.bindDouble(3, anchoCanvasResize);
        sqLiteStatement.bindDouble(4, recorridoX);
        sqLiteStatement.bindDouble(5, recorridoY);
        sqLiteStatement.bindDouble(6, calibracionBrujula);
        sqLiteStatement.bindBlob(7, byteImagen);
        sqLiteStatement.execute();
    }

    public void OperacionBeacon(int id, String uuid, String direccionMAC,int disponilbe, boolean  esInsertar) {
        String sql;
        if (esInsertar) sql = "insert into beacon(uuid_beacon, mac_beacon, utiliza_beacon) values(?,?,?)";
        else sql = "update beacon set uuid_beacon=?, mac_beacon=? where _id=" + id;
        SQLiteStatement sqLiteStatement = bd.compileStatement(sql);
        sqLiteStatement.clearBindings();
        sqLiteStatement.bindString(1, uuid);
        sqLiteStatement.bindString(2, direccionMAC);
        sqLiteStatement.bindDouble(3, disponilbe);
        sqLiteStatement.execute();
    }

    public void OperacionMapaBeacon(int id, int id_mapa, int id_beacon, int posx, int posy, boolean esInsertar) {
        String sql;
        if (esInsertar)
            sql = "insert into mapa_beacon(id_beacon, id_mapa, posx_beacon, posy_beacon) values(?,?,?,?)";
        else
            sql = "update mapa_beacon set id_beacon=?, id_mapa=?, posx_beacon=?, posy_beacon=? where _id=" + id;
        SQLiteStatement sqLiteStatement = bd.compileStatement(sql);
        sqLiteStatement.clearBindings();
        sqLiteStatement.bindDouble(1, id_beacon);
        sqLiteStatement.bindDouble(2, id_mapa);
        sqLiteStatement.bindDouble(3, posx);
        sqLiteStatement.bindDouble(4, posy);
        sqLiteStatement.execute();
    }

    public Cursor retornarConsulta(int id, String tabla, boolean esTotal) {
        Cursor cursor = null;
        switch (tabla) {
            case "mapa":
                if (esTotal) cursor = bd.rawQuery("select * from mapa", null);
                else cursor = bd.rawQuery("select * from mapa where _id=" + id, null);
                break;
            case "beacon":
                if (esTotal) cursor = bd.rawQuery("select * from beacon", null);
                else cursor = bd.rawQuery("select * from beacon where _id=" + id, null);
                break;
            case "mapa_beacon":
                if (esTotal) cursor = bd.rawQuery("select * from mapa_beacon", null);
                else cursor = bd.rawQuery("select * from mapa_beacon where _id=" + id, null);
                break;
            default:
                System.out.println("ERROR");
        }
        //Recupera total de elementos //> cursor.getCount()
        //Mueve el cursor a una ubicaion en especifico //> cursor.moveToPosition(posicion)
        return cursor;
    }
    public void Eliminar(String tabla , int id){
        bd.delete(tabla,"_id=?",new String[]{String.valueOf(id)});
    }
    public void Actualizar(String tabla , ContentValues contentValues, int id){
        bd.update(tabla,contentValues,"_id=?",new String[]{String.valueOf(id)});
    }

    public Cursor EjecutarConsultaSQL(String sql, String[] parametros) {
        return bd.rawQuery(sql, parametros);
    }

    public byte[] CrearByteMapa(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteImagen = baos.toByteArray();
        return byteImagen;
    }

    public Bitmap RecuperarMapaByte(byte[] imagenByte) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imagenByte, 0, imagenByte.length);
        return bitmap;
    }

    public void CerrarBD() {
        bd.close();
    }
}
