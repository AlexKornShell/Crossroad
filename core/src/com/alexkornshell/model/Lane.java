package com.alexkornshell.model;

import java.util.ArrayList;

public class Lane {
    ArrayList<GeneralCar> cars;
    int n;
    double fromX;
    double toX;
    double fromY;
    double toY;
    double length;
    double width;

    public Lane(int n, double fromX, double toX, double fromY, double toY, double length, double width) {
        this.n = n;
        this.cars = new ArrayList<>();
        this.fromX = fromX;
        this.toX = toX;
        this.fromY = fromY;
        this.toY = toY;
        this.length = length;
        this.width = width;
    }

    @Override
    public String toString() {
        return "Lane{" +
                "n=" + n +
                '}';
    }
}
