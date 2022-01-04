package main.buttons.menubuttons;

import main.Main;
import main.rooms.PacksRoom;

public class PacksButton extends MenuButton {

    public PacksButton() {
        super("packs");
    }

    @Override
    public void clicked() {
        Main.INSTANCE.switchRoom(PacksRoom.INSTANCE);
    }

}
