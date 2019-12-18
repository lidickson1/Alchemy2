package main.buttons.iconbuttons;

import main.rooms.Game;
import main.rooms.SaveRoom;

public class Save extends IconButton {

    public Save() {
        super("resources/images/save_button.png");
    }

    @Override
    public void clicked() {
        if (main.getRoom() instanceof Game) {
            if (main.game.getSaveFile() == null) {
                main.switchRoom(main.saveRoom);
            } else {
                main.game.saveGame();
            }
        } else if (main.getRoom() instanceof SaveRoom) {
            main.game.saveGame();
            main.switchRoom(main.game);
        }
    }

}
