package com.example.conde_gabriel_tesis;

import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;

import java.io.File;
import java.util.Locale;

public class Indicaciones {

    private float anguloNodoHijo, distanciaAngulo, volumenIzquierdo, volumenDerecho;
    private String direccionGiro, tipoGiro, advertencia;
    private TextToSpeech textToSpeech;
    private boolean esIndicacion;
    public File file;
    public String tts;

    public boolean isEsIndicacion() {
        return esIndicacion;
    }

    public float getAnguloNodoHijo() {
        return anguloNodoHijo;
    }

    public Indicaciones(Context context) {
        try {
            file = new File(context.getFilesDir(), "indicacion.wav");
            file.createNewFile();
        } catch (Exception e) {
            System.out.println(e);
        }
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == textToSpeech.SUCCESS) {
                    int lenguaje = textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.setSpeechRate(1.5f);
                }
            }
        });
    }

    public File ObtenerInstruccion(Nodo nodoPadre, Nodo nodoHijo, float anguloGrid, float anguloBrujula, boolean esFinal, int color) {
        if (esFinal) {
            tts = "HA LLEGADO A SU DESTINO";
            GenerarArchivoTTS(tts);
            esIndicacion = true;
            return file;
        }
        anguloNodoHijo = ObtenerAnguloNodoHijo(nodoPadre, nodoHijo, anguloGrid);//Angulo obtenido en base a la grid y el angulo calibrado
        distanciaAngulo = ObtenerDistanciaAngulo(anguloBrujula, anguloNodoHijo);//Distancia entre el angulo de mira y el objetivo
        direccionGiro = ObtenerDireccionGiro(anguloNodoHijo, anguloBrujula);//Direccion de Giro
        tipoGiro = ObtenerTipoGiro(distanciaAngulo);//Tipo de giro que se debe realizar
        advertencia = DeteccionTipoNodo(color);
        switch (tipoGiro) {
            case "NO GIRO":
                if (!advertencia.equals("")) tts = advertencia;
                else tts = " AVANCE UN PASO";
                esIndicacion = true;
                break;
            case "LEVE":
                tts = "LEVE";
                esIndicacion = false;
                break;
            case "RECTO":
                tts = "GIRE " + direccionGiro;
                esIndicacion = true;
                break;
            case "FUERTE":
                tts = "FUERTE";
                esIndicacion = false;
                break;
            case "MEDIO GIRO":
                tts = "MEDIO GIRO POR " + direccionGiro;
                esIndicacion = true;
                break;
            default:
                tts = "Error";
                break;
        }
        if (esIndicacion) GenerarArchivoTTS(tts);
        return file;
    }

    private float ObtenerAnguloNodoHijo(Nodo nodoPadre, Nodo nodoHijo, float anguloGrid) {
        //Determina el ángulo en el cual se encuentra el siguiente nodo
        //Nodo de frente == Norte 0°
        float giro = 0f;
        int xPadre, yPadre, xHijo, yHijo;
        xPadre = nodoPadre.getCoordenadaX();
        yPadre = nodoPadre.getCoordenadaY();
        xHijo = nodoHijo.getCoordenadaX();
        yHijo = nodoHijo.getCoordenadaY();
        if (xPadre == xHijo && yPadre > yHijo) giro = 0; //Frente
        if (xPadre < xHijo && yPadre > yHijo) giro = 45; //Diagonal Superior Derecha
        if (xPadre < xHijo && yPadre == yHijo) giro = 90; //Derecha
        if (xPadre < xHijo && yPadre < yHijo) giro = 135; //Diagonal Inferior Derecha
        if (xPadre == xHijo && yPadre < yHijo) giro = 180; //Atras
        if (xPadre > xHijo && yPadre < yHijo) giro = 225; //Diagonal Inferior Izquierda
        if (xPadre > xHijo && yPadre == yHijo) giro = 270; //Izquierda
        if (xPadre > xHijo && yPadre > yHijo) giro = 315; //Diagonal Superior Izquierda
        giro += anguloGrid;
        if (giro > 360) giro -= 360;
        System.out.println("GIRO: " + giro);
        return giro;
    }
    //distancia = Math.abs(anguloBrujula - anguloHijo) % 360;
    //if (anguloResta < 0) anguloResta += 360; //Si es menor a 0 se suma 360


    private float ObtenerDistanciaAngulo(float anguloBrujula, float anguloHijo) {
        //Determina la distancia entre la direccion de mira del usuario y el
        //siguiente nodo, resultado se encuentra entre 0-180 grados
        float distancia;
        distancia = Math.abs(anguloBrujula - anguloHijo);
        if (distancia > 180) distancia = 360 - distancia;
        return distancia;
    }

    private String ObtenerDireccionGiro(float anguloHijo, float anguloBrujula) {
        //Determina la direccion del giro Izquierda o Derecha.
        String direccion;
        float anguloResta = anguloHijo - anguloBrujula;
        if (anguloResta < 0) anguloResta += 360;
        if (anguloResta > 180)
            direccion = "IZQUIERDA"; //Mayor a 180 el giro corto es por la izquierda
        else direccion = "DERECHA"; // sino por la derecha

        return direccion;
    }

    private String ObtenerTipoGiro(float distancia) {
        String tipoGiro = "";
        if (distancia <= 10) tipoGiro = "NO GIRO";
        if (distancia > 10 && distancia < 85) tipoGiro = "LEVE";
        if (distancia >= 85 && distancia <= 95) tipoGiro = "RECTO";
        if (distancia > 95 && distancia < 170) tipoGiro = "FUERTE";
        if (distancia >= 170 && distancia <= 180) tipoGiro = "MEDIO GIRO";
        return tipoGiro;
    }

    private void GenerarArchivoTTS(String instruccionTTS) {
        int cod = textToSpeech.synthesizeToFile(instruccionTTS, null, file, "1");
    }

    public float[] GenerarSonidoStereo() {
        float[] volumenes = new float[2];
        if(tipoGiro.equals("NO GIRO")){
            volumenDerecho = 1.0f;
            volumenIzquierdo = 1.0f;
        }else{
            if (direccionGiro.equals("IZQUIERDA")) {
                volumenDerecho = 0f;
                volumenIzquierdo = 1.0f;
            } else if (direccionGiro.equals("DERECHA")) {
                volumenDerecho = 1.0f;
                volumenIzquierdo = 0f;
            }
        }

        volumenes[0] = volumenIzquierdo;
        volumenes[1] = volumenDerecho;
        return volumenes;
    }

    private String DeteccionTipoNodo(int color) {
        String indicacion = "";
        if (color == Color.GREEN) indicacion = "CUIDADO, TIENES UNA PUERTA A UN PASO,";
        if (color == Color.YELLOW) indicacion = "CUIDADO, TIENES UNA ESCALERA A UN PASO,";
        if (color == Color.CYAN) indicacion = "CUIDADO, TIENES UN ELEVADOR A UN PASO,";
        if (color == Color.MAGENTA) indicacion = "CUIDADO, PUNTO DE INTERÉS A UN PASO,";
        return indicacion;
    }

    public void cerrarTTS() {
        textToSpeech.shutdown();
    }


}
