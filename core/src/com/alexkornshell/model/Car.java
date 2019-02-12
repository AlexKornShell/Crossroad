package com.alexkornshell.model;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;

import static java.lang.Math.*;

public class Car {
    Polygon polygon;
    Polygon p;

    Crossroad cross;
    int screen;
    int carW;
    int carH;

    double width;
    double height;

    Lane laneFrom;
    Lane laneTo;

    boolean onFrom;
    boolean onCross;
    boolean onTo;

    boolean crashed;
    boolean stopped;

    double x;
    double y;
    double vx;
    double vy;
    double v;

    double w;
    double r;
    double dw;

    int ch;
    double t;
    double dwp;
    double vxp;
    double vyp;

    public Car(Crossroad crossroad, Lane laneFrom, Lane laneTo, double paramV) {

        this.cross = crossroad;
        this.screen = crossroad.screen;
        carW = 24;
        carH = 53;
        t = 0.16;
        this.width = (double) carW * 6 / screen;
        this.height = (double) carH * 6 / screen;
        this.laneFrom = laneFrom;
        this.laneTo = laneTo;
        this.onFrom = true;

        this.vx = (laneFrom.toX - laneFrom.fromX) / paramV;
        this.vy = (laneFrom.toY - laneFrom.fromY) / paramV;

        polygon = new Polygon(new float[]{0, 0, 0, carH, carW, carH, carW, 0});
        polygon.setOrigin(carW / 2, carH / 2);

        if (vy > 0) {
            polygon.setRotation(0);
            this.v = vy;
            this.x = laneFrom.fromX;
            this.y = laneFrom.fromY - height / 2;
        } else if (vy < 0) {
            polygon.setRotation(180);
            this.v = vy;
            this.x = laneFrom.fromX;
            this.y = laneFrom.fromY + height / 2;
        } else if (vx > 0) {
            polygon.setRotation(270);
            this.v = vx;
            this.x = laneFrom.fromX - height / 2;
            this.y = laneFrom.fromY;
        } else if (vx < 0) {
            polygon.setRotation(90);
            this.v = vx;
            this.x = laneFrom.fromX + height / 2;
            this.y = laneFrom.fromY;
        }
        polygon.setPosition((float) ((x - width / 2) * screen / 6 + screen / 2), (float) ((y - height / 2) * screen / 6 + screen / 2));

        if (!((laneFrom.n + laneTo.n) % 16 == 1 || (laneFrom.n + laneTo.n) % 16 == 9)) {
            if (laneFrom.n % 4 == 0) {
                r = 1.25;
                w = abs(v) / r;
            } else if (laneFrom.n % 2 == 0) {
                r = 0.25;
                w = -abs(v) / r;
            }
        }

        if (laneFrom.n == 2 || laneFrom.n == 12) {
            ch = -1;
            dw = -PI / 2;
        } else if (laneFrom.n == 14 || laneFrom.n == 8) {
            ch = 1;
            dw = 0;
        } else if (laneFrom.n == 10 || laneFrom.n == 4) {
            ch = -1;
            dw = PI / 2;
        } else if (laneFrom.n == 6 || laneFrom.n == 0) {
            ch = 1;
            dw = PI;
        }

        p = new Polygon(new float[]{0, 0, 0, carH, carW, carH, carW, 0});
        p.setOrigin(carW / 2, carH / 2);
        p.setPosition((float) ((x - width / 2) * screen / 6 + screen / 2), (float) ((y - height / 2) * screen / 6 + screen / 2));
        p.setRotation(polygon.getRotation());

        dwp = dw;
        vxp = vx;
        vyp = vy;

        p.translate((float) (vxp * 3 * t * screen / 6), (float) (vyp * 3 * t * screen / 6));

    }

    public Car move(float dt) {

        if (!stopped && !crashed) {
            moveP(dt);
        }

        if (!crashed) crashed = isCrashed();

        if (onFrom && (abs(x) - abs(laneFrom.toX) < height / 2 + 0.2 && abs(x) - abs(laneFrom.toX) > 0 || abs(y) - abs(laneFrom.toY) < height / 2 + 0.2 && abs(y) - abs(laneFrom.toY) > 0)) {
            stopped = toStop();
            if (!stopped) stopped = toCrash();
        } else {
            stopped = toCrash();
        }

        if (stopped) {
            // Wait
        } else if (crashed) {
            System.out.println("Crashed " + this);
            v = 0;
            vx = 0;
            vy = 0;
            w = 0;
        } else {
            moveC(dt);
        }

        return this;
    }

