package main.buttons.menubuttons;

public class NewGameButton extends MenuButton {

    public NewGameButton() {
        super("new game");
    }

    @Override
    public void clicked() {
        main.game.setSaveFile(null);
        main.switchRoom(main.game);
    }

}
