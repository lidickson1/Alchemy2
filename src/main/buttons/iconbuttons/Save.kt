package main.buttons.iconbuttons

import main.Main
import main.rooms.Game
import main.rooms.SaveRoom

class Save : IconButton("resources/images/save_button.png") {
    override fun clicked() {
        if (Main.room is Game) {
            if (Game.saveFile == null) {
                Main.switchRoom(SaveRoom)
            } else {
                Game.saveGame()
            }
        } else if (Main.room is SaveRoom) {
            Game.saveGame()
            Main.switchRoom(Game)
        }
    }
}