package main.buttons;

import ddf.minim.AudioPlayer;
import main.Entity;
import processing.core.PApplet;
import processing.core.PImage;

public class Button extends Entity {

    public static AudioPlayer click;
    private static final int ALPHA = 80;
    private static PImage error;

    private float x;
    private float y;

    private int width;
    private int height;

    private PImage image;
    protected PImage tintedImage;
    boolean tintOverlay = true;

    private boolean disabled = false;

    protected Button(int width, int height, String path) {
        this(width, height, main.loadImage(path));
    }

    Button(int width, int height, PImage image) {
        this(width, height);
        this.image = image;
        this.image.resize(width, height);
        if (this.image == null) {
            this.image = error.copy();
            this.image.resize(this.width, this.height);
        }
    }

    Button(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static void setErrorImage() {
        error = main.loadImage("resources/images/error.png");
    }

    void setImage(PImage image) {
        this.image = image;
        if (this.image == null) {
            this.image = error.copy();
            this.image.resize(this.width, this.height);
        }
    }

    //using getImage() here so it's easier to implement ToggleButton
    public void draw(float x, float y) {
        this.x = x;
        this.y = y;

        this.drawButton();

        if (this.inBounds()) {
            if (this.tintOverlay && this.getImage() != null) {
                this.getTintedImage();
                if (this.disabled) {
                    main.tint(255,0,0,ALPHA);
                }
                main.image(this.tintedImage, this.getX(), this.getY());
                main.tint(255);
            } else {
                main.noStroke();
                if (this.disabled) {
                    main.fill(255, 0, 0, ALPHA);
                } else {
                    main.fill(255, ALPHA);
                }
                main.rect(x, y, this.width, this.height);
            }
        }

        this.postDraw();
    }

    protected void getTintedImage() {
        if (this.tintedImage == null) {
            this.tintedImage = brightenImage(this.image);
        }
    }

    protected static PImage brightenImage(PImage image) {
        PImage tintedImage = image.copy();
        for (int i = 0;i < tintedImage.pixels.length;i++) {
            float r = main.red(tintedImage.pixels[i]);
            float g = main.green(tintedImage.pixels[i]);
            float b = main.blue(tintedImage.pixels[i]);
            float a = main.alpha(tintedImage.pixels[i]);
            r = PApplet.constrain(r + ALPHA - 30, 0, 255);
            g = PApplet.constrain(g + ALPHA - 30, 0, 255);
            b = PApplet.constrain(b + ALPHA - 30, 0, 255);
            tintedImage.pixels[i] = main.color(r, g, b, a);
        }
        return tintedImage;
    }

    protected void postDraw() {

    }

    protected void draw() {
        this.draw(this.x, this.y);
    }

    public void mousePressed() {
        if (this.inBounds() && !this.disabled) {
            this.clicked();
            if (main.getSettings().getBoolean("sound")) {
                click.play();
                click.rewind();
            }
        }
    }

    public void clicked() {

    }

    //using getImage() here so it's easier to implement ToggleButton
    protected void drawButton() {
        main.image(this.getImage(), this.x, this.y);
    }

    protected boolean inBounds() {
        return main.mouseX >= this.x && main.mouseX < this.x + this.width && main.mouseY >= this.y && main.mouseY < this.y + this.height;
    }

    public PImage getImage() {
        return this.image;
    }

    protected float getX() {
        return this.x;
    }

    protected float getY() {
        return this.y;
    }

    void incrementX(float x) {
        this.x += x;
    }

    void incrementY(float y) {
        this.y += y;
    }

    void setX(float x) {
        this.x = x;
    }

    void setY(float y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}
