package main.buttons.iconbuttons

import main.Main
import main.buttons.Group
import main.rooms.*
import main.rooms.Game.exitGame

class Exit : IconButton("resources/images/exit_button.png") {
    public override fun draw() {
        this.draw((Main.screenWidth - Group.GAP - SIZE).toFloat(), (Main.screenHeight - Group.GAP - SIZE).toFloat())
    }

    override fun clicked() {
        when (Main.room) {
            is SaveRoom, is HistoryRoom, is ElementRoom, is HintRoom -> {
                Main.switchRoom(Game)
            }
            is Game -> {
                exitGame()
            }
            else -> {
                Main.switchRoom(Menu)
            }
        }
    }
}