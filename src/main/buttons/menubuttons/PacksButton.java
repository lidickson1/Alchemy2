package main.buttons.menubuttons;

import main.rooms.PacksRoom;

public class PacksButton extends MenuButton {

    public PacksButton() {
        super("packs");
    }

    @Override
    public void clicked() {
        main.switchRoom(PacksRoom.INSTANCE);
    }

}
