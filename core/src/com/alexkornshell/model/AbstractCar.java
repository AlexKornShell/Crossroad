package com.alexkornshell.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;

import static java.lang.Math.*;

public class AbstractCar extends Actor {

    TextureRegion carR = new TextureRegion(new Texture(Gdx.files.internal("core/assets/car2.png")), 0, 0, 22, 48);
    TextureRegion carB = new TextureRegion(new Texture(Gdx.files.internal("core/assets/car2b.png")), 0, 0, 22, 48);

    Crossroad cross;
    Body body;
    CarConfig carC;

    double width, height;

    Lane laneF, laneT;

    boolean onFrom, onCross, onTo;
    boolean onX;

    boolean crashed, stopped;

    double x, y;
    double vx, vy, v;

    double angle;

    double border;
    double delta;

    double maxa;
    double v0;
    double s0;
    double tt;
    double maxb;
    double delt;

    AbstractCar(Crossroad crossroad, Lane laneF, Lane laneT, CarConfig carConfig) {

        maxa = 3;//0.73;
        v0 = 30;
        s0 = 2;
        tt = 1.5;
        maxb = 1.67;
        delt = 4;

        this.cross = crossroad;
        this.carC = carConfig;
        carC.laneT = laneT;
        this.width = carC.width;
        this.height = carC.height;
        this.laneF = laneF;
        this.laneT = laneT;
        this.onFrom = true;

        boolean queue = false;
        if (laneF.cars.size() > 0) {
            GeneralCar c = laneF.cars.get(laneF.cars.size() - 1);
            if (c.x - laneF.fromX < c.height / 2 + height / 2 + width && c.x != laneF.fromX || c.y - laneF.fromY < c.height / 2 + height / 2 + width && c.y != laneF.fromY) { // Менять
                this.x = c.x - c.height / 2 - height / 2 - width;
                queue = true;
            }
        }

        if (laneF.toX - laneF.fromX > 0) {
            this.vx = carC.carV / (3.6 * cross.UPM); // Единицы в секунду
            this.v = vx;
            if (queue)
                this.x = laneF.cars.get(laneF.cars.size() - 1).x - laneF.cars.get(laneF.cars.size() - 1).height / 2 - height / 2 - width;
            else this.x = laneF.fromX - height / 2;
            this.y = laneF.fromY;
            onX = true;
            angle = 3 * PI / 2;
        } else if (laneF.toX - laneF.fromX < 0) {
            this.vx = -carC.carV / (3.6 * cross.UPM);
            this.v = vx;
            if (queue)
                this.x = laneF.cars.get(laneF.cars.size() - 1).x + laneF.cars.get(laneF.cars.size() - 1).height / 2 + height / 2 + width;
            else this.x = laneF.fromX + height / 2;
            this.y = laneF.fromY;
            onX = true;
            angle = PI / 2;
        } else if (laneF.toY - laneF.fromY > 0) {
            this.vy = carC.carV / (3.6 * cross.UPM);
            this.v = vy;
            this.x = laneF.fromX;
            if (queue)
                this.y = laneF.cars.get(laneF.cars.size() - 1).y - laneF.cars.get(laneF.cars.size() - 1).height / 2 - height / 2 - width;
            else this.y = laneF.fromY - height / 2;
            angle = 0;
        } else {
            this.vy = -carC.carV / (3.6 * cross.UPM);
            this.v = vy;
            this.x = laneF.fromX;
            if (queue)
                this.y = laneF.cars.get(laneF.cars.size() - 1).y + laneF.cars.get(laneF.cars.size() - 1).height / 2 + height / 2 + width;
            else this.y = laneF.fromY + height / 2;
            angle = PI;
        }

        border = width;
        delta = abs(v) * 0.2; // 0.16

        body = initBody();

    }

    private Body initBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = cross.world.createBody(bodyDef);
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(carC.carW / cross.PPM / 2f, carC.carH / cross.PPM / 2f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.density = 0f; //1.0f / carC.carW * carC.carH;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        body.createFixture(fixtureDef);
        polygonShape.dispose();
        body.setUserData(this);
        body.setTransform((float) x * cross.UPM + cross.screen / cross.PPM / 2f, (float) y * cross.UPM + cross.screen / cross.PPM / 2f, (float) angle);
        body.setLinearVelocity((float) (vx * cross.UPM), (float) (vy * cross.UPM));
        return body;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        batch.setProjectionMatrix(cross.camera.combined);
        if (body != null)
            batch.draw(carR, bodyToScreen(body.getPosition().x, carC.width) / cross.PPM, bodyToScreen(body.getPosition().y, carC.height) / cross.PPM, carC.carW / cross.PPM / 2f, carC.carH / cross.PPM / 2f, carC.carW / cross.PPM, carC.carH / cross.PPM, carC.carW / 22f, carC.carH / 48f, MathUtils.radiansToDegrees * (float) angle);
    }

