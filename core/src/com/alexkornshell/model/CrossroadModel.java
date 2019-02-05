package com.alexkornshell.model;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class CrossroadModel extends ApplicationAdapter {
	Crossroad cross;
	SpriteBatch batch;
	Texture img;
	Texture ver;
	Texture hor;
	TextureRegion car;
	TextureRegion carBlue;

	int screen;
	int carW;
	int carH;
	int t = 0;
	
	@Override
	public void create () {
	    screen = 512;
	    carW = 24;
	    carH = 53;

        batch = new SpriteBatch();
        img = new Texture("core/assets/crossroad512.jpg");
        ver = new Texture("core/assets/ver.png");
        hor = new Texture("core/assets/hor.png");
        car = new TextureRegion(new Texture(Gdx.files.internal("core/assets/car.png")), 0, 0, carW, carH);
		carBlue = new TextureRegion(new Texture(Gdx.files.internal("core/assets/carBlue.png")), 0, 0, carW, carH + 1);

		Lane lane1 = new Lane(1, 0.25, 0.25, 1, 3, 2, 0.5);
		Lane lane3 = new Lane(3, 0.75, 0.75, 1, 3, 2, 0.5);
		Lane lane2 = new Lane(2, 3, 1, 0.75, 0.75, 2, 0.5);
		Lane lane4 = new Lane(4, 3, 1, 0.25, 0.25, 2, 0.5);
		Lane lane5 = new Lane(5, 1, 3, -0.25, -0.25, 2, 0.5);
		Lane lane7 = new Lane(7, 1, 3, -0.75, -0.75, 2, 0.5);
		Lane lane6 = new Lane(6, 0.75, 0.75, -3, -1, 2, 0.5);
		Lane lane8 = new Lane(8, 0.25, 0.25, -3, -1, 2, 0.5);
		Lane lane9 = new Lane(9, -0.25, -0.25, -1, -3, 2, 0.5);
		Lane lane11 = new Lane(11, -0.75, -0.75, -1, -3, 2, 0.5);
		Lane lane10 = new Lane(10, -3, -1, -0.75, -0.75, 2, 0.5);
		Lane lane12 = new Lane(12, -3, -1, -0.25, -0.25, 2, 0.5);
		Lane lane13 = new Lane(13, -1, -3, 0.25, 0.25, 2, 0.5);
		Lane lane15 = new Lane(15, -1, -3, 0.75, 0.75, 2, 0.5);
		Lane lane14 = new Lane(14, -0.75, -0.75, 3, 1, 2, 0.5);
		Lane lane0 = new Lane(0, -0.25, -0.25, 3, 1, 2, 0.5);

		ArrayList<Lane> lanes = new ArrayList<Lane>();
		lanes.add(lane0);
		lanes.add(lane1);
		lanes.add(lane2);
		lanes.add(lane3);
		lanes.add(lane4);
		lanes.add(lane5);
		lanes.add(lane6);
		lanes.add(lane7);
		lanes.add(lane8);
		lanes.add(lane9);
		lanes.add(lane10);
		lanes.add(lane11);
		lanes.add(lane12);
		lanes.add(lane13);
		lanes.add(lane14);
		lanes.add(lane15);

		cross = new Crossroad(lanes, screen);

	//	cross.lanes.get(6).cars.add(new Car(cross, lanes.get(6), lanes.get(3), 5));

		for (Lane lane : cross.lanes) {
			if (lane.n % 2 == 0) cross.generateCar(lane);
		}
	}

	@Override
	public void render () {

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
				batch.draw(car, c.polygon.getX(), c.polygon.getY(), c.polygon.getOriginX(), c.polygon.getOriginY(), carW, carH, c.polygon.getScaleX(), c.polygon.getScaleY(), c.polygon.getRotation());
			//	batch.draw(carBlue, c.p.getX(), c.p.getY(), c.p.getOriginX(), c.p.getOriginY(), carW, carH, c.p.getScaleX(), c.p.getScaleY(), c.p.getRotation());
			}
		}
		batch.end();

		for (Lane lane : cross.lanes) {
			if (lane.n % 2 == 0 && lane.cars.size() < 2) cross.generateCar(lane);
		}

	//	if (t == 122) cross.lanes.get(2).cars.add(new Car(cross, cross.lanes.get(2), cross.lanes.get(2 + 1), 5));
	//	t++;

		for (Lane lane : cross.lanes) {
			if (lane.n % 2 == 0) cross.removeCar(lane);
		}

	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

}
