package main

import ddf.minim.AudioPlayer
import ddf.minim.Minim
import g4p_controls.G4P
import main.buttons.Button
import main.buttons.Group
import main.combos.Combo
import main.combos.RandomCombo
import main.rooms.Loading
import main.rooms.Room
import processing.awt.PSurfaceAWT.SmoothCanvas
import processing.core.PApplet
import processing.core.PFont
import processing.core.PImage
import processing.data.JSONArray
import processing.data.JSONObject
import java.awt.*
import java.awt.event.KeyEvent
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.UIManager
import javax.swing.plaf.BorderUIResource

object Main : PApplet() {
    @JvmField
    var screenWidth = 1600

    @JvmField
    var screenHeight = 900
    private var jFrame: JFrame by lateVal()
    private var minim: Minim by lateVal()
    lateinit var backgroundMusic: AudioPlayer
    var backgroundMusicThread: Thread by lateVal()

    @JvmField
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    var font: PFont by lateVal()
    var icon: PImage by lateVal()
    lateinit var room: Room
        private set
    var settings: JSONObject by lateVal()

    @JvmField
    var groups = HashMap<Group, HashSet<Element>>()

    @JvmField
    var elements = HashMap<String, Group>()

    @JvmField
    var comboList = HashSet<Combo>()

    @JvmField
    var randomCombos = HashSet<RandomCombo>()

    override fun settings() {
        settings = if (File("resources/settings.json").exists()) {
            this.loadJSONObject("resources/settings.json")
        } else {
            JSONObject()
        }
        defaultSettings()
        this.size(screenWidth, screenHeight)
        this.smooth(8)
        Entity.init(this)
        G4P.messagesEnabled(false)
    }

    private fun defaultSettings() {
        if (settings.isNull("volume")) {
            settings.put("volume", 0.5)
        }
        if (settings.isNull("music")) {
            settings.put("music", true)
        }
        if (settings.isNull("sound")) {
            settings.put("sound", true)
        }
        if (settings.isNull("fullscreen")) {
            settings.put("fullscreen", false)
        }
        if (settings.isNull("loaded packs")) {
            settings.put("loaded packs", JSONArray().append("Alchemy"))
        }
        if (settings.isNull("group colour")) {
            settings.put("group colour", false)
        }
    }

    override fun setup() {
        icon = this.loadImage("resources/images/icon.png")
        icon.resize(128, 128)
        surface.setTitle("Alchemy")
        surface.setIcon(icon)
        surface.setResizable(true)
        Button.setErrorImage()

        //set minimum size
        val sc = getSurface().native as SmoothCanvas
        jFrame = sc.frame as JFrame
        jFrame.minimumSize = Dimension(1280, 720)
        //        if (this.settings.getBoolean("fullscreen")) {
//            //technically it's not maximized because the window icon is still 1 rectangle
//            this.jFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
//        }
        //so the window doesn't appear to be a bit off-screen
        jFrame.setLocationRelativeTo(null)
        minim = Minim(this)
        Button.click = minim.loadFile("resources/audio/click.mp3")
        Button.click.gain = toGain(settings.getFloat("volume"))
        font = this.createFont("resources/fonts/Franklin Gothic Book Regular.ttf", 20f)
        val errorFont: Font =
            Font.createFont(Font.TRUETYPE_FONT, File("resources/fonts/Franklin Gothic Book Regular.ttf"))
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        ge.registerFont(errorFont)

        //setting up colors for error dialog
        UIManager.put("OptionPane.background", Color.BLACK)
        UIManager.put("Panel.background", Color.BLACK)
        UIManager.put("OptionPane.messageFont", Font(errorFont.fontName, Font.PLAIN, 20))
        UIManager.put("OptionPane.buttonFont", Font(errorFont.fontName, Font.PLAIN, 14))
        UIManager.put("OptionPane.messageForeground", Color.WHITE)
        UIManager.put("Button.foreground", Color.WHITE)
        UIManager.put("Button.background", Color.BLACK)
        UIManager.put("Button.select", Color.BLACK) //the background color when you hold and click on the button
        UIManager.put("Button.focus", Color.BLACK) //the border color that shows up when the button is focused
        UIManager.put(
            "Button.border", BorderUIResource(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                )
            )
        )

        //TODO: make this dynamic?
        val musicNames =
            mutableListOf("Angel Share", "Dreamer", "Easy Lemon", "Frozen Star", "Handbook - Spirits", "Immersed")
        backgroundMusicThread = Thread {
            while (true) {
                musicNames.shuffle()
                for (name in musicNames) {
                    //to hide the mp3 tag error
                    val originalStream = System.out
                    val dummyStream = PrintStream(object : OutputStream() {
                        override fun write(b: Int) {}
                    })
                    System.setOut(dummyStream)
                    backgroundMusic = minim.loadFile("resources/audio/$name.mp3")
                    System.setOut(originalStream)

                    //this is normally controlled by SettingsRoom, we need it here so that it can still mute when we first load the game since we are not in SettingsRoom
                    if (!settings.getBoolean("music")) {
                        backgroundMusic.mute()
                    }
                    backgroundMusic.gain = toGain(settings.getFloat("volume"))
                    backgroundMusic.play()
                    try {
                        Thread.sleep(backgroundMusic.length().toLong())
                    } catch (ignored: InterruptedException) {
                        backgroundMusic.close()
                        backgroundMusic.unmute()
                    }
                }
            }
        }
        backgroundMusicThread.isDaemon = true
        backgroundMusicThread.start()
        switchRoom(Loading)
    }

    fun toGain(value: Float): Float {
        val min = -40
        val max = 6
        return if (value <= 0.02) {
            Int.MIN_VALUE.toFloat()
        } else {
            min + value * (max - min)
        }
    }

    override fun draw() {
        noStroke()
        fill(0)
        rect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat())
        room.draw()

        //updating screen size
        screenWidth = width
        screenHeight = height
        settings.put("fullscreen", jFrame.extendedState == Frame.MAXIMIZED_BOTH)
    }

    override fun exit() {
        saveSettings()
        super.exit()
    }

    fun saveSettings() {
        this.saveJSONObject(settings, "resources/settings.json", "indent=4")
    }

    fun switchRoom(room: Room) {
        if (this::room.isInitialized) {
            this.room.end()
        }
        this.room = room
        this.room.setup()
    }

    override fun mousePressed() {
        room.mousePressed()
    }

    override fun keyPressed() {
        //prevents application from closing when esc key is pressed
        if (key.code == 27) {
            key = 0.toChar()
        } else if (key.code == CODED && keyCode == KeyEvent.VK_F2) {
            this.saveFrame("screenshot.png")
        }
        room.keyPressed()
    }

    fun setFontSize(name: String, start: Int, max: Int): Boolean {
        val threshold = 12 //when font size is <= this we need to display a tooltip
        var i = start
        while (i > 12) {
            this.textFont(font, i.toFloat())
            if (this.textWidth(name) <= max) {
                break
            }
            i--
        }
        return i <= threshold
    }

    fun showError(message: String?) {
        JOptionPane.showMessageDialog(
            null,
            message,
            Language.languageSelected.getLocalizedString("misc", "error"),
            JOptionPane.ERROR_MESSAGE
        )
    }

}

fun main() {
    PApplet.runSketch(arrayOf(Main::class.java.name), Main)
}