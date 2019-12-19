package main.rooms;

import main.buttons.iconbuttons.IconButton;
import main.buttons.menubuttons.LoadGameButton;
import main.buttons.menubuttons.MenuButton;
import main.buttons.menubuttons.NewGameButton;
import main.buttons.menubuttons.PacksButton;
import processing.core.PConstants;
import processing.core.PFont;

public class Menu extends Room {

    private static PFont titleFont;

    private LoadGameButton loadGame;
    private NewGameButton newGame;
    private PacksButton packs;
    private MenuButton achievements;
    private IconButton settings;

    public Menu() {
        titleFont = main.createFont("resources/fonts/Alchemy Gold.ttf", 128);

        this.loadGame = new LoadGameButton();
        this.newGame = new NewGameButton();
        this.packs = new PacksButton();
        this.achievements = new MenuButton("achievements");
        this.settings = new IconButton("resources/images/settings_button.png") {
            @Override
            public void clicked() {
                main.switchRoom(main.settingsRoom);
            }
        };
    }

    @Override
    public void setup() {

    }

    @Override
    public void draw() {
        // draw objects that are specific to menu
        //main.image(background, 0, 0, main.screenWidth, main.screenHeight);
        main.fill(0);
        main.rect(0,0,main.screenWidth, main.screenHeight);
        main.textFont(titleFont, 220);
        main.textAlign(PConstants.CENTER, PConstants.TOP);
        main.fill(255);
        main.text(main.getLanguageSelected().getLocalizedString("menu", "alchemy"), main.screenWidth / 2F, 20);

        int y = 340;
        final int gap = 10;

        this.loadGame.draw(main.screenWidth / 2F - MenuButton.WIDTH / 2F, y);
        y += MenuButton.HEIGHT + gap;

        this.newGame.draw(main.screenWidth / 2F - MenuButton.WIDTH / 2F, y);
        y += MenuButton.HEIGHT + gap;

        this.packs.draw(main.screenWidth / 2F - MenuButton.WIDTH / 2F, y);
        y += MenuButton.HEIGHT + gap;

        this.achievements.draw(main.screenWidth / 2F - MenuButton.WIDTH / 2F, y);

        this.settings.draw(30, main.screenHeight - 30 - IconButton.SIZE);
    }

    @Override
    public void mousePressed() {
        this.loadGame.mousePressed();
        this.newGame.mousePressed();
        this.packs.mousePressed();
        this.achievements.mousePressed();
        this.settings.mousePressed();
    }

}
