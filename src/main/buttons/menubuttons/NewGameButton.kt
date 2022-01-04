package main.buttons.menubuttons

import main.Main
import main.rooms.Game

class NewGameButton : MenuButton("new game") {
    override fun clicked() {
        Game.saveFile = null
        Game.gameLoaded = false
        Main.switchRoom(Game)
    }
}