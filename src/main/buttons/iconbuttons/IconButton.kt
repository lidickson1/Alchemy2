package main.buttons.iconbuttons

import main.buttons.Button
import main.buttons.Group

open class IconButton protected constructor(path: String) : Button(SIZE, SIZE, path) {
    companion object {
        const val SIZE = 80
        const val GAP = Group.GAP
    }
}