package main.buttons.iconbuttons;

import processing.core.PImage;

public class ToggleButton extends IconButton {

    private boolean toggled = true;
    private PImage offImage;

    public ToggleButton(String path) {
        super(path);

        //TODO: this is a rough implementation
        this.offImage = main.loadImage(path.replace(".png", "_off.png"));
        this.offImage.resize(this.getWidth(), this.getHeight());
    }

    @Override
    protected void drawButton() {
        main.image(this.toggled ? this.getImage() : this.offImage, this.getX(), this.getY());
    }

    @Override
    public void clicked() {
        this.toggled = !this.toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return this.toggled;
    }
}
