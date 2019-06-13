package com.alexkornshell.model;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import static java.lang.Math.*;

public class GeneralCar extends AbstractCar {

    double r, rx, ry;
    double w, dw, w0;

    GeneralCar(Crossroad crossroad, Lane laneF, Lane laneT, CarConfig carConfig, boolean b) {

        super(crossroad, laneF, laneT, carConfig, b);

        if (!((laneF.n + laneT.n) % 16 == 1 || (laneF.n + laneT.n) % 16 == 9)) {
            if (laneF.n % 4 == 0) {
                r = 1 + min(abs(laneF.fromX), abs(laneF.fromY));
                w = abs(v) / r;
            } else if (laneF.n % 2 == 0) {
                r = 1 - min(abs(laneF.fromX), abs(laneF.fromY));
                w = -abs(v) / r;
            }

            if (abs(laneF.toX) == 1) {
                rx = laneF.toX;
                if (abs(laneF.toY - r) == 1) ry = laneF.toY - r;
                else ry = laneF.toY + r;
            } else {
                ry = laneF.toY;
                if (abs(laneF.toX - r) == 1) rx = laneF.toX - r;
                else rx = laneF.toX + r;
            }
        }

        if (laneF.n == 2 || laneF.n == 12) dw = -PI / 2;
        else if (laneF.n == 14 || laneF.n == 8) dw = 0;
        else if (laneF.n == 10 || laneF.n == 4) dw = PI / 2;
        else if (laneF.n == 6 || laneF.n == 0) dw = PI;
        w0 = dw;

    }

    @Override
    public void act(float delta) {
        if (!crashed && body != null) {
            if (stopped) {
                stopped = toStop();
                //if (!stopped) body.setLinearVelocity((float) vx * cross.UPM, (float) vy * cross.UPM);
                int i = laneF.cars.indexOf(this);
                if (!stopped && onFrom && (i == 0 || !laneF.cars.get(i - 1).onFrom)) laneF.toStop = false;
            }
            if (body.getLinearVelocity().x != 0 && signum(body.getLinearVelocity().x) == signum(vx)) vx = body.getLinearVelocity().x / cross.UPM;
            else if (signum(body.getLinearVelocity().x) != signum(vx)) body.setLinearVelocity((float) vx * cross.UPM, body.getLinearVelocity().y);
            if (body.getLinearVelocity().y != 0 && signum(body.getLinearVelocity().y) == signum(vy)) vy = body.getLinearVelocity().y / cross.UPM;
            else if (signum(body.getLinearVelocity().y) != signum(vy)) body.setLinearVelocity(body.getLinearVelocity().x, (float) vy * cross.UPM);
            dynamics(delta);
            move(delta);
            stopped = toStop();
        //    if (onFrom && body.getLinearVelocity().x != 0 && signum(body.getLinearVelocity().x) == signum(vx)) vx = body.getLinearVelocity().x / cross.UPM;
        //    else if (onFrom && signum(body.getLinearVelocity().x) != signum(vx)) body.setLinearVelocity((float) vx * cross.UPM, body.getLinearVelocity().y);
        //    if (onFrom && body.getLinearVelocity().y != 0 && signum(body.getLinearVelocity().y) == signum(vy)) vy = body.getLinearVelocity().y / cross.UPM;
        //    else if (onFrom && signum(body.getLinearVelocity().y) != signum(vy)) body.setLinearVelocity(body.getLinearVelocity().x, (float) vy * cross.UPM);
            if (onCross && body.getLinearVelocity().x != 0) vx = body.getLinearVelocity().x / cross.UPM;
            if (onCross && body.getLinearVelocity().y != 0) vy = body.getLinearVelocity().y / cross.UPM;
            //if (r != 0 && laneF.n % 4 == 0) w = body.getLinearVelocity().len() / r;
            //else if (r != 0 && laneF.n % 2 == 0) w = -body.getLinearVelocity().len() / r;
            x = bodyToWorld(body.getPosition().x);
            y = bodyToWorld(body.getPosition().y);
        }
    }

