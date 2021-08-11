package main.buttons.menubuttons;

import main.rooms.LoadGame;

public class LoadGameButton extends MenuButton {

    public LoadGameButton() {
        super("load game");
    }

    @Override
    public void clicked() {
        main.switchRoom(LoadGame.INSTANCE);
    }

}
