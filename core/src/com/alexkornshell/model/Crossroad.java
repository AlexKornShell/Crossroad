package com.alexkornshell.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;

public class Crossroad extends Actor {
    float PPM;
    float UPM;
    OrthographicCamera camera;
    World world;
    Group model;
    private Texture cross;
    public ArrayList<Lane> lanes;
    public int screen;
    public int scale; // Количество пикселей на две полосы, т.е. на 7 реальных метров, или на 1-цу ширины.
    public int density; // Плотность движения.
    public int left; // Процент поворачивающих налево.
    public int right; // Процент поворачивающих направо.

    public Crossroad(OrthographicCamera camera, World world, Group model, ArrayList<Lane> lanes, int screen, int controls, int scale, float PPM, float UPM, int density, int left, int right) {
        this.PPM = PPM;
        this.UPM = UPM;
        this.camera = camera;
        this.world = world;
        this.model = model;
        cross = new Texture("core/assets/crossroad"+screen+"1.jpg");
        this.lanes = lanes;
        this.screen = screen;
        this.scale = scale;
        this.density = density;
        this.left = left;
        this.right = right;
    }

    @Override
    public void act (float delta) {
        for (Lane lane : lanes) {
            if (lane.n % 2 == 0) {
                generateCar(lane);
                removeCar(lane);
            }
        }
    }

    @Override
    public void draw(Batch batch, float alpha) {
        batch.draw(cross, 0, 0, screen / PPM, screen / PPM); // Changed
    //    batch.draw(cross, 0, 0);
        //    Texture ver = new Texture("core/assets/ver.png");
        //    Texture hor = new Texture("core/assets/hor.png");
        /*    batch.begin();
        	batch.draw(ver, (float) (screen / 2.0 - scale), 0);
        	batch.draw(hor, 0, (float) (screen / 2.0 - scale));
        	batch.draw(ver, (float) (screen / 2.0 + scale), 0);
        	batch.draw(hor, 0, (float) (screen / 2.0 + scale));
        batch.end(); */
    }

    public boolean generateCar(Lane laneF) {
        Car car;
        Random rand = new RandomXS128();

        for (Car c : laneF.cars) {
            if (abs(c.x - laneF.fromX) < 1 && c.x != laneF.fromX || abs(c.y - laneF.fromY) < 1 && c.y != laneF.fromY) return false;    // Поправить, но норм. По факту длина машины 0.82
        }

        int carW = rand.nextInt(7) + 18;
        CarConfig carConfig = new CarConfig(carW, 2 * carW + rand.nextInt(7), rand.nextDouble() * 30 + 30, scale, laneF);  // Километры в час

        if (rand.nextInt(density) <= 0) { // Временно!
            if (laneF.n % 4 == 0) {
                if (laneF.fromX - laneF.toX != 0) {
                    if (rand.nextInt(100) < left) car = new Car(this, laneF, lanes.get((laneF.n + 5) % 16), carConfig);
                    else car = new Car(this, laneF, lanes.get((laneF.n + 9) % 16), carConfig);
                } else {
                    car = new Car(this, laneF, lanes.get((laneF.n + 9) % 16), carConfig);
                }
            } else {
                if (rand.nextInt(100) < right) car = new Car(this, laneF, lanes.get(laneF.n + 1), carConfig); // Временно!
                else car = new Car(this, laneF, lanes.get((laneF.n + 13) % 16), carConfig);
            }
            laneF.cars.add(car);
            model.addActor(car);
        }
        return true;

    }

    private Car removeCar(Lane laneF) {
        int i = laneF.cars.size();
        for (Car c : laneF.cars) {
            if (abs(c.x) > 5 || abs(c.y) > 5) {
                i = laneF.cars.indexOf(c);
            }
        }
        if (i < laneF.cars.size()) {
            Car car = laneF.cars.get(i);
            laneF.cars.remove(i);
        //    world.destroyBody(car.body);
            model.removeActor(car);
            car.remove();
            return car;
        }
        return null;
    }
}
