package com.punkipunk;

import com.punkipunk.jade.Window;

/**
 * Al trabajar en el renderizado en un nivel bajo, nos da una ventaja mucho mejor en terminos de velocidad.
 */

public class Launcher {

    public static void main(String[] args) {
        Window window = Window.getInstance();
        window.run();
    }

}