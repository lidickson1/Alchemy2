package main.buttons;

import main.Main;

public abstract class Arrow extends Button {

    public static final int SIZE = 32;
    public static final String LEFT = "resources/images/arrow_left.png";
    public static final String RIGHT = "resources/images/arrow_right.png";
    public static final String UP = "resources/images/arrow_up.png";
    public static final String DOWN = "resources/images/arrow_down.png";

    public Arrow(String path) {
        super(SIZE, SIZE, path);
    }

    @Override
    public void draw(float x, float y) {
        this.setX(x);
        this.setY(y);

        if (this.canDraw()) {
            this.drawButton();

            if (this.inBounds()) {
                Main.INSTANCE.noStroke();
                Main.INSTANCE.fill(255, 80);
                Main.INSTANCE.rect(x, y, this.getWidth(), this.getHeight(), 6);
            }
        }
    }

    protected abstract boolean canDraw();

}
