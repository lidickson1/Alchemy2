package main.buttons;

import ddf.minim.AudioPlayer;
import main.Entity;
import processing.core.PImage;

public class Button extends Entity {

    public static AudioPlayer click;

    private float x;
    private float y;

    private int width;
    private int height;

    private PImage image;

    private boolean disabled = false;

    protected Button(int width, int height, String path) {
        this(width, height, main.loadImage(path));
    }

    Button(int width, int height, PImage image) {
        this(width, height);
        this.image = image;
        this.image.resize(width, height);
    }

    Button(int width, int height) {
        this.width = width;
        this.height = height;
    }

    void setImage(PImage image) {
        this.image = image;
    }

    public void draw(float x, float y) {
        this.x = x;
        this.y = y;

        this.drawButton();

        if (this.inBounds()) {
            main.noStroke();
            if (this.disabled) {
                main.fill(255, 0, 0, 80);
            } else {
                main.fill(255, 80);
            }
            main.rect(x, y, this.width, this.height);
        }
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

    protected void drawButton() {
        main.image(this.image, this.x, this.y);
    }

    protected boolean inBounds() {
        return main.mouseX >= this.x && main.mouseX < this.x + this.width && main.mouseY >= this.y && main.mouseY < this.y + this.height;
    }

    protected PImage getImage() {
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

    protected void setX(float x) {
        this.x = x;
    }

    protected void setY(float y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    protected boolean isDisabled() {
        return this.disabled;
    }

}
