package main.buttons;

public abstract class LongButton extends Button implements Comparable<LongButton> {

    public static final int WIDTH = 500;
    static final int HEIGHT = 64;

    LongButton() {
        super(WIDTH, HEIGHT);
    }

    @Override
    protected void drawButton() {
        main.stroke(255);
        main.noFill();
        main.rect(this.getX(), this.getY(), WIDTH, HEIGHT);

//        main.textAlign(PConstants.RIGHT, PConstants.CENTER);
//        main.fill(120);
//        main.text(main.getLanguageSelected().getLocalizedString("load game", "last modified") + ": " + this.json.getString("last modified"), this.getX() + WIDTH - 10, this.getY() + 28);
    }

}
