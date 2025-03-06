package com.punkipunk.jade;

public class LevelScene extends Scene{

    public LevelScene() {
        System.out.println("Inside level scene");
        Window.getInstance().r = 1;
        Window.getInstance().g = 1;
        Window.getInstance().b = 1;
    }

    @Override
    public void update(double dt) {

    }

}
