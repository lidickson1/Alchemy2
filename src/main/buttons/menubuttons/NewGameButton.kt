package main.buttons.menubuttons

import main.rooms.Game

class NewGameButton : MenuButton("new game") {
    override fun clicked() {
        Game.saveFile = null
        Game.gameLoaded = false
        main.switchRoom(Game)
    }
}