package main.buttons.menubuttons;

import main.Main;
import main.rooms.LoadGame;

public class LoadGameButton extends MenuButton {

    public LoadGameButton() {
        super("load game");
    }

    @Override
    public void clicked() {
        Main.INSTANCE.switchRoom(LoadGame.INSTANCE);
    }

}
