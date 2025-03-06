package com.punkipunk.jade;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * <a href="https://www.glfw.org/docs/latest/input_guide.html">GLFW Input guide</a>
 */

public class MouseListener {

    private double scrollX, scrollY;
    private double xPos, yPos, lastY, lastX;
    private final boolean[] mouseButtonPressed = new boolean[3];
    private boolean isDragging;

    /**
     * Es importante inicializar estos valores ya que el primer frame puede tener algunos errores extraños si estos valores no
     * estan inicializados.
     */
    private MouseListener() {
        this.scrollX = 0.0;
        this.scrollY = 0.0;
        this.xPos = 0.0;
        this.yPos = 0.0;
        this.lastY = 0.0;
        this.lastX = 0.0;
    }

    public static void mousePosCallback(long window, double xpos, double ypos) {
        get().lastX = get().xPos;
        get().lastY = get().yPos;
        get().xPos = xpos;
        get().yPos = ypos;
        // Si uno de los botones del mouse se esta presionando y el mouse se acaba de mover, entonces el usuario esta arrastrando algo
        get().isDragging = get().mouseButtonPressed[0] || get().mouseButtonPressed[1] || get().mouseButtonPressed[2];
    }

    public static void mouesButtonCallback(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS) {
            // Si el indice del boton es menor a la cantidad de botones que tiene el mouse (en caso de que sea un mouse gamer)
            if (button < get().mouseButtonPressed.length) get().mouseButtonPressed[button] = true;
        } else if (action == GLFW_RELEASE) {
            if (button < get().mouseButtonPressed.length) {
                get().mouseButtonPressed[button] = false;
                get().isDragging = false;
            }
        }
    }

    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        get().scrollX = xOffset;
        get().scrollY = yOffset;
    }

    public static void endFrame() {
        get().scrollX = 0;
        get().scrollY = 0;
        get().lastX = get().xPos;
        get().lastY = get().yPos;
    }

    public static float getX() {
        return (float) get().xPos;
    }

    public static float getY() {
        return (float) get().yPos;
    }

    public static float getDx() {
        return (float) (get().lastX - get().xPos);
    }

    public static float getDy() {
        return (float) (get().lastY - get().yPos);
    }

    public static float getScrollX() {
        return (float) get().scrollX;
    }

    public static float getScrollY() {
        return (float) get().scrollY;
    }

    public static boolean isDraggin() {
        return get().isDragging;
    }

    public static boolean mouseButtonDown(int button) {
        return button < get().mouseButtonPressed.length && get().mouseButtonPressed[button];
    }

    public static MouseListener get() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final MouseListener INSTANCE = new MouseListener();
    }

}
