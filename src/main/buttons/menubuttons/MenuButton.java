package main.buttons.menubuttons;

import main.Language;
import main.buttons.Button;
import processing.core.PConstants;

public class MenuButton extends Button {

    public static final int WIDTH = 200;
    public static final int HEIGHT = 70;

    private final String name;

    public MenuButton(String name) {
        super(WIDTH, HEIGHT, "resources/images/menu_button.png");
        this.name = name;
    }

    @Override
    protected void postDraw() {
        final int gap = 10;
        main.setFontSize(this.name, 32, WIDTH - gap * 2);
        main.fill(0);
        main.textAlign(PConstants.CENTER, PConstants.CENTER);
        main.text(Language.getLanguageSelected().getLocalizedString("menu", this.name), this.getX() + WIDTH / 2F, this.getY() + HEIGHT / 2F - 4);
    }

}
