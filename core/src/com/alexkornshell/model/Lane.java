package com.alexkornshell.model;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Lane {
    ArrayList<Car> cars;
    int n;
    double fromX;
    double toX;
    double fromY;
    double toY;
    double length;
    double width;

    public Lane () {
        this.cars = new ArrayList<Car>();
    }

    public Lane(int n, double fromX, double toX, double fromY, double toY, double length, double width) {
        this.n = n;
        this.cars = new ArrayList<Car>();
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
