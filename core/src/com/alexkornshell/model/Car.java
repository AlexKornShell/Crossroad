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
    double tn;
    double dwp;
    double vxp;
    double vyp;

    public Car(Crossroad crossroad, Lane laneFrom, Lane laneTo, double paramV) {

        this.cross = crossroad;
        this.screen = crossroad.screen;
        carW = 24;
        carH = 53;
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

        p = new Polygon(new float[]{0, 0, 0, carH, carW, carH, carW, 0});
        p.setOrigin(carW / 2, carH / 2);
        p.setPosition((float) ((x - width / 2) * screen / 6 + screen / 2), (float) ((y - height / 2) * screen / 6 + screen / 2));
        p.setRotation(polygon.getRotation());

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

        dwp = dw;
        vxp = vx;
        vyp = vy;

    }

    public Car move(float dt) {
        if (!stopped && !crashed) {
            if (tn < 10) {
                t += dt;
                tn++;
            } else if (tn == 10) {
                p.translate((float) (vxp * 4 * t * screen / 6), (float) (vyp * 4 * t * screen / 6));
                tn++;
            } else {

                if (p.getX() + carW / 2 > 170 && p.getY() + carH / 2 > 170 && p.getX() + carW / 2 < 342 && p.getY() + carH / 2 < 342) {
                    if (r == 0) {
                        p.translate((float) (vxp * dt * screen / 6), (float) (vyp * dt * screen / 6));
                    } else {
                        p.translate((float) ((Math.cos(dwp + w * dt) - Math.cos(dwp)) * r * screen / 6), (float) ((Math.sin(dwp + w * dt) - Math.sin(dwp)) * r * screen / 6));
                        p.rotate((float) (w * dt * 180 / PI));
                        dwp += w * dt;
                        if (p.getX() + carW / 2 <= 170 || p.getX() + carW / 2 >= 342) {
                            if (r == 0.25) vxp = v;
                            else if (r == 1.25) vxp = -v;
                            vyp = 0;
                        } else if (p.getY() + carH / 2 <= 170 || p.getY() + carH / 2 >= 342) {
                            if (r == 0.25) vyp = -v;
                            else if (r == 1.25) vyp = v;
                            vxp = 0;
                        }
                    }
                } else {
                    p.translate((float) (vxp * dt * screen / 6), (float) (vyp * dt * screen / 6));
                }



             /*   if (p.getX() > 170 && p.getY() > 170 && p.getX() < 342 && p.getY() < 342 && r != 0) {
                    p.translate((float) ((Math.cos(dw + w * dt) - Math.cos(dw)) * r * screen / 6), (float) ((Math.sin(dw + w * dt) - Math.sin(dw)) * r * screen / 6));
                    p.rotate((float) (w * dt * 180 / PI));
                } else p.translate((float) (vx * dt * screen / 6), (float) (vy * dt * screen / 6)); */
            }
        }

        stopped = toCrash();
        if (!crashed) crashed = isCrashed();

        if (stopped) {
            // Wait
        } else if (crashed) {
            System.out.println(this);
            v = 0;
            vx = 0;
            vy = 0;
            w = 0;
        } else {

            if (abs(x) <= 1 && abs(y) <= 1) {
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
                if (onFrom) {
                    onFrom = false;
                    onCross = true;
                    //    System.out.println(p.getX() + " " + p.getY());
                }
            } else {
                x = x + vx * dt;
                y = y + vy * dt;
                polygon.translate((float) (vx * dt * screen / 6), (float) (vy * dt * screen / 6));
                if (onCross) {
                    onCross = false;
                    onTo = true;
                }
            }
                /*    if (r == 0.25) {
                        vx = ch * -(Math.sin(dw + w * dt)) * v;
                        vy = ch * -(Math.cos(dw + w * dt)) * v;
                        System.out.println(vx + " " + vy);
                        x = x + vx * dt;
                        y = y + vy * dt;
                        System.out.println(x + " " + y);
                    } else { */

            //    polygon.setPosition((float) ((x - width / 2) * screen / 6 + screen / 2), (float) ((y - height / 2) * screen / 6 + screen / 2));
        }

        return this;
    }

    public boolean toStop() {

        if ((laneFrom.n + 1) % 16 == laneTo.n) {
            for (Car c : laneFrom.cars) {

            }
            return false;
        } else if ((laneFrom.n + 13) % 16 == laneTo.n) {
            if (abs(x) - abs(laneFrom.toX) < height / 2 + 0.1 && abs(x) - abs(laneFrom.toX) > 0 && abs(x - laneFrom.toX) < 1) {
                double dt = (laneTo.fromX - x) / vx;
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (!c.crashed && !c.stopped) {

                        if (c.r == 0) {
                            if ((c.laneFrom.toY - c.y) / c.vy + (c.height + 0.1) / abs(v) > dt) {
                                stopped = true;
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        } else if (c.r == 0.25) {
                            if ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height + 0.1) / abs(c.v) > dt) {
                                stopped = true;
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 14) % 16).cars) {
                    if (!c.crashed && !c.stopped) {
                        if (c.r == 0) {
                            if ((c.laneFrom.toY - c.y) / c.vy + (c.height + 0.1) / abs(c.v) > dt) {
                                stopped = true;
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        } else if (c.r == 0.25) {
                            if ((c.laneFrom.toY - c.y) / c.vy + PI / (2 * abs(c.w)) + (c.height + 0.1) / abs(v) > dt) {
                                stopped = true;
                                System.out.println(this + " Hx " + c);
                                return true;
                            }
                        }
                    }
                }
            } else if (abs(y) - abs(laneFrom.toY) < height / 2 + 0.1 && abs(y) - abs(laneFrom.toY) > 0 && abs(y - laneFrom.toY) < 1) {
                double dt = (laneTo.fromY - y) / vy;
                for (Car c : cross.lanes.get((laneFrom.n + 12) % 16).cars) {
                    if (!c.crashed && !c.stopped) {
                        if (c.r == 0) {
                            if ((c.laneFrom.toX - c.x) / c.vx + (c.height + 0.1) / abs(v) > dt) {
                                stopped = true;
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        } else if (c.r == 0.25) {
                            System.out.println(c.w);
                            System.out.println((c.laneFrom.toX - c.x) / c.vx + " " + PI / (2 * abs(c.w)) + " " + height / abs(v));
                            System.out.println((c.laneFrom.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + height / abs(v) + " Hx " + dt);
                            if ((c.laneFrom.toX - c.x) / c.vx + PI / (2 * abs(c.w)) + (c.height + 0.1) / abs(c.v) > dt) {
                                stopped = true;
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                }
                for (Car c : cross.lanes.get((laneFrom.n + 14) % 16).cars) {
                    if (!c.crashed && !c.stopped) {
                        if (c.r == 0) {
                            if ((c.laneFrom.toX - c.x) / vx + (c.height + 0.1) / abs(v) > dt) {
                                stopped = true;
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        } else if (c.r == 0.25) {
                            if ((c.laneFrom.toX - c.x) / vx + PI / (2 * abs(c.w)) + (c.height + 0.1) / abs(v) > dt) {
                                stopped = true;
                                System.out.println(this + " Hy " + c);
                                return true;
                            }
                        }
                    }
                }
            }
        }




    /*    if (abs(x) - abs(laneFrom.toX) < height / 2 + 0.1 && abs(x) - abs(laneFrom.toX) > 0) {
            double dt = PI / (2 * w) + (laneFrom.toX - x) / v;
            if (r == 0.25) {
                for (Car c : cros.lanes.get((laneFrom.n + 4) % 16).cars) {
                    if (!c.crashed && !stopped) {
                        if (c.r != 0.25 && c.r != 1.25) {
                            if (c.vx == 0) {
                                System.out.println(this + " Hx " + c);
                                System.out.println(c.laneFrom.cars);
                                if (abs(c.y + c.vy * dt) >= abs(y) + 0.25 - height) {
                                    stopped = true;
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else if (r == 1.25);
            else {

            }
        } else if (abs(y) - abs(laneFrom.toY) < height / 2 + 0.1 && abs(y) - abs(laneFrom.toY) > 0) {
            double dt = PI / (2 * w) + (laneFrom.toY - y) / v;
            if (r == 0.25) {
                for (Car c : cros.lanes.get((laneFrom.n + 4) % 16).cars) {
                    if (!c.crashed && !stopped) {
                        if (c.r != 0.25 && c.r != 1.25) {
                            if (c.vy == 0) {
                                System.out.println(this + " Hy " + c);
                                System.out.println(c.laneFrom.cars);
                                if (abs(c.x + c.vx * dt) >= abs(x) + 0.25 - height) {
                                    stopped = true;
                                    return true;
                                }
                            }
                        }
                    }
                }
            } else if (r == 1.25);
            else {

            }
        } */
        stopped = false;
        return false;
    }

    public boolean toCrash() {
        for (Lane l : cross.lanes) {
            for (Car c : l.cars) {
                if (this != c && Intersector.overlapConvexPolygons(p, c.polygon)) return true;
            }
        }
        return false;
    }

    public boolean isCrashed() {
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
