package com.alexkornshell.model;

public class CarConfig {
    int carW;
    int carH;
    double width;
    double height;
    double carV;
    Lane laneF;
    Lane laneT;

    CarConfig(int carW, int carH, double carV, float scale, Lane laneF) {
        this.carW = carW;
        this.carH = carH;
        this.carV = carV;
        this.width = (double) carW / scale;
        this.height = (double) carH / scale;
        this.laneF = laneF;
    }
}
