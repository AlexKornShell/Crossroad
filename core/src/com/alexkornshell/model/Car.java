package com.alexkornshell.model;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;

import static java.lang.Math.*;

public class Car {
    Polygon polygon;
    Polygon p;

    Crossroad cross;
    int screen;

    CarConfig carConfig;

    int carW;
    int carH;

    double width;
    double height;

    Lane laneF;
    Lane laneT;

    boolean onFrom;
    boolean onCross;
    boolean onTo;

    boolean crashed;
    boolean stopped;
    boolean needStop;

    double x;
    double y;
    double vx;
    double vy;
    double v;
    double f;
    double fm;

    double w;
    double r;
    double dw;
    double w0;

    double border;
    double delta;

    int ch;
    double t;
    double dwp;
    double vxp;
    double vyp;

    public Car(Crossroad crossroad, Lane laneF, Lane laneT, CarConfig carConfig) {

        this.cross = crossroad;
        this.screen = crossroad.screen;
        this.carConfig = carConfig;
        this.carW = carConfig.carW;
        this.carH = carConfig.carH;
        this.width = (double) carW / cross.scale; // Было * 6 / screen
        this.height = (double) carH / cross.scale;
        this.laneF = laneF;
        this.laneT = laneT;
        this.onFrom = true;

        this.fm = 2;

        if (laneF.toX - laneF.fromX == 0) this.vy = (laneF.toY - laneF.fromY) / laneF.length * carConfig.carV / (3.6 * 7); // Единицы в секунду
        else this.vx = (laneF.toX - laneF.fromX) / laneF.length * carConfig.carV / (3.6 * 7);

        polygon = new Polygon(new float[]{0, 0, 0, carH, carW, carH, carW, 0});
        polygon.setOrigin(carW / 2.0f, carH / 2.0f);
        polygon.setScale(carW / 22.0f, carH / 48.0f);

        if (vy > 0) {
            polygon.setRotation(0);
            this.v = vy;
            this.x = laneF.fromX;
            this.y = laneF.fromY - height / 2;
        } else if (vy < 0) {
            polygon.setRotation(180);
            this.v = vy;
            this.x = laneF.fromX;
            this.y = laneF.fromY + height / 2;
        } else if (vx > 0) {
            polygon.setRotation(270);
            this.v = vx;
            this.x = laneF.fromX - height / 2;
            this.y = laneF.fromY;
        } else if (vx < 0) {
            polygon.setRotation(90);
            this.v = vx;
            this.x = laneF.fromX + height / 2;
            this.y = laneF.fromY;
        }
        polygon.setPosition((float) ((x - width / 2) * cross.scale + screen / 2), (float) ((y - height / 2) * cross.scale + screen / 2));

        if (!((laneF.n + laneT.n) % 16 == 1 || (laneF.n + laneT.n) % 16 == 9)) {
            if (laneF.n % 4 == 0) {
                r = 1 + min(abs(laneF.fromX), abs(laneF.fromY));
                w = abs(v) / r;
            } else if (laneF.n % 2 == 0) {
                r = 1 - min(abs(laneF.fromX), abs(laneF.fromY));
                w = -abs(v) / r;
            }
        }

        if (laneF.n == 2 || laneF.n == 12) {
            ch = -1;
            dw = -PI / 2;
        } else if (laneF.n == 14 || laneF.n == 8) {
            ch = 1;
            dw = 0;
        } else if (laneF.n == 10 || laneF.n == 4) {
            ch = -1;
            dw = PI / 2;
        } else if (laneF.n == 6 || laneF.n == 0) {
            ch = 1;
            dw = PI;
        }
        w0 = dw;

        p = new Polygon(new float[]{0, 0, 0, carH, carW, carH, carW, 0});
        p.setOrigin(carW / 2.0f, carH / 2.0f);
        p.setScale(carW / 22.0f, carH / 48.0f);
        p.setPosition((float) ((x - width / 2) * cross.scale + screen / 2), (float) ((y - height / 2) * cross.scale + screen / 2));
        p.setRotation(polygon.getRotation());

        dwp = dw;
        vxp = vx;
        vyp = vy;
        t = width / abs(v);
        border = abs(v) * t;
        delta = abs(v) * 0.09;

        p.translate((float) (vxp * t * cross.scale), (float) (vyp * t * cross.scale));

    }

