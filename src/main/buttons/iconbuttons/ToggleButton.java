package main.buttons.iconbuttons;

import processing.core.PImage;

public class ToggleButton extends IconButton {

    private boolean toggled = true;
    private PImage onImage;
    private PImage onImageOverlay;
    private PImage offImage;
    private PImage offImageOverlay;

    public ToggleButton(String path) {
        super(path);

        //TODO: this is a rough implementation
        this.onImage = main.loadImage(path);
        this.onImage.resize(this.getWidth(), this.getHeight());
        this.onImageOverlay = brightenImage(this.onImage);

        this.offImage = main.loadImage(path.replace(".png", "_off.png"));
        this.offImage.resize(this.getWidth(), this.getHeight());
        this.offImageOverlay = brightenImage(this.offImage);
    }

    @Override
    protected PImage getImage() {
        return this.toggled ? this.onImage : this.offImage;
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