    @Override
    public void draw(Batch batch, float alpha) {
        batch.setProjectionMatrix(cross.camera.combined);
        if (body != null && r == 0)
            batch.draw(carR, bodyToScreen(body.getPosition().x, carC.width) / cross.PPM, bodyToScreen(body.getPosition().y, carC.height) / cross.PPM, carC.carW / cross.PPM / 2f, carC.carH / cross.PPM / 2f, carC.carW / cross.PPM, carC.carH / cross.PPM, carC.carW / 22f, carC.carH / 48f, MathUtils.radiansToDegrees * (float) (angle + dw - w0));
        else if (body != null)
            batch.draw(carB, bodyToScreen(body.getPosition().x, carC.width) / cross.PPM, bodyToScreen(body.getPosition().y, carC.height) / cross.PPM, carC.carW / cross.PPM / 2f, carC.carH / cross.PPM / 2f, carC.carW / cross.PPM, carC.carH / cross.PPM, carC.carW / 22f, carC.carH / 48f, MathUtils.radiansToDegrees * (float) (angle + dw - w0));
    }

    boolean toStop() {
        if (toStopWithMain(vx, vy)) {
            //body.setLinearVelocity(0, 0);
            int i = laneF.cars.indexOf(this);
            if (onFrom && (i == 0 || !laneF.cars.get(i - 1).onFrom)) laneF.toStop = true;
            return true;
        }
        return false;
    }

