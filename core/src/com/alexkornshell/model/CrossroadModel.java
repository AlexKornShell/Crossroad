package com.alexkornshell.model;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class CrossroadModel extends ApplicationAdapter {

    Crossroad cross;
    SpriteBatch batch;
    Texture img;
    Texture ver;
    Texture hor;
    TextureRegion carR;
    TextureRegion carB;

    int screen;

    public CrossroadModel(int screen) {
        this.screen = screen;
    }

    @Override
    public void create() {

        int probability = 150;
        int left = -1;
        int right = -1;

        int carW = 22;
        int carH = 48;

        batch = new SpriteBatch();
        img = new Texture("core/assets/crossroad"+screen+".jpg");
        ver = new Texture("core/assets/ver.png");
        hor = new Texture("core/assets/hor.png");
        carR = new TextureRegion(new Texture(Gdx.files.internal("core/assets/car2.png")), 0, 0, carW, carH);
        carB = new TextureRegion(new Texture(Gdx.files.internal("core/assets/car2b.png")), 0, 0, carW, carH);

        ArrayList<Lane> lanes = new ArrayList<Lane>();
        lanes.add(new Lane(0, -0.25, -0.25, 4.5, 1, 3.5, 0.5));
        lanes.add(new Lane(1, 0.25, 0.25, 1, 4.5, 3.5, 0.5));
        lanes.add(new Lane(2, 4.5, 1, 0.75, 0.75, 3.5, 0.5));
        lanes.add(new Lane(3, 0.75, 0.75, 1, 4.5, 3.5, 0.5));
        lanes.add(new Lane(4, 4.5, 1, 0.25, 0.25, 3.5, 0.5));
        lanes.add(new Lane(5, 1, 4.5, -0.25, -0.25, 3.5, 0.5));
        lanes.add(new Lane(6, 0.75, 0.75, -4.5, -1, 3.5, 0.5));
        lanes.add(new Lane(7, 1, 4.5, -0.75, -0.75, 3.5, 0.5));
        lanes.add(new Lane(8, 0.25, 0.25, -4.5, -1, 3.5, 0.5));
        lanes.add(new Lane(9, -0.25, -0.25, -1, -4.5, 3.5, 0.5));
        lanes.add(new Lane(10, -4.5, -1, -0.75, -0.75, 3.5, 0.5));
        lanes.add(new Lane(11, -0.75, -0.75, -1, -4.5, 3.5, 0.5));
        lanes.add(new Lane(12, -4.5, -1, -0.25, -0.25, 3.5, 0.5));
        lanes.add(new Lane(13, -1, -4.5, 0.25, 0.25, 3.5, 0.5));
        lanes.add(new Lane(14, -0.75, -0.75, 4.5, 1, 3.5, 0.5));
        lanes.add(new Lane(15, -1, -4.5, 0.75, 0.75, 3.5, 0.5));

        cross = new Crossroad(lanes, screen, probability, left, right);

        for (Lane lane : cross.lanes) {
            if (lane.n % 2 == 0) cross.generateCar(lane);
        }
    }

    @Override
    public void render() {

        for (Lane lane : cross.lanes) {
            for (Car c : lane.cars) {
                c.move(Gdx.graphics.getDeltaTime());
            }
        }

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(img, 0, 0);
        //	batch.draw(ver, -1 * 256 / 3 + 256, 0);
        //	batch.draw(hor, 0, -1 * 256 / 3 + 256);
        //	batch.draw(ver, 1 * 256 / 3 + 256, 0);
        //	batch.draw(hor, 0, 1 * 256 / 3 + 256);
        for (Lane lane : cross.lanes) {
            for (Car c : lane.cars) {
                batch.draw(carR, c.polygon.getX(), c.polygon.getY(), c.polygon.getOriginX(), c.polygon.getOriginY(), c.carW, c.carH, c.polygon.getScaleX(), c.polygon.getScaleY(), c.polygon.getRotation());
                //	batch.draw(carB, c.p.getX(), c.p.getY(), c.p.getOriginX(), c.p.getOriginY(), c.carW, c.carH, c.p.getScaleX(), c.p.getScaleY(), c.p.getRotation());
            }
        }
        batch.end();

        for (Lane lane : cross.lanes) {
            if (lane.n % 2 == 0) {
                cross.generateCar(lane);
                cross.removeCar(lane);
            }
        }

    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}
