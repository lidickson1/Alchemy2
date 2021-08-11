package main.buttons.iconbuttons

import main.rooms.Game
import main.rooms.SaveRoom

class Save : IconButton("resources/images/save_button.png") {
    override fun clicked() {
        if (main.room is Game) {
            if (Game.saveFile == null) {
                main.switchRoom(SaveRoom)
            } else {
                Game.saveGame()
            }
        } else if (main.room is SaveRoom) {
            Game.saveGame()
            main.switchRoom(Game)
        }
    }
}