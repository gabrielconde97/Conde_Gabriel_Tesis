package com.example.conde_gabriel_tesis;

public class Nodo {
    private int coordenadaX;
    private int coordenadaY;
    private double h;
    private double g;
    private double f;
    private String nombreNodo;
    private Nodo nodo;

    public int getCoordenadaX() {
        return coordenadaX;
    }

    public void setCoordenadaX(int coordenadaX) {
        this.coordenadaX = coordenadaX;
    }

    public int getCoordenadaY() {
        return coordenadaY;
    }

    public void setCoordenadaY(int coordenadaY) {
        this.coordenadaY = coordenadaY;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public String getNombreNodo() {
        return nombreNodo;
    }

    public void setNombreNodo(String nombreNodo) {
        this.nombreNodo = nombreNodo;
    }

    public Nodo getNodo() {
        return nodo;
    }

    public void setNodo(Nodo nodo) {
        this.nodo = nodo;
    }
}
