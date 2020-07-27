package com.example.conde_gabriel_tesis;

import java.util.List;

public class Trilateracion {

    private double constanteNDistancia = 4;
            //3.908957985902512; //Valor recomendado entre 2 y 4  / 4-6
    private double constanteAlfarssi = 0.75; //Valor alfa filtro rssi
    private static double[] rssi;
    private int numeroCoordenada = 2;

    private double Distancia(double rssi, double txPower) {
        double distancia;
        distancia = Math.pow(10, (-rssi + txPower) / (10 * constanteNDistancia));
        return distancia;
    }

    public double[] TrilateracionSolve(List<double[]> posiciones, double[] rssiEntrada, double[] txpower, double n) {
        System.out.println(" POS 0: "+posiciones.get(0)[0]+ " - "+posiciones.get(0)[1]);
        System.out.println(" POS 1: "+posiciones.get(1)[0]+ " - "+posiciones.get(1)[1]);
        System.out.println(" POS 2: "+posiciones.get(2)[0]+ " - "+posiciones.get(2)[1]);
        System.out.println("R0: "+rssiEntrada[0]+" R1: "+rssiEntrada[1]+" R2: "+rssiEntrada[2]);
        System.out.println("T0: "+txpower[0]+" T1: "+txpower[1]+" T2: "+txpower[2]);
        System.out.println("N: "+constanteNDistancia);

        //Ubicacion a devolver
        double[] ubicacionDeterminada = new double[2];

        //Variables Temporales
        double[] posicionTemp;
        double rssiTemp;
        double txpowerTemp;
        double distancia;
        double tempNumeradorX;
        double tempNumeradorY;

        //Elementos Formula final (SUMATORIAS)
        double numeradorX = 0;
        double numeradorY = 0;
        double denominador = 0;

        //rssi Filtro
        rssi = FiltroRSSI(rssiEntrada, rssi);

        double[] vectorDistancias = new double[posiciones.size()];
        for (int i = 0; i < vectorDistancias.length; i++) {
            vectorDistancias[i] = Distancia(rssi[i], txpower[i]);
            //System.out.println("DIST: "+i+" = "+vectorDistancias[i]);
        }

        for (int i = 0; i < posiciones.size(); i++) {
            posicionTemp = posiciones.get(i);//posicion beacon
            rssiTemp = rssi[i];//rssi del beacon
            txpowerTemp = txpower[i];//txPower del beacon
            distancia = Distancia(rssiTemp, txpowerTemp);//distancia

            //Numeradores
            tempNumeradorX = (posicionTemp[0]) / (distancia);//X
            tempNumeradorY = (posicionTemp[1]) / (distancia);//Y

            numeradorX += tempNumeradorX;//X Sumatoria
            numeradorY += tempNumeradorY;//Y Sumatoria

            //Denominadores
            denominador += (1 / (distancia));//Sumatoria (IGUAL PARA X y Y)
        }

        ubicacionDeterminada[0] = numeradorX / denominador; //Posicion estimada X
        ubicacionDeterminada[1] = numeradorY / denominador; //Posicion estimada Y

        return ubicacionDeterminada;
    }

    //constanteNDistancia=n;
        /*
        System.out.println(" POS 1: "+posiciones.get(0)[0]+ " - "+posiciones.get(0)[1]);
        System.out.println(" POS 2: "+posiciones.get(1)[0]+ " - "+posiciones.get(1)[1]);
        System.out.println(" POS 3: "+posiciones.get(2)[0]+ " - "+posiciones.get(2)[1]);
//        System.out.println(" POS 4: "+posiciones.get(3)[0]+ " - "+posiciones.get(3)[1]);
        System.out.println("R1: "+rssiEntrada[0]+" R2: "+rssiEntrada[1]+" R3: "+rssiEntrada[2]);
        //+" R4: "+rssiEntrada[3] +" T4: "+txpower[2]
        System.out.println("T1: "+txpower[0]+" T2: "+txpower[1]+" T3: "+txpower[2]);
        System.out.println("N: "+constanteNDistancia);
*/

