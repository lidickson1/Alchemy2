package main.buttons

import main.Language
import main.Main
import main.rooms.Game
import main.rooms.Game.gameLoaded
import main.rooms.Game.saveFile
import processing.core.PConstants
import processing.data.JSONObject
import java.time.LocalDateTime

class SaveFile(val name: String, val json: JSONObject) : LongButton() {
    override fun drawButton() {
        super.drawButton()
        Main.textSize(20f)
        Main.textAlign(PConstants.LEFT, PConstants.CENTER)
        Main.fill(255)
        Main.text(name, x + 10, y + 28)
        Main.textAlign(PConstants.RIGHT, PConstants.CENTER)
        Main.fill(120)
        Main.text(Language.languageSelected.getLocalizedString("load game", "last modified") + ": " + json.getString("last modified"), x + WIDTH - 10, y + 28)
    }

    override fun clicked() {
        saveFile = this
        gameLoaded = false
        Main.switchRoom(Game)
    }

    override fun compareTo(other: LongButton): Int {
        return -LocalDateTime.parse(json.getString("last modified"), Main.formatter).compareTo(LocalDateTime.parse((other as SaveFile).json.getString("last modified"), Main.formatter))
    }
}