    public void move(float dt) {

        if (!stopped) toChangeSpeed();

        if (!stopped && !crashed) moveP(dt);

        if (!crashed) crashed = isCrashed();

        if (onFrom && (abs(x) - abs(laneF.toX) < height / 2 + max(border, 0.2) && abs(x) - abs(laneF.toX) > height / 2 || abs(y) - abs(laneF.toY) < height / 2 + max(border, 0.2) && abs(y) - abs(laneF.toY) > height / 2)) {
            stopped = toStopWithMain();//OnEqual();
            if (!stopped) stopped = toCrash();
            //stopped = toCrash();
        } else stopped = toCrash();

        if (stopped) {
            // Wait
        } else if (crashed) {
            System.out.println("Crashed " + this);
            System.out.println(polygon.getBoundingRectangle() + " " + carH + " " + carW + " " + x + " " + y);
            for (Lane l : cross.lanes) {
                for (Car c : l.cars) {
                    if (this != c && Intersector.overlapConvexPolygons(polygon, c.polygon)) {
                        System.out.println(c.polygon.getBoundingRectangle() + " " + c.carH + " " + c.carW + " " + c.x + " " + c.y);
                    }
                }
            }
            v = 0;
            vx = 0;
            vy = 0;
            w = 0;
        } else {
            moveC(dt);
        }
    }

    private void moveC(double dt) {
        if (abs(x) <= 1 && abs(y) <= 1) {
            if (onFrom) {
                onFrom = false;
                onCross = true;
            }
            if (r == 0) {
                x = x + vx * dt;
                y = y + vy * dt;
                polygon.translate((float) (vx * dt * cross.scale), (float) (vy * dt * cross.scale));
            } else {
                polygon.translate((float) ((Math.cos(dw + w * dt) - Math.cos(dw)) * r * cross.scale), (float) ((Math.sin(dw + w * dt) - Math.sin(dw)) * r * cross.scale));
                polygon.rotate((float) (w * dt * 180 / PI));
                x = x + (Math.cos(dw + w * dt) - Math.cos(dw)) * r;
                y = y + (Math.sin(dw + w * dt) - Math.sin(dw)) * r;
                dw += w * dt;
                if (abs(x) >= 1) {
                    if (r <= 0.5) vx = v;
                    else if (r >= 1) vx = -v;
                    vy = 0;
                    polygon.setRotation((float) ((w0 - w / abs(w) * PI / 2) * 180 / PI));
                } else if (abs(y) >= 1) {
                    if (r <= 0.5) vy = -v;
                    else if (r >= 1) vy = v;
                    vx = 0;
                    polygon.setRotation((float) ((w0 - w / abs(w) * PI / 2) * 180 / PI));
                }
            }
        } else {
            if (onCross) {
                onCross = false;
                onTo = true;
            }
            x = x + vx * dt;
            y = y + vy * dt;
            polygon.translate((float) (vx * dt * cross.scale), (float) (vy * dt * cross.scale));
        }                /*    if (r == 0.25) {
                        vx = ch * -(Math.sin(dw + w * dt)) * v;
                        vy = ch * -(Math.cos(dw + w * dt)) * v;
                        System.out.println(vx + " " + vy);
                        x = x + vx * dt;
                        y = y + vy * dt;
                        System.out.println(x + " " + y);
                    } else { */

        //    polygon.setPosition((float) ((x - width / 2) * screen / 6 + screen / 2), (float) ((y - height / 2) * screen / 6 + screen / 2));
    }

