package main.buttons

import ddf.minim.AudioPlayer
import main.Main
import processing.core.PImage
import processing.core.PApplet

open class Button  {
    val width: Int
    val height: Int
    protected var x = 0f
    protected var y = 0f
    var image: PImage
    @JvmField
    protected var tintedImage: PImage? = null
    open val tintOverlay = true
    private var disabled = false

    internal constructor(width: Int, height: Int): this(width, height, null as PImage?)

    protected constructor(width: Int, height: Int, path: String?) : this(
        width,
        height,
        Main.loadImage(path)
    )

    private constructor(width: Int, height: Int, image: PImage?) {
        this.width = width
        this.height = height
        this.image = image ?: error.copy()
        this.image.resize(width, height)
    }

//    fun setImage(image: PImage?) {
//        this.image = image
//        if (this.image == null) {
//            this.image = error.copy()
//            this.image!!.resize(width, height)
//        }
//    }

    //using getImage() here so it's easier to implement ToggleButton
    open fun draw(x: Float, y: Float) {
        this.x = x
        this.y = y
        drawButton()
        if (inBounds()) {
            if (tintOverlay) {
                getTintedImage()
                if (disabled) {
                    Main.tint(255f, 0f, 0f, ALPHA.toFloat())
                }
                Main.image(tintedImage, this.x, this.y)
                Main.tint(255)
            } else {
                Main.noStroke()
                if (disabled) {
                    Main.fill(255f, 0f, 0f, ALPHA.toFloat())
                } else {
                    Main.fill(255, ALPHA.toFloat())
                }
                Main.rect(x, y, width.toFloat(), height.toFloat())
            }
        }
        postDraw()
    }

    protected open fun getTintedImage() {
        if (tintedImage == null) {
            tintedImage = brightenImage(image)
        }
    }

    protected open fun postDraw() {}

    protected open fun draw() {
        this.draw(x, y)
    }

    open fun mousePressed() {
        if (inBounds() && !disabled) {
            clicked()
            if (Main.settings.getBoolean("sound")) {
                click.play()
                click.rewind()
            }
        }
    }

    open fun clicked() {}

    //using getImage() here so it's easier to implement ToggleButton
    protected open fun drawButton() {
        Main.image(image, x, y)
    }

    protected open fun inBounds(): Boolean {
        return Main.mouseX >= x && Main.mouseX < x + width && Main.mouseY >= y && Main.mouseY < y + height
    }

    fun incrementX(x: Float) {
        this.x += x
    }

    fun incrementY(y: Float) {
        this.y += y
    }

    fun setDisabled(disabled: Boolean) {
        this.disabled = disabled
    }

    companion object {
        lateinit var click: AudioPlayer
        private const val ALPHA = 80
        lateinit var error: PImage
        fun setErrorImage() {
            error = Main.loadImage("resources/images/error.png")
        }

        @JvmStatic
        protected fun brightenImage(image: PImage): PImage {
            val tintedImage = image.copy()
            for (i in tintedImage.pixels.indices) {
                var r = Main.red(tintedImage.pixels[i])
                var g = Main.green(tintedImage.pixels[i])
                var b = Main.blue(tintedImage.pixels[i])
                val a = Main.alpha(tintedImage.pixels[i])
                r = PApplet.constrain(r + ALPHA - 30, 0f, 255f)
                g = PApplet.constrain(g + ALPHA - 30, 0f, 255f)
                b = PApplet.constrain(b + ALPHA - 30, 0f, 255f)
                tintedImage.pixels[i] = Main.color(r, g, b, a)
            }
            return tintedImage
        }
    }
}