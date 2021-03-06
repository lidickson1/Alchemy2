package main.buttons.iconbuttons;

import main.buttons.Group;
import main.rooms.*;

public class Exit extends IconButton {

    public Exit() {
        super("resources/images/exit_button.png");
    }

    @Override
    public void draw() {
        this.draw(main.screenWidth - Group.GAP - IconButton.SIZE, main.screenHeight - Group.GAP - IconButton.SIZE);
    }

    @Override
    public void clicked() {
        if (main.getRoom() instanceof SaveRoom || main.getRoom() instanceof HistoryRoom || main.getRoom() instanceof ElementRoom || main.getRoom() instanceof Hint) {
            main.switchRoom(main.game);
        } else if (main.getRoom() instanceof Game) {
            main.game.exitGame();
        } else {
            main.switchRoom(main.menu);
        }
    }

}
