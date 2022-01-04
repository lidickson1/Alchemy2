package main.rooms

import main.Language
import main.Main
import main.Slider
import main.buttons.iconbuttons.Exit
import main.buttons.iconbuttons.IconButton
import main.buttons.iconbuttons.ToggleButton
import java.io.OutputStream
import java.io.PrintStream

object SettingsRoom : Room() {

    private val musicButton: ToggleButton
    private val soundButton: ToggleButton
    private val groupColour: ToggleButton
    private val exit: Exit
    private lateinit var slider: Slider

    init {
        musicButton = object : ToggleButton("resources/images/music_button.png") {
            override fun clicked() {
                super.clicked()
                if (this.isToggled) {
                    Main.backgroundMusicThread.interrupt()
                } else {
                    //it will be muted automatically, see SettingsRoom.draw
                }
            }
        }
        soundButton = ToggleButton("resources/images/sound_button.png")
        groupColour = ToggleButton("resources/images/group_colour_button.png")
        exit = Exit()
    }

    override fun setup() {
        musicButton.isToggled = Main.settings.getBoolean("music")
        soundButton.isToggled = Main.settings.getBoolean("sound")
        groupColour.isToggled = Main.settings.getBoolean("group colour")

        //for hiding the G4P announcement
        val originalStream = System.out
        val dummyStream = PrintStream(object : OutputStream() {
            override fun write(b: Int) {}
        })
        System.setOut(dummyStream)
        slider = Slider(Main)
        slider.setValue(Main.settings.getFloat("volume"))
        System.setOut(originalStream)
    }

    override fun draw() {
        drawTitle("settings", "settings")
        Main.textSize(20f)
        Main.text(Language.languageSelected.getLocalizedString("settings", "volume"), Main.screenWidth / 2f, 200f)
        slider.moveTo(Main.screenWidth / 2f - slider.width / 2, 220f)
        slider.draw()
        val length = (IconButton.SIZE + IconButton.GAP) * 3 - IconButton.GAP
        var x = Main.screenWidth / 2 - length / 2
        musicButton.draw(x.toFloat(), 400f)
        x += IconButton.SIZE + IconButton.GAP
        soundButton.draw(x.toFloat(), 400f)
        x += IconButton.SIZE + IconButton.GAP
        groupColour.draw(x.toFloat(), 400f)
        exit.draw()

        //this needs to be updated immediately because background music constantly plays in the background
        Main.backgroundMusic.gain = Main.toGain(slider.valueF)
        if (musicButton.isToggled) {
            Main.backgroundMusic.unmute()
        } else {
            Main.backgroundMusic.mute()
        }

        //update settings because they should be in effect immediately
        Main.settings.put("music", musicButton.isToggled)
        Main.settings.put("sound", soundButton.isToggled) //if this wasn't updated here, the button clicks wouldn't respond to this setting
        Main.settings.put("volume", slider.valueF)
        Main.settings.put("group colour", groupColour.isToggled)
    }

    override fun end() {
        slider.dispose()
    }

    override fun mousePressed() {
        musicButton.mousePressed()
        soundButton.mousePressed()
        groupColour.mousePressed()
        exit.mousePressed()
    }
}