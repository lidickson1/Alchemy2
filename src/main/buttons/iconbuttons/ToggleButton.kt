package main.buttons.iconbuttons

import main.Main

open class ToggleButton(path: String) : IconButton(path) {

    var isToggled = true
    //TODO: this is a rough implementation
    private val onImage = Main.loadImage(path)
    private val onImageOverlay = brightenImage(onImage)
    private val offImage = Main.loadImage(path.replace(".png", "_off.png"))
    private val offImageOverlay = brightenImage(offImage)

    override val tintedImage = if (isToggled) onImageOverlay else offImageOverlay

    init {
        onImage.resize(width, height)
        onImageOverlay.resize(width, height)
        offImage.resize(width, height)
        offImageOverlay.resize(width, height)
    }

    override fun drawButton() {
        Main.image(if (isToggled) onImage else offImage, x, y)
    }

    override fun clicked() {
        isToggled = !isToggled
    }
}