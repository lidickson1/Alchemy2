package main.buttons.menubuttons;

public class PacksButton extends MenuButton {

    public PacksButton() {
        super("packs");
    }

    @Override
    public void clicked() {
        main.switchRoom(main.packsRoom);
    }

}
