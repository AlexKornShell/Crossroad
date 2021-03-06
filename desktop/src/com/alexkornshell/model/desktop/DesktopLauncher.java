package com.alexkornshell.model.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.alexkornshell.model.CrossroadModel;

public class DesktopLauncher {
    public static void main(String[] arg) {
        int screen = 756;
        int controls = 200;
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = screen + controls;
        config.height = screen;
        config.forceExit = false;

        new LwjglApplication(new CrossroadModel(screen, controls), config);
    }
}
