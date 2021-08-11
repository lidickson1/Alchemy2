package main.buttons

import main.Language
import main.rooms.Game
import main.rooms.Game.gameLoaded
import main.rooms.Game.saveFile
import processing.core.PConstants
import processing.data.JSONObject
import java.time.LocalDateTime

class SaveFile(val name: String, val json: JSONObject) : LongButton() {
    override fun drawButton() {
        super.drawButton()
        main.textSize(20f)
        main.textAlign(PConstants.LEFT, PConstants.CENTER)
        main.fill(255)
        main.text(name, x + 10, y + 28)
        main.textAlign(PConstants.RIGHT, PConstants.CENTER)
        main.fill(120)
        main.text(Language.getLanguageSelected().getLocalizedString("load game", "last modified") + ": " + json.getString("last modified"), x + WIDTH - 10, y + 28)
    }

    override fun clicked() {
        saveFile = this
        gameLoaded = false
        main.switchRoom(Game)
    }

    override fun compareTo(other: LongButton): Int {
        return -LocalDateTime.parse(json.getString("last modified"), main.formatter).compareTo(LocalDateTime.parse((other as SaveFile).json.getString("last modified"), main.formatter))
    }
}