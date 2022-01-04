package main.buttons.iconbuttons;

import main.Main;
import processing.core.PImage;

public class ToggleButton extends IconButton {

    private boolean toggled = true;
    private final PImage onImage;
    private final PImage onImageOverlay;
    private final PImage offImage;
    private final PImage offImageOverlay;

    public ToggleButton(String path) {
        super(path);

        //TODO: this is a rough implementation
        this.onImage = Main.INSTANCE.loadImage(path);
        this.onImage.resize(this.getWidth(), this.getHeight());
        this.onImageOverlay = brightenImage(this.onImage);

        this.offImage = Main.INSTANCE.loadImage(path.replace(".png", "_off.png"));
        this.offImage.resize(this.getWidth(), this.getHeight());
        this.offImageOverlay = brightenImage(this.offImage);
    }

    @Override
    protected void drawButton() {
        Main.INSTANCE.image(this.toggled ? this.onImage : this.offImage, this.getX(), this.getY());
    }

    @Override
    protected void getTintedImage() {
        this.tintedImage = this.toggled ? this.onImageOverlay : this.offImageOverlay;
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
