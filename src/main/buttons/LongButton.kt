package main.buttons

import main.Main

abstract class LongButton internal constructor() : Button(WIDTH, HEIGHT), Comparable<LongButton> {
    override val tintOverlay = false
    override fun drawButton() {
        Main.stroke(255)
        Main.noFill()
        Main.rect(x, y, WIDTH.toFloat(), HEIGHT.toFloat())
    }

    companion object {
        const val WIDTH = 500
        const val HEIGHT = 64
    }
}