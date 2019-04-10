package com.alexkornshell.model;

import com.badlogic.gdx.math.RandomXS128;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;

public class Crossroad {
    ArrayList<Lane> lanes;
    int screen;
    double scale;
    int density;
    int left;
    int right;

    public Crossroad(ArrayList<Lane> lanes, int screen, double scale, int density, int left, int right) {
        this.lanes = lanes;
        this.screen = screen;
        this.scale = scale;
        this.density = density;
        this.left = left;
        this.right = right;
    }

    public boolean generateCar(Lane laneF) {
        Car car;
        Random rand = new RandomXS128();

        for (Car c : laneF.cars) {
            if (abs(c.x - laneF.fromX) < 1 && c.x != laneF.fromX || abs(c.y - laneF.fromY) < 1 && c.y != laneF.fromY)
                return false;    // Поправить, но норм. По факту длина машины 0.82
        }

        int carW = rand.nextInt(7) + 18;
        CarConfig carConfig = new CarConfig(carW, 2 * carW + rand.nextInt(7), rand.nextDouble() * 30 + 30);  // Километры в час

        if (rand.nextInt(density) == 0) {
            if (laneF.n % 4 == 0) {
                if (laneF.fromX - laneF.toX != 0) {
                    if (rand.nextInt(100) < left) car = new Car(this, laneF, lanes.get((laneF.n + 5) % 16), carConfig);
                    else car = new Car(this, laneF, lanes.get((laneF.n + 9) % 16), carConfig);
                } else {
                    car = new Car(this, laneF, lanes.get((laneF.n + 9) % 16), carConfig);
                }
            } else {
                if (rand.nextInt(100) < right) car = new Car(this, laneF, lanes.get(laneF.n + 1), carConfig);
                else car = new Car(this, laneF, lanes.get((laneF.n + 13) % 16), carConfig);
            }
            laneF.cars.add(car);
        }
        return true;

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
