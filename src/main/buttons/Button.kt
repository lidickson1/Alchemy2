package main.buttons

import ddf.minim.AudioPlayer
import main.Main
import processing.core.PApplet
import processing.core.PImage

open class Button(val width: Int, val height: Int, val image: PImage) {
    protected var x = 0f
    protected var y = 0f

    //if this is not lazy, it will cause the screen to flicker when the game is loading lol
    open val tintedImage: PImage? by lazy { brightenImage(image) }
    var disabled = false

    internal constructor(width: Int, height: Int) : this(width, height, error.copy())

    protected constructor(width: Int, height: Int, path: String) : this(
        width,
        height,
        Main.loadImage(path) ?: error.copy()
    )

    init {
        image.resize(width, height)
    }

    open fun draw(x: Float, y: Float) {
        this.x = x
        this.y = y
        drawButton()
        if (inBounds()) {
            if (tintedImage != null) {
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

    protected open fun postDraw() {}

    protected open fun draw() {
        draw(x, y)
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

    protected open fun drawButton() {
        Main.image(image, x, y)
    }

    protected open fun inBounds(): Boolean {
        return Main.mouseX >= x && Main.mouseX < x + width && Main.mouseY >= y && Main.mouseY < y + height
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