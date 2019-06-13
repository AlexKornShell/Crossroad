package com.alexkornshell.model;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import java.util.ArrayList;
import static java.lang.Math.abs;

public class Crossroad extends Actor {
    float PPM, UPM; // Их произведение - количество пикселей на две полосы, т.е. на 7 реальных метров, или на 1-цу ширины.
    OrthographicCamera camera;
    World world;
    Group model;
    private Texture cross;
    ArrayList<Lane> lanes;
    int screen;
    double[] lambda, gamma; // Плотность движения
    int[] left, right; // Процент поворачивающих налево и направо.
    double[] length;

    double t;
    int n = 1;

    Crossroad(OrthographicCamera camera, World world, Group model, ArrayList<Lane> lanes, int screen, float PPM, float UPM, double[] lambda, double[] gamma, int[] left, int[] right, double[] length) {
        this.PPM = PPM;
        this.UPM = UPM;
        this.camera = camera;
        this.world = world;
        this.model = model;
        cross = new Texture(("core/assets/crossroad" + screen).concat("1.jpg"));
        this.lanes = lanes;
        this.screen = screen;
        this.lambda = lambda;
        this.gamma = gamma;
        this.left = left;
        this.right = right;
        this.length = length;
        for (Lane lane : lanes) if (lane.n % 2 == 0) lane.createStop(this);
    }

    @Override
    public void act(float delta) {
        generateCars(lambda);
        for (Lane lane : lanes) {
            length[lane.n] = length[lane.n] * n;
            for (GeneralCar car : lane.cars)
                if (car.onFrom && (car.body.getLinearVelocity().len2() <= 9)) length[lane.n]++; // Менять
            length[lane.n] = length[lane.n] / (n + 1);
        }
        t += delta;
        n++;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        batch.draw(cross, 0, 0, screen / PPM, screen / PPM);
        //for (Lane lane : lanes) if (lane.n % 2 == 0) batch.draw(lane.stop.carR, lane.stop.worldToScreen());
    }

    private void generateCars(double[] lambda) {
        for (Lane lane : lanes) {
            if (lane.n % 2 == 0) {
                GeneralCar car = lane.generateCar(this, t, lambda[lane.n], gamma[lane.n], left[lane.n], right[lane.n]);
                if (car != null) model.addActor(car);
            }
        }
    }

    void removeCar(Lane laneF) {
        AbstractCar c;
        for (int i = 0; i < laneF.cars.size(); i++) {
            c = laneF.cars.get(i);
            if (c.onTo && (abs(c.x) > 5 || abs(c.y) > 5) || c.crashed) {
                laneF.cars.remove(i);
                //    car.body.destroyFixture(car.body.getFixtureList().get(0));
                if (c.body != null) world.destroyBody(c.body);
                model.removeActor(c);
                c.remove();
            }
        }
    }
}