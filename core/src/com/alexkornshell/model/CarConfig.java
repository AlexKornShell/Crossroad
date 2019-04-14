package com.alexkornshell.model;

public class CarConfig {
    public int carW;
    public int carH;
    public double width;
    public double height;
    public double carV;
    public Lane laneF;
    public Lane laneT;

    public CarConfig(int carW, int carH, double carV, int scale, Lane laneF) {
        this.carW = carW;
        this.carH = carH;
        this.carV = carV;
        this.width = (double) carW / scale; // Было * 6 / screen
        this.height = (double) carH / scale;
        this.laneF = laneF;
    }
}
