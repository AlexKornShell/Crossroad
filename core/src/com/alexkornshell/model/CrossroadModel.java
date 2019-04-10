package com.alexkornshell.model;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class CrossroadModel extends ApplicationAdapter {

    Stage stage;

    Crossroad cross;
    SpriteBatch batch;
    Texture img;
    Texture ver;
    Texture hor;
    TextureRegion menu;
    TextureRegion carR;
    TextureRegion carB;
    TextButton apply;
    TextField text;

    int screen;
    int controls;
    double scale;

    public CrossroadModel(int screen, int controls) {
        this.screen = screen;
        this.controls = controls;
    }

    @Override
    public void create() {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        scale = 84; // Количество пикселей на две полосы, т.е. на 7 реальных метров, или на 1-цу ширины.

        int density = 150;
        int left = 0;
        int right = 0;

        int carW = 22;
        int carH = 48;

        batch = new SpriteBatch();
        img = new Texture("core/assets/crossroad"+screen+".jpg");
        ver = new Texture("core/assets/ver.png");
        hor = new Texture("core/assets/hor.png");
        menu = new TextureRegion(new Texture(Gdx.files.internal("core/assets/blank.png")), 0, 0, controls, screen);
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

        cross = new Crossroad(lanes, screen, scale, density, left, right);

        initUI();

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
        //	batch.draw(ver, (float) (screen / 2.0 - scale), 0);
        //	batch.draw(hor, 0, (float) (screen / 2.0 - scale));
        //	batch.draw(ver, (float) (screen / 2.0 + scale), 0);
        //	batch.draw(hor, 0, (float) (screen / 2.0 + scale));
        for (Lane lane : cross.lanes) {
            for (Car c : lane.cars) {
                batch.draw(carR, c.polygon.getX(), c.polygon.getY(), c.polygon.getOriginX(), c.polygon.getOriginY(), c.carW, c.carH, c.polygon.getScaleX(), c.polygon.getScaleY(), c.polygon.getRotation());
                //	batch.draw(carB, c.p.getX(), c.p.getY(), c.p.getOriginX(), c.p.getOriginY(), c.carW, c.carH, c.p.getScaleX(), c.p.getScaleY(), c.p.getRotation());
            }
        }
        batch.draw(menu, screen, 0);
        batch.end();

        for (Lane lane : cross.lanes) {
            if (lane.n % 2 == 0) {
                cross.generateCar(lane);
                cross.removeCar(lane);
            }
        }
        //stage.act();
        stage.draw();
    }

    private void initUI() {
        Skin skin = new Skin(Gdx.files.internal("core/assets/uiskin.json"));

    /*    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        skin.add("default", new BitmapFont());

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", new Color(0, 0, 0, 1));
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle); */

        final Label label = new Label("Manage traffic", skin,"black");
        label.setPosition(screen + controls / 2 - label.getWidth() / 2, screen - label.getHeight() - 20);
        stage.addActor(label);

    /*    text = new TextField("", skin);
        text.setPosition(screen + controls / 2 - text.getWidth() / 2, screen - label.getHeight() - text.getHeight() - 40);
        stage.addActor(text); */

        final Label density = new Label("Density  ", skin,"black");
        density.setPosition(screen + 20, screen - label.getHeight() - density.getHeight() - 50);
        stage.addActor(density);

        final TextField tdensity = new TextField(""+cross.density, skin);
        tdensity.setSize(50, density.getHeight());
        tdensity.setPosition(screen + density.getWidth() + 30, screen - label.getHeight() - density.getHeight() - 50);
        stage.addActor(tdensity);

        final Label left = new Label("Left (%)  ", skin,"black");
        left.setPosition(screen + 20, screen - label.getHeight() - density.getHeight() - left.getHeight() - 60);
        stage.addActor(left);

        final TextField tleft = new TextField(""+cross.left, skin);
        tleft.setSize(38, left.getHeight());
        tleft.setPosition(screen + left.getWidth() + 30, screen - label.getHeight() - density.getHeight() - left.getHeight() - 60);
        stage.addActor(tleft);

        final Label right = new Label("Right (%)", skin,"black");
        right.setPosition(screen + 20, screen - label.getHeight() - density.getHeight() - left.getHeight() - right.getHeight() - 70);
        stage.addActor(right);

        final TextField tright = new TextField(""+cross.right, skin);
        tright.setSize(38, left.getHeight());
        tright.setPosition(screen + right.getWidth() + 30, screen - label.getHeight() - density.getHeight() - left.getHeight() - right.getHeight() - 70);
        stage.addActor(tright);

        apply = new TextButton("Apply", skin, "black");
        apply.setPosition(screen + controls / 2 - apply.getWidth() / 2, screen - label.getHeight() - density.getHeight() - left.getHeight() - right.getHeight() - apply.getHeight() - 80);
        apply.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeListener.ChangeEvent event, Actor actor) {
                if (tdensity.getText() != "") cross.density = Integer.valueOf(tdensity.getText());
                if (tleft.getText() != "") cross.left = Integer.valueOf(tleft.getText());
                if (tright.getText() != "") cross.right = Integer.valueOf(tright.getText());
                //tleft.setText(tleft.getText());
            }
        });
        stage.addActor(apply);

    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}
