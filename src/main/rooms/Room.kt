package main.rooms

import main.Language
import main.Main
import processing.core.PConstants
import processing.core.PImage

abstract class Room {
    internal val plus: PImage = Main.loadImage("resources/images/plus.png")
    internal val equal: PImage = Main.loadImage("resources/images/equal.png")

    fun drawTitle(section: String, key: String) {
        Main.textSize(40f)
        Main.textAlign(PConstants.CENTER, PConstants.CENTER)
        Main.fill(255)
        Main.text(Language.languageSelected.getLocalizedString(section, key), Main.screenWidth / 2f, 60f)
    }

    abstract fun setup()
    abstract fun draw()
    open fun end() {}
    open fun mousePressed() {}
    open fun keyPressed() {}
}