    public double[] TrilateracionSolve2(List<double[]> posiciones, double[] rssiEntrada, double[] txpower) {
        System.out.println(" POS 1: "+posiciones.get(0)[0]+ " - "+posiciones.get(0)[1]);
        System.out.println(" POS 2: "+posiciones.get(1)[0]+ " - "+posiciones.get(1)[1]);
        System.out.println(" POS 3: "+posiciones.get(2)[0]+ " - "+posiciones.get(2)[1]);
        System.out.println("R1: "+rssiEntrada[0]+" R2: "+rssiEntrada[1]+" R3: "+rssiEntrada[2]);
        System.out.println("T1: "+txpower[0]+" T2: "+txpower[1]+" T3: "+txpower[2]);
        //Vector x (Solucion) LSQ
        double[] ubicacionDeterminada = new double[2];
        int numF = posiciones.size() - 1;
        int totalPosiciones= posiciones.size();
        //Matriz A
        double[][] matrizA = new double[numF][numeroCoordenada];
        //Vector Distancias
        double[] vectorDistancias = new double[posiciones.size()];
        //Vector B
        double[][] matrizB = new double[numF][1];
        //Variables
        double[] poscionesTemp;
        double[] posicionesUltimo = posiciones.get(posiciones.size() - 1);
        rssi = FiltroRSSI(rssiEntrada, rssi);
        //Matriz A Llenar
        for (int i = 0; i < posiciones.size() - 1; i++) {
            poscionesTemp = posiciones.get(i);
            matrizA[i][0] = 2 * (poscionesTemp[0] - posicionesUltimo[0]);
            matrizA[i][1] = 2 * (poscionesTemp[1] - posicionesUltimo[1]);
        }
        //Vector Distancias Llenar
        for (int i = 0; i < vectorDistancias.length; i++) {
            vectorDistancias[i] = Distancia(rssi[i], txpower[i]);
            System.out.println("DIST: "+i+" = "+vectorDistancias[i]);
        }
        //Matriz B Llenar
        //for (int i = 0; i < matrizB.length; i++) { }
        matrizB[0][0] = Math.pow(posiciones.get(0)[0], 2) - Math.pow(posicionesUltimo[0], 2) +
                        Math.pow(posiciones.get(0)[1], 2) - Math.pow(posicionesUltimo[1], 2)+
                        Math.pow(vectorDistancias[vectorDistancias.length - 1], 2) - Math.pow(vectorDistancias[0], 2);

        matrizB[1][0] = Math.pow(posiciones.get(totalPosiciones-2)[0], 2) - Math.pow(posicionesUltimo[0], 2) +
                        Math.pow(posiciones.get(totalPosiciones-2)[1], 2) - Math.pow(posicionesUltimo[1], 2)+
                        Math.pow(vectorDistancias[vectorDistancias.length - 2], 2) - Math.pow(vectorDistancias[0], 2);
        //Matriz Traspuesta A
        double[][] matrizAT = new double[numeroCoordenada][numF];
        for (int i = 0; i < matrizA.length; i++) {
            for (int j = 0; j < matrizA[i].length; j++) {
                matrizAT[j][i] = matrizA[i][j];
            }
        }
        //Multiplicacion X=(At*A)
        double[][] matrizX = new double[matrizAT.length][matrizA[0].length];
        for (int i = 0; i < matrizX.length; i++) {
            for (int j = 0; j < matrizX[0].length; j++) {
                for (int k = 0; k < matrizX[0].length; k++) {
                    matrizX[i][j] += matrizAT[i][k] * matrizA[k][j];
                }
            }
        }
        //Matriz Adjunta X
        double[][] matrizXadjunta = new double[matrizX.length][matrizX[0].length];
        matrizXadjunta[0][0] = matrizX[1][1];
        matrizXadjunta[0][1] = -matrizX[1][0];
        matrizXadjunta[1][0] = -matrizX[0][1];
        matrizXadjunta[1][1] = matrizX[0][0];

        //matriz Traspuesta adjX
        double[][] matrizXadjTras = new double[matrizX.length][matrizX[0].length];
        for (int i = 0; i < matrizXadjTras.length; i++) {
            for (int j = 0; j < matrizXadjTras[0].length; j++) {
                matrizXadjTras[j][i] = matrizXadjunta[i][j];
            }
        }
        double determinante = DeterminanteMatriz(matrizX);
        //Matriz Inversa  X-1=adj(Xt)/|X|
        double[][] matrizXinversa = new double[matrizX.length][matrizX[0].length];
        for (int i = 0; i < matrizXinversa.length; i++) {
            for (int j = 0; j < matrizXinversa[0].length; j++) {
                matrizXinversa[i][j] = matrizXadjTras[i][j] / determinante;
            }
        }

        //Multiplicacion C=At*b
        double[][] matrizC = new double[matrizAT.length][matrizB[0].length];
        /*
        for (int i = 0; i < matrizC.length; i++) {
            for (int j = 0; j < matrizC[0].length; j++) {
                for (int k = 0; k < matrizC[0].length; k++) {
                    matrizC[i][j] += matrizAT[i][k] * matrizB[k][j];
                }
            }
        }*/
        matrizC[0][0]=(matrizAT[0][0]*matrizB[0][0])+(matrizAT[0][1]*matrizB[1][0]);
        matrizC[1][0]=(matrizAT[1][0]*matrizB[0][0])+(matrizAT[1][1]*matrizB[1][0]);
        //Multiplicacion respuesta R=X-1 * C
        double[][] matrizR = new double[matrizXinversa.length][matrizC[0].length];
        /*
        for (int i = 0; i < matrizR.length; i++) {
            for (int j = 0; j < matrizR[0].length; j++) {
                for (int k = 0; k < matrizR[0].length; k++) {
                    matrizR[i][j] += matrizXinversa[i][k] * matrizC[k][j];
                }
            }
        }*/
        matrizR[0][0]=(matrizXinversa[0][0]*matrizC[0][0])+(matrizXinversa[0][1]*matrizC[1][0]);
        matrizR[1][0]=(matrizXinversa[1][0]*matrizC[0][0])+(matrizXinversa[1][1]*matrizC[1][0]);
        ubicacionDeterminada[0]=matrizR[0][0];
        ubicacionDeterminada[1]=matrizR[1][0];
        return ubicacionDeterminada;
    }

    private double DeterminanteMatriz(double[][] matriz) {
        double determinante;
        double tempDiagPrincipal = 1;
        double temDiagSecundaria = 1;
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                if (i == j) {
                    tempDiagPrincipal *= matriz[i][j];
                } else {
                    temDiagSecundaria *= matriz[i][j];
                }
            }
        }
        determinante = tempDiagPrincipal - temDiagSecundaria;
        return determinante;
    }

    private double[] FiltroRSSI(double[] valoresActuales, double[] salida) {
        if (salida == null) {
            return valoresActuales;
        }
        // salida[i]=constanteAlfarssi * valorActual +(1 - constanteAlfarssi) * salida[i];

        for (int i = 0; i < valoresActuales.length; i++) {
            salida[i] = constanteAlfarssi * valoresActuales[i] + (1 - constanteAlfarssi) * salida[i];
        }

        return salida;
    }

}
