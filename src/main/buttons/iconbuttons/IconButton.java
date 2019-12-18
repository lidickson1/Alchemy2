package main.buttons.iconbuttons;

import main.buttons.Button;
import main.buttons.Group;

public class IconButton extends Button {

    public static final int SIZE = 80;
    public static final int GAP = Group.GAP;

    public IconButton(String path) {
        super(SIZE, SIZE, path);
    }

    @Override
    public void draw(float x, float y) {
        this.setX(x);
        this.setY(y);

        this.drawButton();

        if (this.inBounds()) {
            main.noStroke();
            if (this.isDisabled()) {
                main.fill(255, 0, 0, 80);
            } else {
                main.fill(255, 80);
            }
            main.rect(x, y, this.getWidth(), this.getHeight(), 15);
        }
    }

}
