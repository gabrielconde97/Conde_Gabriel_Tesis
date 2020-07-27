package com.example.conde_gabriel_tesis;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Plano extends View {
    private Paint paintMarco, paintCuadro, paintGrid, paintBorrador, paintBeacon;
    private Canvas canvasbm;
    private Path path, pathCuadro, pathGrid, pathBorrador, pathBeacon;
    private int divisionGrid = 50, divisionGridAncho, divisionGridAlto, anchoCanvasResize = 0, altoCanvasResize = 0;
    private float posx, posy;
    private Bitmap bm;
    private boolean esEditar, esBeacon;
    private MapaControl mapaControl = new MapaControl();
    private EditarMapa editarMapa = new EditarMapa();

    public Plano(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Se declara e inicializa las variables a utilizar

        paintMarco = new Paint();
        paintCuadro = new Paint();
        paintGrid = new Paint();
        paintBorrador = new Paint();
        paintBeacon = new Paint();
        canvasbm = new Canvas();
        path = new Path();
        pathCuadro = new Path();
        pathGrid = new Path();
        pathBorrador = new Path();
        pathBeacon = new Path();

        //PaintMarco
        paintMarco.setStyle(Paint.Style.STROKE);
        paintMarco.setStrokeWidth(5);
        paintMarco.setColor(Color.BLACK);

        //Paint Cuadro
        paintCuadro.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCuadro.setStrokeWidth(5);
        paintCuadro.setColor(Color.BLACK);

        //Paint Grid
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(1);
        paintGrid.setColor(Color.GRAY);

        //Paint Borrador
        paintBorrador.setStyle(Paint.Style.FILL_AND_STROKE);
        paintBorrador.setStrokeWidth(5);
        paintBorrador.setColor(Color.WHITE);

        //Paint Beacon
        paintBeacon.setStyle(Paint.Style.FILL_AND_STROKE);
        paintBeacon.setStrokeWidth(5);
        paintBeacon.setColor(getResources().getColor(R.color.AzulBeacon));
      //  paintBeacon.setColor(Color.TRANSPARENT);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bm != null) {
            canvas.drawBitmap(bm, 0, 0, null);
            canvas.drawPath(pathBeacon, paintBeacon);
        }
    }

    public void Inicializar(int ancho, int alto) {
        anchoCanvasResize = ancho;
        altoCanvasResize = alto;
        bm = Bitmap.createBitmap(anchoCanvasResize, altoCanvasResize, Bitmap.Config.ARGB_8888);
        canvasbm = new Canvas(bm);
        invalidate();
    }

    public void InicializarEditar(int ancho, int alto, int recorridoX, int recorridoY, Bitmap bitmap, List<float[]> tempList) {
        divisionGridAncho = recorridoX;
        divisionGridAlto = recorridoY;
        esEditar = true;
        anchoCanvasResize = ancho;
        altoCanvasResize = alto;
        Bitmap temp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bm = Bitmap.createBitmap(temp);
        canvasbm = new Canvas(bm);
        invalidate();
        LimpiarBeacons(tempList);
        DibujarGrid(divisionGridAncho,divisionGridAlto,anchoCanvasResize,altoCanvasResize);
    }

    public void DibujarMarco(int ancho, int alto) {
        path.reset();
        path.moveTo(0, 0);
        path.lineTo(0, alto);
        path.moveTo(0, alto);
        path.lineTo(ancho, alto);
        path.moveTo(ancho, alto);
        path.lineTo(ancho, 0);
        path.moveTo(ancho, 0);
        path.lineTo(0, 0);
        canvasbm.drawPath(path, paintMarco);
    }

    public void DibujarGrid(int pixelAncho, int pixelAlto, int ancho, int alto) {
        divisionGridAncho = pixelAncho;
        divisionGridAlto = pixelAlto;
        pathGrid.reset();
        int avancex = 0, avancey = 0;
        while (true) {
            if (avancex > ancho) {
                break;
            } else {
                pathGrid.moveTo(avancex, 0);
                pathGrid.lineTo(avancex, alto);
            }
            if (avancey > alto) {
                break;
            } else {
                pathGrid.moveTo(0, avancey);
                pathGrid.lineTo(ancho, avancey);
            }
            avancex = avancex + divisionGridAncho;
        }
        while (true) {
            if (avancey > alto) {
                break;
            } else {
                pathGrid.moveTo(0, avancey);
                pathGrid.lineTo(ancho, avancey);
            }
            avancey = avancey + divisionGridAlto;
        }
        canvasbm.drawPath(pathGrid, paintGrid);
        DibujarMarco(ancho, alto);
        invalidate();
    }

    public void DibujarObstaculo(float posx, float posy, int color) {
        double x, y, posinicialx, posinicialy, posfinalx, posfinaly;
        RectF rect = new RectF();
        x = posx / divisionGridAncho;
        y = posy / divisionGridAlto;
        x = Math.round(x);
        y = Math.round(y);
        posinicialx = ((x * divisionGridAncho) - divisionGridAncho);
        posinicialy = ((y * divisionGridAlto) - divisionGridAlto);
        posfinalx = (x * divisionGridAncho);
        posfinaly = (y * divisionGridAlto);
        rect.set((int) posinicialx + 2, (int) posinicialy + 2, (int) posfinalx - 2, (int) posfinaly - 2);
        if (color != Color.WHITE && color != Color.GRAY) {
            pathBorrador.reset();
            pathBorrador.addRect(rect, Path.Direction.CW);
            canvasbm.drawPath(pathBorrador, paintBorrador);
        }
        if (color == Color.WHITE) {
            pathCuadro.reset();
            pathCuadro.addRect(rect, Path.Direction.CW);
            canvasbm.drawPath(pathCuadro, paintCuadro);
        }
        if (color == Color.GRAY) {
            System.out.println("PULSE GRID");
        }
        canvasbm.drawPath(pathGrid, paintGrid);
        System.out.println("An: " + anchoCanvasResize);
        System.out.println("Al: " + altoCanvasResize);
        DibujarGrid(divisionGridAncho, divisionGridAlto, anchoCanvasResize, altoCanvasResize);
        invalidate();
    }

    public void DibujarPunto() {
        double x, y, posinicialx, posinicialy;
        pathBeacon.reset();
        HashMap<String,float[]> temp;
        if(!esEditar) temp=mapaControl.getBeacons();
        else temp=editarMapa.getBeacons();
        for (Map.Entry<String, float[]> entrada : temp.entrySet()) {
            x = entrada.getValue()[0] / divisionGridAncho;
            y = entrada.getValue()[1] / divisionGridAlto;
            x = Math.round(x);
            y = Math.round(y);
            posinicialx = ((x * divisionGridAncho) - divisionGridAncho);
            posinicialy = ((y * divisionGridAlto) - divisionGridAlto);
            pathBeacon.moveTo((float) posinicialx + 12, (float) posinicialy + 12);
            pathBeacon.addCircle((float) posinicialx + 12, (float) posinicialy + 12, 7, Path.Direction.CW);
        }
        invalidate();
    }

    public void LimpiarBeacons(List<float[]> puntos) {
        double x, y, posinicialx, posinicialy, posfinalx, posfinaly;
        //System.out.println("HOLA ENTRO: "+puntos.isEmpty());
        RectF rect = new RectF();
        for (float[] temp : puntos) {
            //System.out.println("HOLA ENTRO");
            x = temp[0] / divisionGridAncho;
            y = temp[1] / divisionGridAlto;
            x = Math.round(x);
            y = Math.round(y);
            posinicialx = ((x * divisionGridAncho) - divisionGridAncho);
            posinicialy = ((y * divisionGridAlto) - divisionGridAlto);
            posfinalx = (x * divisionGridAncho);
            posfinaly = (y * divisionGridAlto);
            rect.set((int) posinicialx + 2, (int) posinicialy + 2, (int) posfinalx - 2, (int) posfinaly - 2);
            int color=bm.getPixel((int) rect.centerX(), (int) rect.centerY());
            paintCuadro.setColor(color);
            pathCuadro.addRect(rect, Path.Direction.CW);
        }
        canvasbm.drawPath(pathCuadro,paintCuadro);
        invalidate();
        pathCuadro.reset();
        paintCuadro.setColor(Color.BLACK);
        DibujarPunto();
    }


    public void Obstaculo(int Color) {
        paintCuadro.setColor(Color);
    }

    public void setEsBeacon(boolean esBeacon) {
        this.esBeacon = esBeacon;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN://Acción del primer dedo pulsado
                int i = event.getActionIndex();//Recupera coordenada donde ocurrio el evento
                //DibujarPunto(event.getX(i),event.getY(i));
                System.out.println("Punto de evento " + event.getX(i) + " / / " + event.getY(i));
                posx = event.getX(i);
                posy = event.getY(i);
                //Rect rect2 = new Rect();
                //rect2.set(0,0,3,4);
                //System.out.println("CENTERX"+rect2.exactCenterX());
                //System.out.println("CENTERY"+rect2.exactCenterY());
                //Equivalencia recorrido diagonal promedio de las dos equivaencias
                Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                Canvas cv = new Canvas(bitmap);
                Plano myplano;
                if (!esEditar) myplano = findViewById(R.id.plano);
                else myplano = findViewById(R.id.planoEditar);
                myplano.draw(cv);
                if (esBeacon) {
                    //MapaControl mapaControl = new MapaControl();
                    // pathBeacon.reset();
                    if(!esEditar) mapaControl.ActualizarCoordenadasBeacon(new float[]{posx, posy});
                    else editarMapa.ActualizarCoordenadasBeacon(new float[]{posx, posy});
                    //System.out.println("SELECCION: " + mapaControl.seleccionBeacon);
                    DibujarPunto();
                    //posx, posy
                } else {
                    DibujarObstaculo(event.getX(i), event.getY(i), bitmap.getPixel((int) posx, (int) posy));
                }
                break;
        }

        return true;
    }
}



