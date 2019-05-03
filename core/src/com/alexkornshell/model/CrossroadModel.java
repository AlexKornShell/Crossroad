package com.alexkornshell.model;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class CrossroadModel extends ApplicationAdapter {

    final float PPM = 12f;
    final float UPM = 7f;
    private Viewport viewport;
    OrthographicCamera camera;
    Box2DDebugRenderer renderer;
    World world;
    Stage stage;
    Group model;
    Group ui;
    Crossroad cross;
    int screen;
    int controls;
    boolean paused;

    public CrossroadModel(int screen, int controls) {
        this.screen = screen;
        this.controls = controls;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / PPM / 2f, Gdx.graphics.getHeight() / PPM / 2f, 0);
        camera.update();
        viewport = new FitViewport(camera.viewportWidth / PPM, camera.viewportHeight / PPM, camera);
        renderer = new Box2DDebugRenderer();
        //    batch = new SpriteBatch();
        //    batch.setProjectionMatrix(camera.combined);

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                contact.getFixtureA().getBody().setLinearVelocity(0, 0);
                contact.getFixtureB().getBody().setLinearVelocity(0, 0);
                contact.getFixtureA().getBody().setAngularVelocity(0);
                contact.getFixtureB().getBody().setAngularVelocity(0);
                ((GeneralCar) contact.getFixtureA().getBody().getUserData()).crashed = true;
                ((GeneralCar) contact.getFixtureB().getBody().getUserData()).crashed = true;
                paused = true;
                //    System.out.println(contact.getFixtureA().getBody().getPosition() + " " + contact.getFixtureA().getBody().getWorldCenter());
                //    System.out.println(((GeneralCar) contact.getFixtureA().getBody().getUserData()).x + " "+ ((GeneralCar) contact.getFixtureA().getBody().getUserData()).carP.getX() + " " + ((GeneralCar) contact.getFixtureA().getBody().getUserData()).width);
                //    System.out.println();
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        model = new Group();
        //    model.setScale(1 / PPM);
        stage.addActor(model);
        ui = new Group();
        ui.setScale(1 / PPM);
        stage.addActor(ui);

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

        cross = new Crossroad(camera, world, model, lanes, screen, PPM, UPM, 500, 0, 100);
        model.addActor(cross);

        initUI();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        stage.draw();
    //    renderer.render(world, camera.combined);

        if (!paused) world.step(1 / 60f, 6, 2);
        if (!paused) stage.act(1 / 60f);
        for (Lane lane : cross.lanes) {
            if (lane.n % 2 == 0) {
                cross.removeCar(lane);
            }
        }
    }

    private void initUI() {
        Skin skin = new Skin(Gdx.files.internal("core/assets/uiskin.json"));

        Image blank = new Image(new Texture(Gdx.files.internal("core/assets/blank.png")));
        blank.setPosition(screen, 0);
        ui.addActor(blank);

        Label label = new Label("Manage traffic", skin, "black");
        label.setPosition(screen + controls / 2 - label.getWidth() / 2, screen - label.getHeight() - 20);
        ui.addActor(label);

        Label ldensity = new Label("Density  ", skin, "black");
        ldensity.setPosition(screen + 20, screen - label.getHeight() - ldensity.getHeight() - 50);
        ui.addActor(ldensity);

        final TextField density = new TextField("" + cross.density, skin);
        density.setSize(50, ldensity.getHeight());
        density.setPosition(screen + ldensity.getWidth() + 30, screen - label.getHeight() - ldensity.getHeight() - 50);
        ui.addActor(density);

        Label lleft = new Label("Left (%)  ", skin, "black");
        lleft.setPosition(screen + 20, screen - label.getHeight() - ldensity.getHeight() - lleft.getHeight() - 60);
        ui.addActor(lleft);

        final TextField left = new TextField("" + cross.left, skin);
        left.setSize(38, lleft.getHeight());
        left.setPosition(screen + lleft.getWidth() + 30, screen - label.getHeight() - ldensity.getHeight() - lleft.getHeight() - 60);
        ui.addActor(left);

        Label lright = new Label("Right (%)", skin, "black");
        lright.setPosition(screen + 20, screen - label.getHeight() - ldensity.getHeight() - lleft.getHeight() - lright.getHeight() - 70);
        ui.addActor(lright);

        final TextField right = new TextField("" + cross.right, skin);
        right.setSize(38, lleft.getHeight());
        right.setPosition(screen + lright.getWidth() + 30, screen - label.getHeight() - ldensity.getHeight() - lleft.getHeight() - lright.getHeight() - 70);
        ui.addActor(right);

        TextButton apply = new TextButton("Apply", skin, "black");
        apply.setPosition(screen + controls / 2 - apply.getWidth() / 2, screen - label.getHeight() - ldensity.getHeight() - lleft.getHeight() - lright.getHeight() - apply.getHeight() - 80);
        apply.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                if (density.getText().matches("\\d+")) cross.density = Integer.valueOf(density.getText());
                if (left.getText().matches("\\d+")) cross.left = Integer.valueOf(left.getText());
                if (right.getText().matches("\\d+")) cross.right = Integer.valueOf(right.getText());
            }
        });
        ui.addActor(apply);

        ImageButton play = new ImageButton(skin, "play");
        play.setPosition(screen + 40, screen - label.getHeight() - ldensity.getHeight() - lleft.getHeight() - lright.getHeight() - apply.getHeight() - play.getHeight() - 90);
        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                paused = false;
            }
        });
        ui.addActor(play);

        ImageButton pause = new ImageButton(skin, "pause");
        pause.setPosition(screen + play.getWidth() + 50, screen - label.getHeight() - ldensity.getHeight() - lleft.getHeight() - lright.getHeight() - apply.getHeight() - pause.getHeight() - 90);
        pause.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                paused = true;
            }
        });
        ui.addActor(pause);

        ImageButton stop = new ImageButton(skin, "stop");
        stop.setPosition(screen + play.getWidth() + pause.getWidth() + 60, screen - label.getHeight() - ldensity.getHeight() - lleft.getHeight() - lright.getHeight() - apply.getHeight() - stop.getHeight() - 90);
        stop.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        ui.addActor(stop);
    }

    @Override
    public void dispose() {
        world.dispose();
        stage.clear();
        stage.dispose();
    }
}
