package com.alexkornshell.model;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;

public class Crossroad {
    ArrayList<Lane> lanes;
    int screen;
    int width;
    int height;
    int probability;
    int left;
    int right;

    public Crossroad(ArrayList<Lane> lanes, int screen, int probability, int left, int right) {
        this.lanes = lanes;
        this.screen = screen;
        this.width = screen;
        this.height = screen;
        this.probability = probability;
        this.left = left;
        this.right = right;
    }

    public void generateCar(Lane laneFrom) {
        Car car;
        Random rand = new Random();
        int r;
        boolean closed = false;
        for (Car c : laneFrom.cars) {
            if (abs(c.x - laneFrom.fromX) < 1 && c.x != laneFrom.fromX || abs(c.y - laneFrom.fromY) < 1 && c.y != laneFrom.fromY)
                closed = true;    // Поправить, но норм. По факту длина машины 0.82
        }

        if (laneFrom.n % 4 == 0) {
            r = rand.nextInt(probability);
            if (r == 0 && !closed) {
                int carW = rand.nextInt(7) + 18;
                int carH = 2 * carW + rand.nextInt(7);
                double paramV = rand.nextDouble() + 1.5;
                r = rand.nextInt(100);
                if (laneFrom.fromX - laneFrom.toX != 0) {
                    if (r <= left) car = new Car(this, laneFrom, lanes.get((laneFrom.n + 5) % 16), carW, carH, paramV);
                    else car = new Car(this, laneFrom, lanes.get((laneFrom.n + 9) % 16), carW, carH, 2);
                }
                else {
                    car = new Car(this, laneFrom, lanes.get((laneFrom.n + 9) % 16), carW, carH, 2);
                }
                laneFrom.cars.add(car);
            }
        } else {
            r = rand.nextInt(150);
            if (r == 0 && !closed) {
                int carW = rand.nextInt(7) + 18;
                int carH = 2 * carW + rand.nextInt(7);
                double paramV = rand.nextDouble() + 1.5;
                r = rand.nextInt(100);
                if (r <= right) car = new Car(this, laneFrom, lanes.get(laneFrom.n + 1), carW, carH, 2);
                else car = new Car(this, laneFrom, lanes.get((laneFrom.n + 13) % 16), carW, carH, paramV);
                laneFrom.cars.add(car);
            }
        }

    }

    public void removeCar(Lane laneFrom) {
        int i = laneFrom.cars.size();
        for (Car c : laneFrom.cars) {
            if (abs(c.x) > 5 || abs(c.y) > 5) {
                i = laneFrom.cars.indexOf(c);
            }
        }
        if (i < laneFrom.cars.size()) laneFrom.cars.remove(i);
    }
}