    @Override
    public void act(float delta) {
        if (!crashed && body != null) {
            if (stopped) {
                stopped = toStop();
                if (!stopped) body.setLinearVelocity((float) vx * cross.UPM, (float) vy * cross.UPM);
            } else {
                move(delta);
                stopped = toStop();
                if (body.getLinearVelocity().x != 0) vx = body.getLinearVelocity().x / cross.UPM;
                if (body.getLinearVelocity().y != 0) vy = body.getLinearVelocity().y / cross.UPM;
            }
            x = bodyToWorld(body.getPosition().x);
            y = bodyToWorld(body.getPosition().y);
        }
    }

    private boolean toStop() {
        if (toCrash()) return true;
        else if (toGive()) {
            body.setLinearVelocity(0, 0);
            return true;
        }
        return false;
    }

    protected boolean toCrash() {
        boolean toCrash = false;
        for (Lane l : cross.lanes) {
            for (GeneralCar c : l.cars) {
                if (this != c && laneF.n == c.laneF.n && laneT.n == c.laneT.n &&
                        ((abs(body.getLinearVelocity().x) > abs(c.body.getLinearVelocity().x) || c.stopped) && abs(laneT.toX - x) > abs(c.laneT.toX - c.x) && abs(c.x - x) < (height / 2 + c.height / 2 + border) ||
                                (abs(body.getLinearVelocity().y) > abs(c.body.getLinearVelocity().y) || c.stopped) && abs(laneT.toY - y) > abs(c.laneT.toY - c.y) && abs(c.y - y) < (height / 2 + c.height / 2 + border))) {
                    // && signum(vx) == signum(c.vx) && signum(vy) == signum(c.vy) && (onX && c.onX || !onX && !c.onX)) {
                    body.setLinearVelocity(c.body.getLinearVelocity());
                    return true;
                } else if (this != c && (onFrom && c.onFrom && laneF.n == c.laneF.n) &&
                        ((abs(body.getLinearVelocity().x) > abs(c.body.getLinearVelocity().x) || c.stopped) && abs(laneF.toX - x) > abs(c.laneF.toX - c.x) && abs(c.x - x) < (height / 2 + c.height / 2 + border) ||
                                (abs(body.getLinearVelocity().y) > abs(c.body.getLinearVelocity().y) || c.stopped) && abs(laneF.toY - y) > abs(c.laneF.toY - c.y) && abs(c.y - y) < (height / 2 + c.height / 2 + border))) {
                    body.setLinearVelocity(c.body.getLinearVelocity());
                    return true;
                } else if (this != c && (onTo && c.onTo && laneT.n == c.laneT.n) &&
                        ((abs(body.getLinearVelocity().x) > abs(c.body.getLinearVelocity().x) || c.stopped) && abs(laneT.toX - x) > abs(c.laneT.toX - c.x) && abs(c.x - x) < (height / 2 + c.height / 2 + border) ||
                                (abs(body.getLinearVelocity().y) > abs(c.body.getLinearVelocity().y) || c.stopped) && abs(laneT.toY - y) > abs(c.laneT.toY - c.y) && abs(c.y - y) < (height / 2 + c.height / 2 + border))) {
                    body.setLinearVelocity(c.body.getLinearVelocity());
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean toGive() {
        return toStopWithMain();
    }

    protected void move(double delta) {
        if (abs(bodyToWorld(body.getPosition().x)) <= 1 && abs(bodyToWorld(body.getPosition().y)) <= 1) {
            if (onFrom) {
                onFrom = false;
                onCross = true;
            }
        } else if (onCross) {
            onCross = false;
            onTo = true;
        }
        // dynamics(delta);
    }

    protected boolean toStopOnStraight(GeneralCar c) {
        if (vx == 0) {
            double txmax = (laneF.toX - c.x) / c.vx + (-c.height / 2 - width / 2 - c.border) / abs(c.vx) - 1 / 60f,// - delta,
                    txmin = (laneF.toX - c.x) / c.vx + (c.height / 2 + width / 2) / abs(c.vx) + 1 / 60f,
                    tymax = (c.laneF.toY - y) / vy + (height / 2 + c.width / 2) / abs(vy) + 1 / 60f,
                    tymin = (c.laneF.toY - y) / vy + (-height / 2 - c.width / 2 - border) / abs(vy) - 1 / 60f;// - delta;
    /*    if ((laneF.toX - c.x) / c.vx + (-c.height / 2 - width / 2 - c.border - delta) / abs(c.vx) < (c.laneF.toY - y) / vy + (height / 2 + c.width / 2) / abs(vy)
                && (laneF.toX - c.x) / c.vx + (c.height / 2 + width / 2) / abs(c.vx) > (c.laneF.toY - y) / vy + (-height / 2 - c.width / 2 - border - delta) / abs(vy)) */
            return txmax - tymax < 0 && txmin - tymin > 0; // Оно же return !(txmax >= tymax || txmin <= tymin);
        }
        return false;
    }

    protected boolean toStopWithMain() {
        if (onFrom && (abs(x) - abs(laneF.toX) < height / 2 + max(border, 0.1) && abs(x) - abs(laneF.toX) > height / 2 || abs(y) - abs(laneF.toY) < height / 2 + max(border, 0.1) && abs(y) - abs(laneF.toY) > height / 2)) {
            if ((laneF.n + 13) % 16 == laneT.n) {
                if (vy != 0) {
                    for (GeneralCar c : cross.lanes.get((laneF.n + 4) % 16).cars) {
                        if (!c.onTo && c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 6) % 16).cars) {
                        if (!c.onTo && c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (!c.onTo) {
                            if (c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                            else if (c.r == 0.25 && c.onFrom && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 - c.border - delta) / abs(c.v) >= ((laneT.fromY - y) / vy + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + (c.height / 2) / abs(c.v) <= (laneT.fromY - y) / vy + (-height / 2 - border - delta) / abs(v))) && !c.stopped) // Нет или да
                                return true; // Здесь если автомобиль уже выехал на перекрёсток и поворачивает, то пока успеет и без проверки
                        }
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 14) % 16).cars) {
                        if (!c.onTo) {
                            if (c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                            else if (c.r == 1.25 && c.onFrom && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.25 - width / 2 - c.border - delta) / abs(c.v) >= ((laneT.fromY - y) / vy + (height / 2 - 0.75 + c.width / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 0.25 + width / 2) / abs(c.v) <= ((laneT.fromY - y) / vy + (-height / 2 - 0.75 - c.width / 2 - border - delta) / abs(v)))) && !c.stopped) // Приближение!! И ещё нет
                                return true; // Здесь если автомобиль уже выехал на перекрёсток и поворачивает, то пока успеет и без проверки
                        }
                    }
                }
            } else if ((laneF.n + 9) % 16 == laneT.n) {
                if (vy != 0) {
                    double dt = (laneT.fromY - y) / vy;
                    for (GeneralCar c : cross.lanes.get((laneF.n + 2) % 16).cars) {
                        if (!c.onTo && c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 4) % 16).cars) {
                        if (!c.onTo) {
                            if (c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                            else if (c.r == 1.25 && !((max((c.laneF.toX - c.x) / c.vx, 0) + PI / (2 * 2 * abs(c.w)) - (c.w0 - c.dw) / c.w + (-c.height / 2 - c.border - delta) / abs(c.v) >= (dt + (height / 2 - 1) / abs(v)))
                                    || (max((c.laneF.toX - c.x) / c.vx, 0) + PI / (2 * 3 / 4 * abs(c.w)) - (c.w0 - c.dw) / c.w + (c.height / 2) / abs(c.v) <= (dt + (-height / 2 - 1 - border - delta) / abs(v)))) && !c.stopped) // Приближение!! И нет.
                                return true;
                        }
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 10) % 16).cars) {
                        if (!c.onTo && c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (!c.onTo) {
                            if (c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                            else if (c.r == 1.25 && !((max((c.laneF.toX - c.x) / c.vx, 0) + PI / (2 * 4 * abs(c.w)) - (c.w0 - c.dw) / c.w + (-c.height / 2) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || (max((c.laneF.toX - c.x) / c.vx, 0) + PI / (2 * abs(c.w)) - (c.w0 - c.dw) / c.w + (c.height / 2 - 0.5) / abs(c.v) <= (dt + (-height / 2 - 1.2) / abs(v)))) && !c.stopped) // Приближение!! И нет.
                                return true; // Здесь если автомобиль уже выехал на перекрёсток и поворачивает, то пока успеет и без проверки
                        }
                    }
                }
            }
            return false;
        } else return false;
    }


    float worldToBody(double x) {
        return (float) x * cross.UPM + cross.screen / (2 * cross.PPM);
    }

    double bodyToWorld(float b) {
        return (b - cross.screen / (2 * cross.PPM)) / cross.UPM;
    }

    float worldToScreen(double x, double w) {
        return (float) (x - w / 2) * cross.PPM * cross.UPM + cross.screen / 2f;
    }

    double screenToWorld(float s, double w) {
        return (s - cross.screen / 2f) / cross.PPM / cross.UPM + w / 2;
    }

    float bodyToScreen(float b, double w) {
        return (b - (float) w / 2 * cross.UPM) * cross.PPM;
    }

    float screenToBody(float s, double w) {
        return s / cross.PPM + (float) w / 2 * cross.UPM;
    }

    @Override
    public String toString() {
        return "GeneralCar{" + "laneF=" + laneF + ", laneT=" + laneT + '}';
    }

}