    public boolean toStop() {
        if ((laneFrom.n + 1) % 16 == laneTo.n) {
            if (vy != 0) {
                double dt = (laneFrom.toY - y) / vy + PI / (2 * abs(w));
                for (Car c : cross.lanes.get((laneFrom.n + 4) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneTo.fromX - c.x) / c.vx + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            }
            return false;
        } else if ((laneFrom.n + 13) % 16 == laneTo.n) {
            if (vx != 0) {
            /*    double dt = (laneTo.fromX - x) / vx;
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        } else if (c.r == 0.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 14) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (c.height + 0.2) / abs(c.v) > dt)) {
                                System.out.println(this + " Hx " + c);
                                return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * 4 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                                System.out.println(this + " Hx " + c);
                                return true;
                        }
                    }
                }*/
            } else if (vy != 0) {
                double dt = (laneTo.fromY - y) / vy;
                for (Car c : cross.lanes.get((laneFrom.n + 4) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneTo.fromX - c.x) / c.vx + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 6) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * 2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 0.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 14) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / vx + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * 4 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            }
        } else if ((laneFrom.n + 9) % 16 == laneTo.n) {
            if (vx != 0) {
            /*    double dt = (laneTo.fromX - x) / vx;
                for (Car c : cross.lanes.get((laneFrom.n + 10) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * 2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        }
                    }
                }*/
            } else if (vy != 0) {
                double dt = (laneTo.fromY - y) / vy;
                for (Car c : cross.lanes.get((laneFrom.n + 10) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * 2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            }
        } else if ((laneFrom.n + 5) % 16 == laneTo.n) {
            if (vx != 0) {
                double dt = (laneFrom.toX - x) / vx + PI / (2 * abs(w));
            /*    for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneTo.fromY - c.y) / c.vy + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }*/
                for (Car c : cross.lanes.get((laneFrom.n + 6) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 8) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (1 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * abs(c.w)) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            } else if (vy != 0) {
                double dt = (laneFrom.toY - y) / vy + PI / (2 * abs(w));
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneTo.fromX - c.x) / c.vx + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 6) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 8) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (1 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * abs(c.w)) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
                polygon.translate((float) (vx * dt * screen / 6), (float) (vy * dt * screen / 6));
            } else {
                x = x + (Math.cos(dw + w * dt) - Math.cos(dw)) * r;
                y = y + (Math.sin(dw + w * dt) - Math.sin(dw)) * r;
                dw += w * dt;
                if (abs(x) >= 1) {
                    if (r == 0.25) vx = v;
                    else if (r == 1.25) vx = -v;
                    vy = 0;
                } else if (abs(y) >= 1) {
                    if (r == 0.25) vy = -v;
                    else if (r == 1.25) vy = v;
                    vx = 0;
                }
                polygon.translate((float) ((Math.cos(dw + w * dt) - Math.cos(dw)) * r * screen / 6), (float) ((Math.sin(dw + w * dt) - Math.sin(dw)) * r * screen / 6));
                polygon.rotate((float) (w * dt * 180 / PI));
            }
        } else {
            if (onCross) {
                onCross = false;
                onTo = true;
            }
            x = x + vx * dt;
            y = y + vy * dt;
            polygon.translate((float) (vx * dt * screen / 6), (float) (vy * dt * screen / 6));
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
        if (p.getX() + carW / 2 > 170 && p.getY() + carH / 2 > screen / 3 && p.getX() + carW / 2 < 342 && p.getY() + carH / 2 < 2 * screen / 3 && r != 0) {
            p.translate((float) ((Math.cos(dwp + w * dt) - Math.cos(dwp)) * r * screen / 6), (float) ((Math.sin(dwp + w * dt) - Math.sin(dwp)) * r * screen / 6));
            p.rotate((float) (w * dt * 180 / PI));
            dwp += w * dt;
            if (p.getX() + carW / 2 <= screen / 3 || p.getX() + carW / 2 >= 2 * screen / 3) {
                if (r == 0.25) vxp = v;
                else if (r == 1.25) vxp = -v;
                vyp = 0;
            } else if (p.getY() + carH / 2 <= screen / 3 || p.getY() + carH / 2 >= 2 * screen / 3) {
                if (r == 0.25) vyp = -v;
                else if (r == 1.25) vyp = v;
                vxp = 0;
            }
        } else {
            p.translate((float) (vxp * dt * screen / 6), (float) (vyp * dt * screen / 6));
        }
    }

    public boolean toStopOnEqual() {
        if ((laneFrom.n + 1) % 16 == laneTo.n) {
            return false;
        } else if ((laneFrom.n + 13) % 16 == laneTo.n) {
            if (vx != 0) {
                double dt = (laneTo.fromX - x) / vx;
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        } else if (c.r == 0.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 14) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * 4 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        }
                    }
                }
            } else if (vy != 0) {
                double dt = (laneTo.fromY - y) / vy;
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 0.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 14) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / vx + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * 4 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            }
        } else if ((laneFrom.n + 9) % 16 == laneTo.n) {
            if (vx != 0) {
                double dt = (laneTo.fromX - x) / vx;
                for (Car c : cross.lanes.get((laneFrom.n + 10) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * 2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hx " + c);
                            return true;
                        }
                    }
                }
            } else if (vy != 0) {
                double dt = (laneTo.fromY - y) / vy;
                for (Car c : cross.lanes.get((laneFrom.n + 10) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * 2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            }
        } else if ((laneFrom.n + 5) % 16 == laneTo.n) {
            if (vx != 0) {
                double dt = (laneFrom.toX - x) / vx + PI / (2 * abs(w));
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneTo.fromY - c.y) / c.vy + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 6) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 8) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toX - c.x) / c.vx + (1 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * abs(c.w)) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            } else if (vy != 0) {
                double dt = (laneFrom.toY - y) / vy + PI / (2 * abs(w));
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneTo.fromX - c.x) / c.vx + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + (c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 6) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (0.5 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 8) % 16).cars) {
                    if (c.onFrom) {
                        if (c.r == 0 && ((c.laneFrom.toY - c.y) / c.vy + (1 + c.height + 0.2) / abs(c.v) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        } else if (c.r == 1.25 && ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * abs(c.w)) > dt)) {
                            System.out.println(this + " Hy " + c);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean toCrash() {
        for (Lane l : cross.lanes) {
            for (Car c : l.cars) {
                if (this != c && Intersector.overlapConvexPolygons(p, c.polygon)) return true;
            }
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
                "laneFrom=" + laneFrom +
                ", laneTo=" + laneTo +
                '}';
    }
}