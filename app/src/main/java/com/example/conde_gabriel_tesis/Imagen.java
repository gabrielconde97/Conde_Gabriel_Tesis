package com.example.conde_gabriel_tesis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Imagen extends View {
    private Matrix matrix;
    private Paint paint, paintRuta, paintUbicacion;
    private Canvas canvas;
    private Path path, path2, pathUbicacion;
    private Bitmap imagen, cursor, cursor2;
    private Indicaciones indicaciones;
    private File file;
    private Nodo nodoInicial;
    private List<Nodo> openSet, closedSet, hijosNodo, listaRuta;
    public MediaPlayer mp;
    private int destinoX, destinoY, costoDiagonal = Integer.MAX_VALUE, costoRecto = Integer.MAX_VALUE, pasox = 1, pasoy = 1, divisionGrid = 50,
            divisionGridAncho, divisionGridAlto, anchoCanvasResize = 0, altoCanvasResize = 0;
    private float posx, posy, angulo, anguloGridFrente, anguloGraficar, anguloNodoHijo;
    private float[] volumen;
    private boolean esFinal = false;//,inicioTimer = false;
    private final Handler handlerTime = new Handler();
    private Thread hiloIndicacion, hiloGiro;
    private Context ctx;
    private long tiempoActualTouch = 0;
    private BluetoothLE bluetoothLE;

    public Imagen(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Se declara e inicializa las variables a utilizar
        indicaciones = new Indicaciones(getContext());
        matrix = new Matrix();
        paint = new Paint();
        paintRuta = new Paint();
        paintUbicacion = new Paint();
        canvas = new Canvas();
        path = new Path();
        path2 = new Path();
        pathUbicacion = new Path();
        //Paint Negro
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);

        ctx = context;
        //Paint RUTA
        paintRuta.setStyle(Paint.Style.STROKE);
        paintRuta.setStrokeWidth(5);
        paintRuta.setColor(Color.BLUE);

        //Paint Ubicacion
        paintUbicacion.setStyle(Paint.Style.STROKE);
        paintUbicacion.setStrokeWidth(3);
        paintUbicacion.setColor(Color.RED);

        cursor = BitmapFactory.decodeResource(context.getResources(), R.drawable.cursor);

        volumen = new float[2];

        ctx = context;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        if (imagen != null) {
            //System.out.println("ANCHO: "+getWidth()+" ALTO: "+getHeight());
            canvas.drawBitmap(imagen, 0, 0, null);
        }
        canvas.drawPath(path, paintRuta);
        canvas.drawPath(pathUbicacion, paintUbicacion);
        if (posx != 0 && posy != 0) {
            //matrix.reset();
            matrix.setRotate(anguloGraficar, posx, posy);
            //matrix.postTranslate(posx,posy);
            cursor2 = Bitmap.createBitmap(cursor, 0, 0, cursor.getWidth(), cursor.getHeight(), matrix, true);
            canvas.drawBitmap(cursor2, posx - divisionGrid / 2, posy - divisionGrid / 2, null);
        }
    }

    public void Rotar(float anguloRotacion, float anguloGridF, float anguloGrafico) {
        //Obtiene el giro del Magneròmetro+Aceleròmetro desde UbicacionControl y los asigna a la variable global angulo
        angulo = anguloRotacion;
        anguloGridFrente = anguloGridF;
        anguloGraficar = anguloGrafico;
        invalidate();
    }

    public void RutaAlgoritmoA() {
        //Incializaciòn de Nodos
        Nodo nodoInicio, nodoFin, nodoActual, nodoIzquierda, nodoDerecha, nodoFrente, nodoAtras, nodoDigSupIzquierda, nodoDigSupDerecha, nodoDigInfIzquierda, nodoDigInfDerecha;
        //Definiciòn del nodo de Inicio
        nodoInicial = crearNodo((int) posx, (int) posy, 0, null);
        nodoInicio = crearNodo((int) posx, (int) posy, 0, nodoInicial);
        //Definiciòn del Nodo Fin
        nodoFin = crearNodo(destinoX, destinoY, 0, null);

        nodoActual = new Nodo();
        openSet = new ArrayList<>();
        closedSet = new ArrayList<>();
        // double tBreak=1+1/(getWidth()+getHeight());
        double menorF;

        //Añadimos el primer nodo a la OpenSet
        openSet.add(nodoInicio);

        //Mientras openSet tenga nodos abra un camino disponible, sino no hay camino posible
        while (!openSet.isEmpty()) {
            hijosNodo = new ArrayList<>();
            int indice = 0;
            menorF = openSet.get(0).getF();
            //Se obtienen el menor nodo Costo F(x)=G(x)+H(x)
            for (int i = 0; i < openSet.size(); i++) {
                if (menorF >= openSet.get(i).getF()) {
                    menorF = openSet.get(i).getF();
                    nodoActual = openSet.get(i);
                    indice = i;
                }
            }

            // Comprobacion del nodo final nodoActual==nodoFin  LLEGAMOS AL FINAL!!!!

            if (nodoActual.getNombreNodo().equals(nodoFin.getNombreNodo())) {
                DibujarRuta(nodoActual); //Dibujamos la RUTA
                break;
            }

            //Removemos el Nodo con menor F de la openSet y lo agregamos al closedSet

            openSet.remove(indice);
            closedSet.add(nodoActual);

            //Generamos los posibles NODOS disponibles en base al nodo actual
            //NODOS 4 DIRECCIONES

            nodoFrente = CrearNodoFrente(nodoActual.getCoordenadaX(), nodoActual.getCoordenadaY(), nodoActual);
            nodoAtras = CrearNodoAtras(nodoActual.getCoordenadaX(), nodoActual.getCoordenadaY(), nodoActual);
            nodoIzquierda = CrearNodoIzquierda(nodoActual.getCoordenadaX(), nodoActual.getCoordenadaY(), nodoActual);
            nodoDerecha = CrearNodoDerecha(nodoActual.getCoordenadaX(), nodoActual.getCoordenadaY(), nodoActual);
            if (nodoFrente.getNombreNodo() != null) hijosNodo.add(nodoFrente);
            if (nodoAtras.getNombreNodo() != null) hijosNodo.add(nodoAtras);
            if (nodoIzquierda.getNombreNodo() != null) hijosNodo.add(nodoIzquierda);
            if (nodoDerecha.getNombreNodo() != null) hijosNodo.add(nodoDerecha);

            //Si el nodo diagonal no cumple con que sus vecinos NODOS pròximos no son disponibles entonces no se crea
            //COMPROBACION DE DIAGONALES LIBRES

            if (nodoFrente.getNombreNodo() != null && nodoIzquierda.getNombreNodo() != null) {
                nodoDigSupIzquierda = CrearNodoDigSupIzquierda(nodoActual.getCoordenadaX(), nodoActual.getCoordenadaY(), nodoActual);
                if (nodoDigSupIzquierda.getNombreNodo() != null) hijosNodo.add(nodoDigSupIzquierda);
            }
            if (nodoFrente.getNombreNodo() != null && nodoDerecha.getNombreNodo() != null) {
                nodoDigSupDerecha = CrearNodoDigSupDerecha(nodoActual.getCoordenadaX(), nodoActual.getCoordenadaY(), nodoActual);
                if (nodoDigSupDerecha.getNombreNodo() != null) hijosNodo.add(nodoDigSupDerecha);
            }

            if (nodoAtras.getNombreNodo() != null && nodoIzquierda.getNombreNodo() != null) {
                nodoDigInfIzquierda = CrearNodoDigInfIzquierda(nodoActual.getCoordenadaX(), nodoActual.getCoordenadaY(), nodoActual);
                if (nodoDigInfIzquierda.getNombreNodo() != null) hijosNodo.add(nodoDigInfIzquierda);
            }
            if (nodoAtras.getNombreNodo() != null && nodoDerecha.getNombreNodo() != null) {
                nodoDigInfDerecha = CrearNodoDigInfDerecha(nodoActual.getCoordenadaX(), nodoActual.getCoordenadaY(), nodoActual);
                if (nodoDigInfDerecha.getNombreNodo() != null) hijosNodo.add(nodoDigInfDerecha);
            }

            //System.out.println("Entro foreach");
            //Comprobamos todos los hijos NODO del nodoActual
            for (Nodo n : hijosNodo) {
                boolean comprobar = false, comprobar2 = false;
                //Verifica si el Nodo Actual Se encuentra en la lista cerrada (Nodos analizados)
                for (Nodo nodoClosed : closedSet) {
                    if (nodoClosed.getNombreNodo().equals(n.getNombreNodo())) {
                        comprobar = true;
                        continue;
                    }
                }
                if (comprobar) continue;

                //Calculamos el valor de G para el hijo NODO
                double valorG = Math.sqrt(Math.pow((nodoActual.getCoordenadaX() - n.getCoordenadaX()), 2) + Math.pow((nodoActual.getCoordenadaY() - n.getCoordenadaY()), 2)) + nodoActual.getG();

                //Variables temporales
                double valorGFinal = 0;
                int contador = 0;
                int index = 0;

                /*
                 * Comprobamos si el nodoHijo se encuentra en la openSet
                 * Si  se lo encuentra guardamos sus valores en variables temporales para su posterior anàlisis con los nuevos valores.  Bandera comprobar 2==true
                 * Caso contrario, no se guarda ningùn valor Bandera comprobar 2==false
                 * */

                for (Nodo nodoOpen : openSet) {
                    if (nodoOpen.getNombreNodo().equals(n.getNombreNodo())) {
                        index = contador;
                        comprobar2 = true;
                        valorGFinal = nodoOpen.getG();
                        break;
                    } else {
                        comprobar2 = false;
                    }
                    contador++;
                }

                /*
                 * Si no se encuentra el nodo en openSet se lo agrega con sus nuevos valores: nodoPadre, G(), F()
                 * Caso contrario, se compara su valor de G con el existente en openSet
                 * Si el valor de G del hijoNodo es mayor o igual se ignora y no se hace ningùn cambio,
                 * Caso contrario si el valor de G del hijoNodo es menor se procede a eliminar el nodo existente en openSet
                 * y se agrega el mismo nodo con los nuevos valores calculados
                 * */

                if (!comprobar2) {
                    n.setNodo(nodoActual);
                    n.setG(valorG);
                    n.setF(n.getG() + n.getH());
                    openSet.add(n);
                } else if (valorG < valorGFinal) {
                    n.setNodo(nodoActual);
                    n.setG(valorG);
                    n.setF(n.getG() + n.getH());
                    openSet.remove(index);
                    openSet.add(n);
                }
            }
            //System.out.println("Slgo foreach");
        }
    }


    public void DibujarRuta(Nodo nodo) {
        if (mp != null) if (mp.isPlaying()) mp.stop();
        if (hiloIndicacion != null) {
            hiloIndicacion.interrupt();
            hiloIndicacion = null;
        }
        if (hiloGiro != null) {
            hiloGiro.interrupt();
            hiloGiro = null;
        }


        //Grafica la ruta generada por el Algoritmo A*

        path.reset();
        listaRuta = new ArrayList<>();
        Nodo temp = nodo;
        while (true) {
            if (temp.getNombreNodo().equals(temp.getNodo().getNombreNodo())) {
                listaRuta.add(temp);
                break;
            }
            listaRuta.add(temp);
            temp = temp.getNodo();
        }
        for (Nodo n : listaRuta) {
            path.moveTo(n.getCoordenadaX(), n.getCoordenadaY());
            path.lineTo(n.getNodo().getCoordenadaX(), n.getNodo().getCoordenadaY());
            canvas.drawPath(path, paintRuta);
        }
        Collections.reverse(listaRuta);

        hiloIndicacion = new Thread() {
            @Override
            public void run() {
                //super.run();
                try {
                    //int contador = 1;
                    while (!isInterrupted()) {
                        //System.out.println("HAGO Indicacion: " + contador + " STATE: " + isInterrupted());
                        //if (inicioTimer) hiloIndicacion.interrupt();
                        if (esFinal) {
                            GenerarIndicacion();
                            break;
                        }
                        if (mp == null) {
                            GenerarIndicacion();
                        } else {
                            if (!mp.isPlaying()) {
                                GenerarIndicacion();
                            }
                        }
                        //inicioTimer = false;
                        //contador++;
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    //System.out.println("EXCEPCION");
                }

            }

            @Override
            public void interrupt() {
                super.interrupt();
                //System.out.println("Me detuve!!!!!");
            }
        };
        hiloIndicacion.start();

    }

    public void GenerarIndicacion() {
        int colorNodoHijo = 0;
        Nodo nodoP = null, nodoH = null;
        if (!esFinal) {
            colorNodoHijo = imagen.getPixel(listaRuta.get(1).getCoordenadaX(), listaRuta.get(1).getCoordenadaY());
            nodoP = listaRuta.get(0);
            nodoH = listaRuta.get(1);
        }
        file = indicaciones.ObtenerInstruccion(nodoP, nodoH, anguloGridFrente, angulo, esFinal, colorNodoHijo);
        if (!esFinal) {
            volumen = indicaciones.GenerarSonidoStereo();
            anguloNodoHijo = indicaciones.getAnguloNodoHijo();
        } else {
            volumen = new float[]{1.0f, 1.0f};
            anguloNodoHijo = 0;
        }
        final boolean indicacion = indicaciones.isEsIndicacion();
        handlerTime.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println(volumen[0]+" / "+volumen[1] );
                IndicacionVoz(volumen[0], volumen[1], indicacion);
            }
        }, 500);
    }

    public void FinalizarVista() {
        indicaciones.cerrarTTS();
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
                mp.reset();
            }
        }
        if (hiloIndicacion != null) {
            hiloIndicacion.interrupt();
            hiloIndicacion = null;
        }
        if (hiloGiro != null) {
            hiloGiro.interrupt();
            hiloGiro = null;
        }
    }

    public void IndicacionVoz(float volumenIzq, float volumenDer, boolean esIndicacion) {
        try {
            if (esIndicacion) {
                //controlar=false;
                if (mp != null) if (mp.isPlaying()) mp.stop();
                mp = new MediaPlayer();
                FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                //Uri uri = Uri.parse("file://"+file.getAbsolutePath());
                mp.setDataSource(fis.getFD());
                mp.setVolume(volumenIzq, volumenDer);
                mp.prepare();
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.stop();
                        //mp.reset();
                    }
                });
                fis.close();
            } else {
                if (mp != null) if (mp.isPlaying()) mp.stop();
                if (volumenIzq > volumenDer) {
                    mp = MediaPlayer.create(getContext(), R.raw.audiopianoiz);
                } else {
                    mp = MediaPlayer.create(getContext(), R.raw.audiopianoder);
                }
                mp.setVolume(volumenIzq, volumenDer);
                mp.start();
                hiloGiro = new Thread() {
                    @Override
                    public void run() {
                        try {
                            //int contador = 1;
                            while (!isInterrupted()) {
                                //System.out.println("Vuelta: " + contador);
                                if ((Math.round(angulo) > Math.round(anguloNodoHijo - 5)) && (Math.round(angulo) < Math.round(anguloNodoHijo + 5))) {
                                    mp.stop();
                                    hiloGiro.interrupt();
                                    GenerarIndicacion();
                                    System.out.println("STOP");
                                }
                                //contador++;
                                Thread.sleep(150);
                            }
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    @Override
                    public void interrupt() {
                        super.interrupt();
                    }
                };
                hiloGiro.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            mp.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Nodo crearNodo(int CoordenadaX, int CoordenadaY, int g, Nodo nodoPadre) {
        //Crea el nodo con todos los valores no definidos

        Nodo nodo = new Nodo();
        nodo.setCoordenadaX(CoordenadaX);
        nodo.setCoordenadaY(CoordenadaY);
        nodo.setG(g);
        nodo.setH(Heuristica(CoordenadaX, CoordenadaY));
        //nodo.setF(nodo.getG() + nodo.getH());
        nodo.setF(0);
        //nodo.setF(nodo.getG() + nodo.getH());
        nodo.setNombreNodo(Integer.toString(CoordenadaX) + Integer.toString(CoordenadaY));
        nodo.setNodo(nodoPadre);
        return nodo;

    }


    public double Heuristica(int CoordenadaX, int CoordenadaY) {
        //Calculo de heuristica para el Algoritmo A*
        double valorHeuristica;
        //Distancia Manhattan
        valorHeuristica = Math.abs(CoordenadaX - destinoX) + Math.abs(CoordenadaY - destinoY);
        //Distancia Documento
        // valorHeuristica=Math.min(Math.abs(CoordenadaX - destinoX),Math.abs(CoordenadaY - destinoY))*Math.sqrt(2)+Math.max(Math.abs(CoordenadaX - destinoX),Math.abs(CoordenadaY - destinoY))-Math.min(Math.abs(CoordenadaX - destinoX),Math.abs(CoordenadaY - destinoY));
        //Distancia euclidiana
        //valorHeuristica=Math.sqrt(Math.pow((CoordenadaX-destinoX),2)+Math.pow((CoordenadaY-destinoY),2));
        return valorHeuristica;
    }


    public void DibujarImagen(Bitmap bm) {
        //Obtienen la imagen seleccionada
        imagen = bm;
    }


    public Nodo CrearNodoFrente(int x, int y, Nodo nodoPadre) {
        Nodo nodo = new Nodo();
        pasoy = y - divisionGridAlto;
        if (pasoy < getHeight() && pasoy > 0) {
            int ObstaculoFrente = imagen.getPixel(x, pasoy);
            if (!(ObstaculoFrente == Color.BLACK))
                nodo = crearNodo(x, pasoy, costoRecto, nodoPadre);
        }
        return nodo;
    }

    public Nodo CrearNodoAtras(int x, int y, Nodo nodoPadre) {
        Nodo nodo = new Nodo();
        pasoy = y + divisionGridAlto;
        if (pasoy < getHeight() && pasoy > 0) {
            int ObstaculoFrente = imagen.getPixel(x, pasoy);
            if (!(ObstaculoFrente == Color.BLACK))
                nodo = crearNodo(x, pasoy, costoRecto, nodoPadre);
        }
        return nodo;
    }


    public Nodo CrearNodoIzquierda(int x, int y, Nodo nodoPadre) {
        Nodo nodo = new Nodo();
        pasox = x - divisionGridAncho;
        if (pasox < getWidth() && pasox > 0) {
            int ObstaculoFrente = imagen.getPixel(pasox, y);
            if (!(ObstaculoFrente == Color.BLACK))
                nodo = crearNodo(pasox, y, costoRecto, nodoPadre);
        }
        return nodo;
    }


    public Nodo CrearNodoDerecha(int x, int y, Nodo nodoPadre) {
        Nodo nodo = new Nodo();
        pasox = x + divisionGridAncho;
        if (pasox < getWidth() && pasox > 0) {
            int ObstaculoFrente = imagen.getPixel(pasox, y);
            if (!(ObstaculoFrente == Color.BLACK))
                nodo = crearNodo(pasox, y, costoRecto, nodoPadre);
        }
        return nodo;
    }


    public Nodo CrearNodoDigSupIzquierda(int x, int y, Nodo nodoPadre) {
        Nodo nodo = new Nodo();
        pasox = x - divisionGridAncho;
        pasoy = y - divisionGridAlto;
        if (pasox < getWidth() && pasox > 0 && pasoy < getHeight() && pasoy > 0) {
            int ObstaculoFrente = imagen.getPixel(pasox, pasoy);
            if (!(ObstaculoFrente == Color.BLACK))
                nodo = crearNodo(pasox, pasoy, costoDiagonal, nodoPadre);
        }
        return nodo;
    }


    public Nodo CrearNodoDigSupDerecha(int x, int y, Nodo nodoPadre) {
        Nodo nodo = new Nodo();
        pasox = x + divisionGridAncho;
        pasoy = y - divisionGridAlto;
        if (pasox < getWidth() && pasox > 0 && pasoy < getHeight() && pasoy > 0) {
            int ObstaculoFrente = imagen.getPixel(pasox, pasoy);
            if (!(ObstaculoFrente == Color.BLACK))
                nodo = crearNodo(pasox, pasoy, costoDiagonal, nodoPadre);
        }
        return nodo;
    }


    public Nodo CrearNodoDigInfIzquierda(int x, int y, Nodo nodoPadre) {
        Nodo nodo = new Nodo();
        pasox = x - divisionGridAncho;
        pasoy = y + divisionGridAlto;
        if (pasox < getWidth() && pasox > 0 && pasoy < getHeight() && pasoy > 0) {
            int ObstaculoFrente = imagen.getPixel(pasox, pasoy);
            if (!(ObstaculoFrente == Color.BLACK))
                nodo = crearNodo(pasox, pasoy, costoDiagonal, nodoPadre);
        }
        return nodo;
    }


    public Nodo CrearNodoDigInfDerecha(int x, int y, Nodo nodoPadre) {
        Nodo nodo = new Nodo();
        pasox = x + divisionGridAncho;
        pasoy = y + divisionGridAlto;
        if (pasox < getWidth() && pasox > 0 && pasoy < getHeight() && pasoy > 0) {
            int ObstaculoFrente = imagen.getPixel(pasox, pasoy);
            if (!(ObstaculoFrente == Color.BLACK))
                nodo = crearNodo(pasox, pasoy, costoDiagonal, nodoPadre);
        }
        return nodo;
    }


    public void DibujarPunto(float xE, float yE) {

        double x, y;
        x = xE / divisionGridAncho;
        y = yE / divisionGridAlto;
        x = Math.round(x);
        y = Math.round(y);
        xE = (float) ((x * divisionGridAncho) - (divisionGridAncho / 2));
        yE = (float) ((y * divisionGridAlto) - (divisionGridAlto / 2));

        //Necesario si se desea conocer la ruta o total de puntos marcados por la trilateracion
        //pathUbicacion.reset();
        //pathUbicacion.moveTo(xE, yE);
        //pathUbicacion.addCircle(xE, yE, 10, Path.Direction.CW);

        canvas.drawPath(pathUbicacion, paintUbicacion);
        posx=xE;
        posy=yE;
        /*
        if(posx !=posxTemp && posy!=posyTemp){
            posxTemp=posx;
            posyTemp=posy;
            RutaAlgoritmoA();
        }
        */
    }

    private void DibujarDestino(float x, float y) {
        pathUbicacion.reset();
        pathUbicacion.moveTo(x, y);
        pathUbicacion.addCircle(x, y, 10, Path.Direction.CW);
        canvas.drawPath(pathUbicacion, paintUbicacion);
    }

    public void DefinirDivisionGridPixel(int anchoPixel, int altoPixel, int ancho, int alto, BluetoothLE bluetoothLEI) {
        divisionGridAncho = anchoPixel;
        divisionGridAlto = altoPixel;
        anchoCanvasResize = ancho;
        altoCanvasResize = alto;
        bluetoothLE=bluetoothLEI;
        //System.out.println("X: "+divisionGridAncho+" Y: "+divisionGridAlto);
    }
    public void RestaurarVista(){
        path.reset();
        pathUbicacion.reset();
        invalidate();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP://Acción del primer dedo pulsado
                bluetoothLE.DetenerEscaneoTrilateracion(true);
                if (System.currentTimeMillis() >= (tiempoActualTouch + 1000)) {
                    int i = event.getActionIndex();//Recupera coordenada donde ocurrio el evento
                    destinoX = (int) event.getX(i);
                    destinoY = (int) event.getY(i);
                    if (!(imagen.getPixel((int) Math.round(destinoX), (int) Math.round(destinoY)) == Color.BLACK)) {
                        double x1, y1;
                        x1 = (double) destinoX / divisionGridAncho;
                        y1 = (double) destinoY / divisionGridAlto;
                        x1 = Math.round(x1);
                        y1 = Math.round(y1);
                        destinoX = (int) ((x1 * divisionGridAncho) - (divisionGridAncho / 2));
                        destinoY = (int) ((y1 * divisionGridAlto) - (divisionGridAlto / 2));
                        if(posx!=0 && posy!=0){
                            if (posx == destinoX && posy == destinoY) esFinal = true;
                            else esFinal = false;
                            if (!(imagen.getPixel((int) Math.round(posx), (int) Math.round(posy)) == Color.BLACK)) {
                                RutaAlgoritmoA();
                            }
                        }
                        DibujarDestino(destinoX, destinoY);
                    }else{
                        Toast.makeText(ctx, "Seleccione una celda válida", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (destinoY != 0 && destinoX != 0) {
                        int i = event.getActionIndex();//Recupera coordenada donde ocurrio el evento
                        posx = event.getX(i);
                        posy = event.getY(i);
                        if (!(imagen.getPixel((int) Math.round(posx), (int) Math.round(posy)) == Color.BLACK)) {
                            invalidate();
                            double x, y;
                            x = posx / divisionGridAncho;
                            y = posy / divisionGridAlto;
                            x = Math.round(x);
                            y = Math.round(y);
                            posx = (float) ((x * divisionGridAncho) - (divisionGridAncho / 2));
                            posy = (float) ((y * divisionGridAlto) - (divisionGridAlto / 2));
                            if (posx == destinoX && posy == destinoY) esFinal = true;
                            else esFinal = false;
                            RutaAlgoritmoA();
                            DibujarDestino(destinoX, destinoY);
                        }else{
                            FinalizarVista();
                            //posx=0;
                            //posy=0;
                            Toast.makeText(ctx, "Seleccione una celda válida", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(ctx, "Seleccione un destino", Toast.LENGTH_LONG).show();

                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                tiempoActualTouch = System.currentTimeMillis();
                break;
        }
        return true;
    }


}
