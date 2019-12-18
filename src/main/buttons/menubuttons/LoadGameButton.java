package main.buttons.menubuttons;

public class LoadGameButton extends MenuButton {

    public LoadGameButton() {
        super("load game");
    }

    @Override
    public void clicked() {
        main.switchRoom(main.loadGame);
    }

}
