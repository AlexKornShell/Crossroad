package com.alexkornshell.model;

import com.badlogic.gdx.math.RandomXS128;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;

public class Lane {
    ArrayList<GeneralCar> cars;
    int n;
    double fromX, toX, fromY, toY;
    double length, width;

    private Random rand;
    double t;

    Lane(int n, double fromX, double toX, double fromY, double toY, double length, double width) {
        this.n = n;
        this.cars = new ArrayList<>();
        this.fromX = fromX;
        this.toX = toX;
        this.fromY = fromY;
        this.toY = toY;
        this.length = length;
        this.width = width;
        rand = new RandomXS128();
    }

    GeneralCar generateCar(Crossroad cross, double t0, double lambda, double gamma, int left, int right) {
        if (lambda != 0 && t0 >= t) {

            int height = 36;
            int width = 18;

            double dt = 1 / lambda;
            if (fromX - toX == 0) dt = -1 / lambda * Math.log(rand.nextDouble());
            else dt = 1 / lambda;
            // double ddt = -1 / gamma * Math.log(rand.nextDouble());
            double ddt = 1 / gamma;
            double v = (36 + 18 + 18) * 3.6 / cross.PPM / ddt;
            if (fromX - toX == 0) v = (0.5) * 3.6 * cross.UPM / ddt; //v = (294) * 3.6 / cross.PPM / ddt;
            else v = (height + width + width) * 3.6 / cross.PPM / ddt;
            System.out.println(dt + " " + ddt + " " + v + " " + length);
            //int carW = rand.nextInt(7) + 18;
            // CarConfig carConfig = new CarConfig(carW, 2 * carW + rand.nextInt(7), rand.nextDouble() * 30 + 30, cross.PPM * cross.UPM, this);  // Километры в час
            CarConfig carConfig = new CarConfig(width, height, v, cross.PPM * cross.UPM, this);
            GeneralCar car;

            if (n % 4 == 0) {
                if (fromX - toX != 0) {
                    if (rand.nextInt(100) < left)
                        car = new GeneralCar(cross, this, cross.lanes.get((n + 5) % 16), carConfig);
                    else car = new GeneralCar(cross, this, cross.lanes.get((n + 9) % 16), carConfig);
                } else {
                    car = new GeneralCar(cross, this, cross.lanes.get((n + 9) % 16), carConfig);
                }
            } else {
                if (rand.nextInt(100) < right)
                    car = new GeneralCar(cross, this, cross.lanes.get(n + 1), carConfig); // Временно!
                else car = new GeneralCar(cross, this, cross.lanes.get((n + 13) % 16), carConfig);
            }

            //cross.model.addActor(car);

            cars.add(car);

            //    while (abs(c.x + c.vx * ddt - fromX) < 1 && c.x != fromX || abs(c.y + c.vy * ddt - fromY) < 1 && c.y != fromY)
            //        ddt = 1 + -1 / lambda * Math.log(rand.nextDouble());
            t += dt; if (fromX - toX != 0) t += ddt;
            //    System.out.println(t);
            return car;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Lane{" +
                "n=" + n +
                '}';
    }
}
