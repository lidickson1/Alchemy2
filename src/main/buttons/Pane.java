package main.buttons;

import main.Main;
import processing.core.PConstants;

public abstract class Pane extends Button {

    private boolean active;

    protected Pane() {
        super(400, 200, "resources/images/pane.png");
    }

    @Override
    public final void draw(float x, float y) {
        this.setX(x);
        this.setY(y);

        this.drawButton();

        //no overlay if mouse in bounds
    }

    @Override
    protected void drawButton() {
        Main.INSTANCE.image(this.getImage(), this.getX(), this.getY());

        Main.INSTANCE.fill(255);
        Main.INSTANCE.textAlign(PConstants.CENTER, PConstants.CENTER);
        Main.INSTANCE.textSize(20);
        Main.INSTANCE.text(this.getText(), Main.INSTANCE.screenWidth / 2F, this.getY() + 30);
    }

    protected abstract String getText();

    @Override
    public void mousePressed() {
        //overridden because it should play no sound
        if (this.inBounds()) {
            this.clicked();
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void clicked() {
        this.active = false;
    }

}
