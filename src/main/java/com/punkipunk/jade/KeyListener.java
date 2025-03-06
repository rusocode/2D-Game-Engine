package com.punkipunk.jade;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {

    private final boolean[] keyPressed = new boolean[350];

    private KeyListener() {

    }

    /**
     * @param window   ventana en donde se detectan las entradas de tecla
     * @param key      tecla presionada
     * @param scancode ?
     * @param action   GLFW_PRESS, GLFW_REPEAT o GLFW_RELEASE
     * @param mods     tecla adicional
     */
    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) get().keyPressed[key] = true;
        else if (action == GLFW_RELEASE) get().keyPressed[key] = false;
    }

    public static boolean isKeyPressed(int keyCode) {
        return get().keyPressed[keyCode];
    }

    public static KeyListener get() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final KeyListener INSTANCE = new KeyListener();
    }

}
