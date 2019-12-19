package main.rooms;

import main.Entity;
import processing.core.PConstants;
import processing.core.PImage;

public abstract class Room extends Entity {

    static PImage background;

    Room() {
        if (background == null) {
            background = main.loadImage("resources/images/background.png");
        }
    }

    void drawTitle(String section, String key) {
        main.noStroke();
        main.fill(0);
        main.rect(0, 0, main.screenWidth, main.screenHeight);

        main.textSize(40);
        main.textAlign(PConstants.CENTER, PConstants.CENTER);
        main.fill(255);
        main.text(main.getLanguageSelected().getLocalizedString(section, key), main.screenWidth / 2F, 60);
    }

    public abstract void setup();

    public abstract void draw();

    public void end() {

    }

    public void mousePressed() {

    }

    public void keyPressed() {

    }

}