    @Override
    protected boolean toCrash() {
        for (Lane l : cross.lanes) {
            for (GeneralCar c : l.cars) {
                if (this != c && laneF.n == c.laneF.n && laneT.n == c.laneT.n && r == 0 &&
                        ((abs(body.getLinearVelocity().x) > abs(c.body.getLinearVelocity().x) || c.stopped) && abs(laneT.toX - x) > abs(c.laneT.toX - c.x) && abs(c.x - x) < (height / 2 + c.height / 2 + s0 / cross.UPM) ||
                                (abs(body.getLinearVelocity().y) > abs(c.body.getLinearVelocity().y) || c.stopped) && abs(laneT.toY - y) > abs(c.laneT.toY - c.y) && abs(c.y - y) < (height / 2 + c.height / 2 + s0 / cross.UPM))) {
                    // && signum(vx) == signum(c.vx) && signum(vy) == signum(c.vy) && (onX && c.onX || !onX && !c.onX)) {
                    body.setLinearVelocity(c.body.getLinearVelocity());
                    return true;
                } else if (this != c && (onFrom && c.onFrom && laneF.n == c.laneF.n) &&
                        ((abs(body.getLinearVelocity().x) > abs(c.body.getLinearVelocity().x) || c.stopped) && abs(laneF.toX - x) > abs(c.laneF.toX - c.x) && abs(c.x - x) < (height / 2 + c.height / 2 + s0 / cross.UPM) ||
                                (abs(body.getLinearVelocity().y) > abs(c.body.getLinearVelocity().y) || c.stopped) && abs(laneF.toY - y) > abs(c.laneF.toY - c.y) && abs(c.y - y) < (height / 2 + c.height / 2 + s0 / cross.UPM))) {
                    body.setLinearVelocity(c.body.getLinearVelocity());
                    if (!onCross && r != 0) {
                        vx = c.vx;
                        vy = c.vy;
                        w = signum(w) * sqrt(vx * vx + vy * vy) / r;
                    }
                    return true;
                } else if (this != c && (onTo && c.onTo && laneT.n == c.laneT.n) &&
                        ((abs(body.getLinearVelocity().x) > abs(c.body.getLinearVelocity().x) || c.stopped) && abs(laneT.toX - x) > abs(c.laneT.toX - c.x) && abs(c.x - x) < (height / 2 + c.height / 2 + s0 / cross.UPM) ||
                                (abs(body.getLinearVelocity().y) > abs(c.body.getLinearVelocity().y) || c.stopped) && abs(laneT.toY - y) > abs(c.laneT.toY - c.y) && abs(c.y - y) < (height / 2 + c.height / 2 + s0 / cross.UPM))) {
                    body.setLinearVelocity(c.body.getLinearVelocity());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void move(double delta) {
        if (abs(bodyToWorld(body.getPosition().x)) <= 1 && abs(bodyToWorld(body.getPosition().y)) <= 1) {
            if (onFrom) {
                onFrom = false;
                onCross = true;
                if (r != 0) w = signum(w) * sqrt(vx * vx + vy * vy) / r;
                body.setAngularVelocity((float) w);
            }
            if (w != 0) {
                body.applyForceToCenter(new Vector2((float) (-w * w) * (body.getPosition().x - worldToBody(rx)), (float) (-w * w) * (body.getPosition().y - worldToBody(ry))), false);
                dw += w * delta;
            }
        } else if (onCross) {
            onCross = false;
            onTo = true;
            body.setAngularVelocity(0);
            if (abs(bodyToWorld(body.getPosition().x)) >= 1) vy = 0;
            else if (abs(bodyToWorld(body.getPosition().y)) >= 1) vx = 0;
            if (w != 0 && r <= 0.5) dw = w0 - PI / 2;
            else if (w != 0) dw = w0 + PI / 2;
            body.setLinearVelocity((float) vx * cross.UPM, (float) vy * cross.UPM);
            //if (onX && abs(body.getLinearVelocity().y) > abs(body.getLinearVelocity().x) || !onX && abs(body.getLinearVelocity().x) > abs(body.getLinearVelocity().y)) onX = !onX;
        }
    }


    private void dynamics(double delta) {
        // IDM
        if (onFrom || onTo || (onCross && r == 0)) {
            int i = laneF.cars.indexOf(this);
            if (laneF.toStop && onFrom && (i == 0 || !laneF.cars.get(i - 1).onFrom) && (abs(laneF.stop.x - x) > 0.5 * (height) + border && vx != 0 || abs(laneF.stop.y - y) > 0.5 * (height) + border && vy != 0)) {
                double sa;
                float dva;
                double ss;
                float vvx = abs(body.getLinearVelocity().x);
                float vvy = abs(body.getLinearVelocity().y);
                if (vvx > vvy) {
                    sa = cross.UPM * (abs(laneF.stop.x - x) - 0.5 * (height));
                    dva = vvx - abs(0);
                    ss = s0 + vvx * tt + vvx * dva / (2 * maxa * maxb);
                } else {
                    sa = cross.UPM * (abs(laneF.stop.y - y) - 0.5 * (height));
                    dva = vvy - abs(0);
                    ss = s0 + vvy * tt + vvy * dva / (2 * maxa * maxb);
                }
                if (vvx > vvy)
                    body.applyForceToCenter(new Vector2((float) (signum(vx) * maxa * (1 - pow(vvx / v0, delt) - pow(ss / sa, 2))), 0), false);
                else
                    body.applyForceToCenter(new Vector2(0, (float) (signum(vy) * maxa * (1 - pow(vvy / v0, delt) - pow(ss / sa, 2)))), false);
            } else if (i > 0) {
                GeneralCar c = laneF.cars.get(i - 1);

                double sa;
                float dva;
                double ss;
                float vvx = abs(body.getLinearVelocity().x);
                float vvy = abs(body.getLinearVelocity().y);
                if (vvx > vvy) {
                    sa = cross.UPM * (abs(c.x - x) - 0.5 * (height + c.height));
                    dva = vvx - abs(c.body.getLinearVelocity().x);
                    ss = s0 + vvx * tt + vvx * dva / (2 * maxa * maxb);
                } else {
                    sa = cross.UPM * (abs(c.y - y) - 0.5 * (height + c.height));
                    dva = vvy - abs(c.body.getLinearVelocity().y);
                    ss = s0 + vvy * tt + vvy * dva / (2 * maxa * maxb);
                }
                if (vvx > vvy)
                    body.applyForceToCenter(new Vector2((float) (signum(vx) * maxa * (1 - pow(vvx / v0, delt) - pow(ss / sa, 2))), 0), false);
                else
                    body.applyForceToCenter(new Vector2(0, (float) (signum(vy) * maxa * (1 - pow(vvy / v0, delt) - pow(ss / sa, 2)))), false);
            } else {
                if (abs(vx) > abs(vy))
                    body.applyForceToCenter(new Vector2((float) (signum(vx) * maxa * (1 - pow(abs(body.getLinearVelocity().x / v0), delt))), 0), false);
                else
                    body.applyForceToCenter(new Vector2(0, (float) (signum(vy) * maxa * (1 - pow(abs(body.getLinearVelocity().y / v0), delt)))), false);
            }
        }
    }

    @Override
    protected boolean toStopWithMain(double vx, double vy) {
        int i = laneF.cars.indexOf(this);

        if (onFrom && (i == 0 || !laneF.cars.get(i - 1).onFrom)) {
            //if (onFrom && (abs(x) - abs(laneF.toX) < height / 2 + 2 * abs(v) / 60f + 0.1 && abs(x) - abs(laneF.toX) > height / 2 || abs(y) - abs(laneF.toY) < height / 2 + 2 * abs(v) / 60f + 0.1 && abs(y) - abs(laneF.toY) > height / 2)) {
            if (super.toStopWithMain(vx, vy)) return true;
            else if ((laneF.n + 1) % 16 == laneT.n) {
                if (vy != 0) {
                    double dt = (laneF.toY - y) / vy + PI / (2 * abs(w));
                    for (GeneralCar c : cross.lanes.get((laneF.n + 4) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneT.fromX - c.x) / c.vx + (-c.height / 2 - c.border - delta) / abs(c.v) >= dt + (height / 2) / abs(v)) // Немного примерно. Очень.
                                    || ((c.laneT.fromX - c.x) / c.vx + (c.height / 2) / abs(c.v) <= 0 - border / abs(v))) && !c.stopped)  // Немного примерно. Очень.
                                return true;
                        }
                    }
                }
                return false;
            } else if ((laneF.n + 5) % 16 == laneT.n) {     // Правим тут!!!!
                if (vx != 0) {
                    double dt = (laneF.toX - x) / vx + PI / (2 * abs(w));
                    for (GeneralCar c : cross.lanes.get((laneF.n + 6) % 16).cars) { // Неплохо
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 - c.border + 0.5) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - border - 0.5) / abs(v)))) && !c.stopped)     // Примерно
                                return true;
                        }
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 8) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 - c.border + 0.5) / abs(c.v) >= (dt - 1 * PI / (2 * 4 * abs(w)) + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 1.5) / abs(c.v) <= (dt - PI / (2 * 2 * abs(w)) + (-height / 2 - border) / abs(v)))) && !c.stopped)
                                return true;
                        }
                    }
                } else if (vy != 0) {
                    double dt = (laneF.toY - y) / vy + PI / (2 * abs(w));
                    for (GeneralCar c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 - c.border + 0.5) / abs(c.v) >= (dt - PI / (2 * 2 * abs(c.w)) + (height / 2 + 0.2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 2) / abs(c.v) <= (dt + (-height / 2 - border - 0.5) / abs(v)))) && !c.stopped)
                                return true;
                            else if (c.r == 1.25 && c.onFrom && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 - c.border + 1) / abs(c.v) >= (dt + (height / 2 - 1) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height / 2 - 1) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 - border + 1) / abs(v)))) && !c.stopped) // Приближение!!
                                return true;
                            else if (c.r == 1.25 && c.onCross) return true;
                        }
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 6) % 16).cars) { // Неплохо
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 - c.border + 0.5) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1.5) / abs(c.v) <= (dt + (-height / 2 - border - 0.5) / abs(v)))) && !c.stopped) // c.height / 2 + 1
                                return true;
                        }
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 8) % 16).cars) { // Править!!!
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 - c.border + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1.5) / abs(c.v) <= (dt - PI / (2 * 2 * abs(w)) + (-height / 2 - border) / abs(v)))) && !c.stopped)
                                return true;
                        }
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 2) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 - c.border + 1) / abs(c.v) >= (dt - PI / (2 * abs(w)) + (height / 2 + 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1.5) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 - border) / abs(v)))) && !c.stopped)
                                return true;
                        }
                    }
                    for (GeneralCar c : cross.lanes.get((laneF.n + 4) % 16).cars) { // Примерно!!!
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 - c.border + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1.5) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 - border + 0.5) / abs(v)))) && !c.stopped)
                                return true;
                            else if (c.r == 1.25 && !c.stopped && c.onFrom && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 - c.border + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))))
                                // || ((c.laneF.toY - c.y) / c.vy + PI / (2 * 2 * abs(c.w)) + (c.height / 2 + 0.2) / abs(c.v) <= (dt - PI / (2 * 2 * abs(w)) + (-height / 2 - 0.2) / abs(v))))) // Приближение!!
                                return true;
                            else if (c.r == 1.25 && c.onCross) return true;
                        }
                    }
                }
            }
            return false;
        } else return false;
    }

}