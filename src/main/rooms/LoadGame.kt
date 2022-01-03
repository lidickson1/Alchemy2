package main.rooms

import main.Language
import main.Main
import main.buttons.SaveFile
import main.buttons.iconbuttons.Exit
import processing.core.PApplet
import java.io.File

object LoadGame : Room() {
    private val exit: Exit = Exit()
    private var failed = false
    private val saveFiles = mutableListOf<SaveFile>()
    override fun setup() {
        failed = false
        saveFiles.clear()
        File("resources/saves/").listFiles()?.let {
            for (file in it) {
                if (file.isFile && file.extension == "json") {
                    saveFiles.add(SaveFile(file.nameWithoutExtension, PApplet.loadJSONObject(file)))
                }
            }
        }
        saveFiles.sort()
    }

    override fun draw() {
        drawTitle("load game", "load game")
        var y = 120
        for (saveFile in saveFiles) {
            saveFile.draw(Main.screenWidth / 2f - saveFile.width / 2f, y.toFloat())
            y += saveFile.height
        }
        exit.draw()
    }

    override fun mousePressed() {
        for (saveFile in saveFiles) {
            saveFile.mousePressed()
            if (failed) {
                break
            }
        }
        if (failed) {
            Main.showError(Language.languageSelected.getLocalizedString("load game", "elements not loaded"))
            Main.switchRoom(this)
        }
        exit.mousePressed()
    }

    fun failed() {
        failed = true
    }

}