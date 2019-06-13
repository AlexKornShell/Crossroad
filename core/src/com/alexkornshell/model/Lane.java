package com.alexkornshell.model;

import com.badlogic.gdx.math.RandomXS128;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.log;
import static java.lang.Math.signum;

public class Lane {
    ArrayList<GeneralCar> cars;
    int n;
    double fromX, toX, fromY, toY;
    double length, width;
    private Random rand;
    double t;
    AbstractCar stop;
    boolean toStop;

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
        toStop = false;
    }

    void createStop(Crossroad cross) {
        stop = new AbstractCar(cross, this, this, new CarConfig(18, 36, 0, 84f, this), false);
        stop.x += (toX - fromX);
        stop.y += (toY - fromY);
        if (toX - fromX != 0) stop.x += signum(toX - fromX) * stop.height;
        else if (toY - fromY != 0) stop.y += signum(toY - fromY) * stop.height;
    }

    GeneralCar generateCar(Crossroad cross, double t0, double lambda, double gamma, int left, int right) {
        if (lambda != 0 && t0 >= t) {

            //int height = 36;
            //int width = 18;

            double dt = -1 / lambda * log(rand.nextDouble());
            //if (fromX - toX != 0) dt = 1 / lambda;

            //double ddt = -1 / gamma * log(rand.nextDouble());
            //ddt = 1 / gamma;
            //double v = (36 + 18 + 18) * 3.6 / cross.PPM / ddt;
            //if (fromX - toX == 0) v = (0.5) * 3.6 * cross.UPM / ddt; //v = (294) * 3.6 / cross.PPM / ddt;
            //else v = (height + width + width) * 3.6 / cross.PPM / ddt;

            //System.out.println(dt + " " + ddt + " " + v + " " + length);
            int carW = rand.nextInt(7) + 18;
            int carH = 2 * carW + rand.nextInt(7);
            double v = rand.nextDouble() * 30 + 30;
            if (fromX - toX != 0) v = rand.nextDouble() * 30 + 30;
            double ddt = carH * 3.6 / cross.PPM / v;
            //System.out.println(ddt);


            CarConfig carConfig = new CarConfig(carW, carH, v, cross.PPM * cross.UPM, this);  // Километры в час
            //CarConfig carConfig = new CarConfig(width, height, rand.nextDouble() * 30 + 30, cross.PPM * cross.UPM, this);  // Километры в час
            //CarConfig carConfig = new CarConfig(width, height, v, cross.PPM * cross.UPM, this);

            GeneralCar car;
            if (n % 4 == 0) {
                if (rand.nextInt(100) < left)
                    car = new GeneralCar(cross, this, cross.lanes.get((n + 5) % 16), carConfig, true);
                else car = new GeneralCar(cross, this, cross.lanes.get((n + 9) % 16), carConfig, true);
            } else {
                if (rand.nextInt(100) < right)
                    car = new GeneralCar(cross, this, cross.lanes.get(n + 1), carConfig, true); // Временно!
                else car = new GeneralCar(cross, this, cross.lanes.get((n + 13) % 16), carConfig, true);
            }
            cars.add(car);

            t += dt;
            t += ddt;
            //if (fromX - toX != 0) t += ddt;
            return car;
        } else if (lambda == 0) t = t0;
        return null;
    }
}