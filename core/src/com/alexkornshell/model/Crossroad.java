package com.alexkornshell.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static java.lang.Math.abs;

public class Crossroad {
    //    ArrayList<Road> roads;
    ArrayList<Lane> lanes;
    int screen;
    int width;
    int height;

    public Crossroad () {
        this.lanes = new ArrayList<Lane>();
    }

    public Crossroad (ArrayList<Lane> lanes, int screen) {
        this.lanes = lanes;
        this.screen = screen;
        this.width = screen;
        this.height = screen;
    }

    public void generateCar (Lane laneFrom) {
        Car car;
        Random rand = new Random();
        int r;
        boolean closed = false;
        for (Car c : laneFrom.cars) {
            if (abs(c.x - laneFrom.fromX) < 1 && c.x != laneFrom.fromX || abs(c.y - laneFrom.fromY) < 1 && c.y != laneFrom.fromY) closed = true;    // Поправить, но норм. По факту длина машины 0.82
        }

        if (laneFrom.n % 4 == 0) {
            r = rand.nextInt(200);
            if (r == 0 && !closed) {
                r = rand.nextInt(2);
                if (r == 0) car = new Car(this, laneFrom, lanes.get((laneFrom.n + 5) % 16), 5);
                else car = new Car(this, laneFrom, lanes.get((laneFrom.n + 9) % 16), 5);
                laneFrom.cars.add(car);
            }
        } else {
            r = rand.nextInt(200);
            if (r == 0 && !closed) {
                r = rand.nextInt(2);
                if (r == 0) car = new Car(this, laneFrom, lanes.get((laneFrom.n + 13) % 16), 5);
                else car = new Car(this, laneFrom, lanes.get(laneFrom.n + 1), 5);
                laneFrom.cars.add(car);
            }
        }

    }

    public void removeCar (Lane laneFrom) {
        int i = laneFrom.cars.size();
        for (Car c : laneFrom.cars) {
            if (abs(c.x) > 4 || abs(c.y) > 4) {
                i = laneFrom.cars.indexOf(c);
            }
        }
        if (i < laneFrom.cars.size()) laneFrom.cars.remove(i);
    }
}
