package main.rooms

import main.Entity
import main.Language
import processing.core.PConstants
import processing.core.PImage

abstract class Room : Entity() {
    internal val plus: PImage = main.loadImage("resources/images/plus.png")
    internal val equal: PImage = main.loadImage("resources/images/equal.png")

    fun drawTitle(section: String?, key: String?) {
        main.textSize(40f)
        main.textAlign(PConstants.CENTER, PConstants.CENTER)
        main.fill(255)
        main.text(Language.getLanguageSelected().getLocalizedString(section, key), main.screenWidth / 2f, 60f)
    }

    abstract fun setup()
    abstract fun draw()
    open fun end() {}
    open fun mousePressed() {}
    open fun keyPressed() {}
}