    private void moveP(double dt) {
        if (p.getX() + carW / 2 > screen / 2 - cross.scale && p.getY() + carH / 2 > screen / 2 - cross.scale && p.getX() + carW / 2 < screen / 2 + cross.scale && p.getY() + carH / 2 < screen / 2 + cross.scale && r != 0) {
            p.translate((float) ((Math.cos(dwp + w * dt) - Math.cos(dwp)) * r * cross.scale), (float) ((Math.sin(dwp + w * dt) - Math.sin(dwp)) * r * cross.scale));
            p.rotate((float) (w * dt * 180 / PI));
            dwp += w * dt;
            if (p.getX() + carW / 2 <= screen / 2 - cross.scale || p.getX() + carW / 2 >= screen / 2 + cross.scale) {
                if (r <= 0.5) vxp = v;
                else if (r >= 1) vxp = -v;
                vyp = 0;
                p.setRotation((float) ((w0 - w / abs(w) * PI / 2) * 180 / PI));
            } else if (p.getY() + carH / 2 <= screen / 2 - cross.scale || p.getY() + carH / 2 >= screen / 2 + cross.scale) {
                if (r <= 0.5) vyp = -v;
                else if (r >= 1) vyp = v;
                vxp = 0;
                p.setRotation((float) ((w0 - w / abs(w) * PI / 2) * 180 / PI));
            }
        } else {
            p.translate((float) (vxp * dt * cross.scale), (float) (vyp * dt * cross.scale));
        }
    }

    public boolean toStopOnStraight(Car c) {
        double tym;
        double txmax = (-abs(c.vx) + sqrt(c.vx * c.vx + 2 *c.fm * (abs(laneF.toX - c.x) - c.height / 2 - width / 2 - c.border - delta))) / c.fm;
        double txmin = (abs(c.vx) + sqrt(c.vx * c.vx - 2 *c.fm * (abs(laneF.toX - c.x) + c.height / 2 + width / 2))) / c.fm;
        txmax = (laneF.toX - c.x) / c.vx + (-c.height / 2 - width / 2 - c.border - delta) / abs(c.vx);
        txmin = (laneF.toX - c.x) / c.vx + (c.height / 2 + width / 2) / abs(c.vx);
    /*    if ((laneF.toX - c.x) / c.vx + (-c.height / 2 - width / 2 - c.border - delta) / abs(c.vx) < (c.laneF.toY - y) / vy + (height / 2 + c.width / 2) / abs(vy)
                && (laneF.toX - c.x) / c.vx + (c.height / 2 + width / 2) / abs(c.vx) > (c.laneF.toY - y) / vy + (-height / 2 - c.width / 2 - border - delta) / abs(vy)) */
        if (!(txmax >= ((c.laneF.toY - y) / vy + (height / 2 + c.width / 2) / abs(vy))
                || txmin <= (c.laneF.toY - y) / vy + (-height / 2 - c.width / 2 - border - delta) / abs(vy))) {
        //    if (abs(laneF.toY - y) < height / 2 + delta)
                return true;
        }
        return false;
    }

    public boolean toStopWithMain() {
        if (onFrom && (abs(x) - abs(laneF.toX) < height / 2 + max(border, 0.2) && abs(x) - abs(laneF.toX) > height / 2 || abs(y) - abs(laneF.toY) < height / 2 + max(border, 0.2) && abs(y) - abs(laneF.toY) > height / 2)) {
            if ((laneF.n + 1) % 16 == laneT.n) {
                if (vy != 0) {
                    double dt = (laneF.toY - y) / vy + PI / (2 * abs(w));
                    for (Car c : cross.lanes.get((laneF.n + 4) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneT.fromX - c.x) / c.vx + (-c.height / 2 - c.border - delta) / abs(c.v) >= dt + (height / 2) / abs(v)) // Немного примерно. Очень.
                                    || ((c.laneT.fromX - c.x) / c.vx + (c.height / 2) / abs(c.v) <= 0)) && !c.stopped) {  // Немного примерно. Очень.
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                }
                return false;
            } else if ((laneF.n + 13) % 16 == laneT.n) {
                if (vy != 0) {
                    for (Car c : cross.lanes.get((laneF.n + 4) % 16).cars) {
                        if (!c.onTo && c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                    }
                    for (Car c : cross.lanes.get((laneF.n + 6) % 16).cars) {
                        if (!c.onTo && c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                    }
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (!c.onTo) {
                            if (c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                            else if (c.r == 0.25 && c.onFrom && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 - c.border - delta) / abs(c.v) >= ((laneT.fromY - y) / vy + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + PI / (2 * abs(w)) + (c.height / 2) / abs(c.v) <= (laneT.fromY - y) / vy + (-height / 2 - border - delta) / abs(v))) && !c.stopped) {   // Нет или да
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            } // Здесь если автомобиль уже выехал на перекрёсток и поворачивает, то пока успеет и без проверки
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 14) % 16).cars) {
                        if (!c.onTo) {
                            if (c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                            else if (c.r == 1.25 && c.onFrom && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.25 - width / 2 - c.border - delta) / abs(c.v) >= ((laneT.fromY - y) / vy + (height / 2 - 0.75 + c.width / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 0.25 + width / 2) / abs(c.v) <= ((laneT.fromY - y) / vy + (-height / 2 - 0.75 - c.width / 2 - border - delta) / abs(v)))) && !c.stopped) { // Приближение!! И ещё нет
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            } // Здесь если автомобиль уже выехал на перекрёсток и поворачивает, то пока успеет и без проверки
                        }
                    }
                }
            } else if ((laneF.n + 9) % 16 == laneT.n) {
                if (vy != 0) {
                    double dt = (laneT.fromY - y) / vy;
                    for (Car c : cross.lanes.get((laneF.n + 2) % 16).cars) {
                        if (!c.onTo && c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                    }
                    for (Car c : cross.lanes.get((laneF.n + 4) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !c.stopped && toStopOnStraight(c)) return true;
                            else if (c.r == 1.25 && !((max((c.laneF.toX - c.x) / c.vx, 0) + PI / (2 * 2 * abs(c.w)) - (c.w0 - c.dw) / c.w + (-c.height / 2 - c.border - delta) / abs(c.v) >= (dt + (height / 2 - 1) / abs(v)))
                                    || (max((c.laneF.toX - c.x) / c.vx, 0) + PI / (2 * 3 / 4 * abs(c.w)) - (c.w0 - c.dw) / c.w + (c.height / 2) / abs(c.v) <= (dt + (-height / 2 - 1 - border - delta) / abs(v)))) && !c.stopped) { // Приближение!! И нет.
                                //    System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 10) % 16).cars) {
                        if (!c.onTo && c.r == 0 && !c.stopped && toStopOnStraight(c)) {
                            return true;
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !c.stopped && toStopOnStraight(c)) {
                                //    System.out.println(this + " Hx " + c);
                                return true;
                            } else if (c.r == 1.25 && !((max((c.laneF.toX - c.x) / c.vx, 0) + PI / (2 * 4 * abs(c.w)) - (c.w0 - c.dw) / c.w + (-c.height / 2) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || (max((c.laneF.toX - c.x) / c.vx, 0) + PI / (2 * abs(c.w)) - (c.w0 - c.dw) / c.w + (c.height / 2 - 0.5) / abs(c.v) <= (dt + (-height / 2 - 1.2) / abs(v)))) && !c.stopped) { // Приближение!! И нет.
                                //    System.out.println(this + " Hx " + c);
                                return true;
                            } // Здесь если автомобиль уже выехал на перекрёсток и поворачивает, то пока успеет и без проверки
                        }
                    }
                }
            } else if ((laneF.n + 5) % 16 == laneT.n) {     // Правим тут!!!!
                if (vx != 0) {
                    double dt = (laneF.toX - x) / vx + PI / (2 * abs(w));
                    for (Car c : cross.lanes.get((laneF.n + 6) % 16).cars) { // Неплохо
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v)))) && !c.stopped) {     // Примерно
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 8) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.5) / abs(c.v) >= (dt - 1 * PI / (2 * 4 * abs(w)) + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 1.5) / abs(c.v) <= (dt - PI / (2 * 2 * abs(w)) + (-height / 2) / abs(v)))) && !c.stopped) {
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                } else if (vy != 0) {
                    double dt = (laneF.toY - y) / vy + PI / (2 * abs(w));
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.85) / abs(c.v) >= (dt - PI / (2 * 2 * abs(c.w)) + (height / 2 + 0.2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 2) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v)))) && !c.stopped) {
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            } else if (c.r == 1.25 && c.onFrom && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 1) / abs(c.v) >= (dt + (height / 2 - 1) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height / 2 - 1) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 + 1) / abs(v)))) && !c.stopped) { // Приближение!!
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 6) % 16).cars) { // Неплохо
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v)))) && !c.stopped) {
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 8) % 16).cars) { // Править!!!
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1.5) / abs(c.v) <= (dt - PI / (2 * 2 * abs(w)) + (-height / 2 - 0.2) / abs(v)))) && !c.stopped) {
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 2) % 16).cars) {
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 1) / abs(c.v) >= (dt - PI / (2 * abs(w)) + (height / 2 + 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1.5) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2) / abs(v)))) && !c.stopped) {
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 4) % 16).cars) { // Примерно!!!
                        if (c.onFrom || c.onCross) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1.5) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 + 0.5) / abs(v)))) && !c.stopped) {
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            } else if (c.r == 1.25 && c.onFrom && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + PI / (2 * 2 * abs(c.w)) + (c.height / 2 + 0.2) / abs(c.v) <= (dt - PI / (2 * 2 * abs(w)) + (-height / 2 - 0.2) / abs(v)))) && !c.stopped) { // Приближение!!
                                //    System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } else return false;
    }

    private boolean toChangeSpeed() {
        if (onFrom && (abs(x) - abs(laneF.toX) < height / 2 + max(border, 0.2) && abs(x) - abs(laneF.toX) > height / 2 || abs(y) - abs(laneF.toY) < height / 2 + max(border, 0.2) && abs(y) - abs(laneF.toY) > height / 2)) {
            for (Car c : laneF.cars) {
                if (vx != 0 && !c.stopped) {
                    if (this != c && laneT == c.laneT && (c.onCross) && abs(vx) > abs(c.vx) && (c.laneT.fromX - c.x) / c.vx > (laneT.fromX - x) / vx - (height + c.height + border + delta) / (2 * abs(v))) {
                        vx = (laneT.fromX - x) * c.vx / (c.laneT.fromX - c.x);
                    //    vx = c.vx;
                        v = vx;
                        System.out.println("Changed " + this + " " + vx + " " + vy);
                        vxp = vx;
                    }
                } else if (!c.stopped){
                    if (this != c && laneT == c.laneT && (c.onCross) && abs(vy) > abs(c.vy) && (c.laneT.fromY - c.y) / c.vy > (laneT.fromY - y) / vy - (height + c.height + border + delta) / (2 * abs(v))) {
                        vy = (laneT.fromY - y) * c.vy / (c.laneT.fromY - c.y);
                    //    vy = c.vy;
                        v = vy;
                        System.out.println("Changed " + this + " " + vx + " " + vy);
                        vyp = vy;
                    }
                }
            }
        } else if (onTo) {
        /*    for (Car c : laneF.cars) {
                if (this != c && laneT == c.laneT && abs(v) > abs(c.v) && c.onTo && max(abs(c.laneT.toX - c.x), abs(c.laneT.toY - c.y)) / abs(c.v) < max(abs(laneT.toX - x), abs(laneT.toY - y)) / (abs(v) - abs(c.v))) {
                //    System.out.println("Changed " + this + " " + v + " " + c.v);
                    v = c.v;
                    vx = c.vx;
                    vy = c.vy;
                    vxp = vx;
                    vyp = vy;
                }
            }*/
        }
        return false;
    }

    private boolean toCrash() {
        boolean toCrash = false;
        for (Lane l : cross.lanes) {
            for (Car c : l.cars) {
                if (this != c && Intersector.overlapConvexPolygons(p, c.polygon)) {
                    if (min(abs(x - c.x), abs(y - c.y)) != 0) System.out.println("To crash " + this + " " + c);
                    if (abs(c.v) < abs(v) && (onCross && (c.laneF == laneF || c.laneT == laneT) || onFrom && c.laneF == laneF || onTo && c.laneT == laneT)) {
                        v = c.v;
                        vx = c.vx;
                        vy = c.vy;
                        vxp = vx;
                        vyp = vy;
                        System.out.println("Now slow " + laneT);
                    } else return true;
                    toCrash = true;
                    if (c.stopped || c.crashed) return true;
                }
                if (this != c && (onCross && c.onCross && (c.laneF == laneF || c.laneT == laneT) || onFrom && c.onFrom && c.laneF == laneF || onTo && c.onTo && c.laneT == laneT) &&
                        (abs(x - c.x) < laneF.width / 2 && (laneT.toY > 0 && c.y - y > 0 && c.y - y < (c.height / 2 + height / 2 + border + delta) || laneT.toY < 0 && c.y - y > 0 && y - c.y < (c.height / 2 + height / 2 + border + delta))
                        || abs(y - c.y) < laneF.width / 2 && (laneT.toX > 0 && c.x - x > 0 && c.x - x < (c.height / 2 + height / 2 + border + delta) || laneT.toX < 0 && c.x - x > 0 && x - c.x < (c.height / 2 + height / 2 + border + delta)))) {
                    v = c.v;
                    vx = c.vx;
                    vy = c.vy;
                    vxp = vx;
                    vyp = vy;
                    toCrash = true;
                }
            }
        }
        if (!toCrash && abs(v) < carConfig.carV / (3.6 * 7)) {
            if (vx == 0) {
                this.vy = (laneF.toY - laneF.fromY) / laneF.length * carConfig.carV / (3.6 * 7); // Единицы в секунду
                v = vy;
            }
            else {
                this.vx = (laneF.toX - laneF.fromX) / laneF.length * carConfig.carV / (3.6 * 7);
                v = vx;
            }
            vxp = vx;
            vyp = vy;
        }
        return false;
    }

    private boolean isCrashed() {
        for (Lane l : cross.lanes) {
            for (Car c : l.cars) {
                if (this != c && Intersector.overlapConvexPolygons(polygon, c.polygon)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Car{" +
                "laneF=" + laneF +
                ", laneT=" + laneT +
                '}';
    }

    public boolean toStopOnEqual() {
        if (onFrom && (abs(x) - abs(laneF.toX) < height / 2 + 0.2 && abs(x) - abs(laneF.toX) > height / 2 || abs(y) - abs(laneF.toY) < height / 2 + 0.2 && abs(y) - abs(laneF.toY) > height / 2)) {
            if ((laneF.n + 1) % 16 == laneT.n) {
                return false;
            } else if ((laneF.n + 13) % 16 == laneT.n) {
                if (vx != 0) {
                    double dt = (laneT.fromX - x) / vx;
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 0.5) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hx " + c);
                                return true;
                            } else if (c.r == 0.25 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 0.5) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) { // Приближени!!
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 14) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 0.5) / abs(c.v) <= (dt + (-height / 2 - 1) / abs(v))))) {
                                System.out.println(this + " Hx " + c);
                                return true;
                            } else if (c.r == 1.25 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 0.5) / abs(c.v) <= (dt + (-height / 2 - 1) / abs(v))))) { // Приближение!!
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                } else if (vy != 0) {
                    double dt = (laneT.fromY - y) / vy;
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 0.5) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            } else if (c.r == 0.25 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 0.5) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 14) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 0.5) / abs(c.v) <= (dt + (-height / 2 - 1) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            } else if (c.r == 1.25 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 0.5) / abs(c.v) <= (dt + (-height / 2 - 1) / abs(v))))) { // Приближение!!
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                }
            } else if ((laneF.n + 9) % 16 == laneT.n) {
                if (vx != 0) {
                    double dt = (laneT.fromX - x) / vx;
                    for (Car c : cross.lanes.get((laneF.n + 10) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - 1) / abs(v))))) {
                                System.out.println(this + " Hx " + c);
                                return true;
                            } else if (c.r == 1.25 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + PI / (2 * 2 * abs(c.w)) + (c.height / 2) / abs(c.v) <= (dt + (-height / 2 - 1.2) / abs(v))))) { // Приближение!!
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                } else if (vy != 0) {
                    double dt = (laneT.fromY - y) / vy;
                    for (Car c : cross.lanes.get((laneF.n + 10) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - 1) / abs(v))))) {
                                System.out.println(this + " Hx " + c);
                                return true;
                            } else if (c.r == 1.25 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + PI / (2 * 2 * abs(c.w)) + (c.height / 2) / abs(c.v) <= (dt + (-height / 2 - 1.2) / abs(v))))) { // Приближение!!
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                }
            } else if ((laneF.n + 5) % 16 == laneT.n) { // Всё в приближении!!!
                if (vx != 0) {
                    double dt = (laneF.toX - x) / vx + PI / (2 * abs(w));
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.85) / abs(c.v) >= (dt - PI / (2 * 2 * abs(c.w)) + (height / 2 + 0.2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 2) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            } else if (c.r == 1.25 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 1) / abs(c.v) >= (dt + (height / 2 - 1) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + (c.height / 2 - 1) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 + 1) / abs(v))))) { // Приближение!!
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 6) % 16).cars) { // Неплохо
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 8) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toX - c.x) / c.vx + (c.height / 2 + 1) / abs(c.v) <= (dt - PI / (2 * 2 * abs(w)) + (-height / 2 - 0.2) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            } /*else if (c.r == 1.25 && !(((c.laneF.toX - c.x) / c.vx + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                || ((c.laneF.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + (c.height / 2 - 0.5) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 + 0.5) / abs(v))))) { // Приближение!!
                            System.out.println(this + " Hy " + c);
                            return true;
                        }*/
                        }
                    }
                } else if (vy != 0) {
                    double dt = (laneF.toY - y) / vy + PI / (2 * abs(w));
                    for (Car c : cross.lanes.get((laneF.n + 12) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.85) / abs(c.v) >= (dt - PI / (2 * 2 * abs(c.w)) + (height / 2 + 0.2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 2) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            } else if (c.r == 1.25 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 1) / abs(c.v) >= (dt + (height / 2 - 1) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height / 2 - 1) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 + 1) / abs(v))))) { // Приближение!!
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 6) % 16).cars) { // Неплохо
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1) / abs(c.v) <= (dt + (-height / 2 - 0.5) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                    for (Car c : cross.lanes.get((laneF.n + 8) % 16).cars) {
                        if (c.onFrom) {
                            if (c.r == 0 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                    || ((c.laneF.toY - c.y) / c.vy + (c.height / 2 + 1) / abs(c.v) <= (dt - PI / (2 * 2 * abs(w)) + (-height / 2 - 0.2) / abs(v))))) {
                                System.out.println(this + " Hy " + c);
                                return true;
                            } /*else if (c.r == 1.25 && !(((c.laneF.toY - c.y) / c.vy + (-c.height / 2 + 0.5) / abs(c.v) >= (dt + (height / 2 - 0.5) / abs(v)))
                                || ((c.laneF.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height / 2 - 0.5) / abs(c.v) <= (dt - PI / (2 * abs(w)) + (-height / 2 + 0.5) / abs(v))))) { // Приближение!!
                            System.out.println(this + " Hy " + c);
                            return true;
                        }*/
                        }
                    }
                }
            }
            return false;
        } else return false;
    }
}