package main.rooms

import main.Language
import main.Slider
import main.buttons.iconbuttons.Exit
import main.buttons.iconbuttons.ToggleButton
import java.io.OutputStream
import java.io.PrintStream

object SettingsRoom : Room() {

    private val musicButton: ToggleButton
    private val soundButton: ToggleButton
    private val groupColour: ToggleButton
    private val exit: Exit
    lateinit var slider: Slider

    init {
        musicButton = object : ToggleButton("resources/images/music_button.png") {
            override fun clicked() {
                super.clicked()
                if (this.isToggled) {
                    main.backgroundMusicThread.interrupt()
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
        musicButton.isToggled = main.settings.getBoolean("music")
        soundButton.isToggled = main.settings.getBoolean("sound")
        groupColour.isToggled = main.settings.getBoolean("group colour")

        //for hiding the G4P announcement
        val originalStream = System.out
        val dummyStream = PrintStream(object : OutputStream() {
            override fun write(b: Int) {}
        })
        System.setOut(dummyStream)
        slider = Slider(main)
        slider.setValue(main.settings.getFloat("volume"))
        System.setOut(originalStream)
    }

    override fun draw() {
        drawTitle("settings", "settings")
        main.textSize(20f)
        main.text(Language.getLanguageSelected().getLocalizedString("settings", "volume"), main.screenWidth / 2f, 200f)
        slider.moveTo(main.screenWidth / 2f - slider.width / 2, 220f)
        slider.draw()
        val length = (ToggleButton.SIZE + ToggleButton.GAP) * 3 - ToggleButton.GAP
        var x = main.screenWidth / 2 - length / 2
        musicButton.draw(x.toFloat(), 400f)
        x += ToggleButton.SIZE + ToggleButton.GAP
        soundButton.draw(x.toFloat(), 400f)
        x += ToggleButton.SIZE + ToggleButton.GAP
        groupColour.draw(x.toFloat(), 400f)
        exit.draw()

        //this needs to be updated immediately because background music constantly plays in the background
        main.backgroundMusic.gain = main.toGain(slider.valueF)
        if (musicButton.isToggled) {
            main.backgroundMusic.unmute()
        } else {
            main.backgroundMusic.mute()
        }

        //update settings because they should be in effect immediately
        main.settings.put("music", musicButton.isToggled)
        main.settings.put("sound", soundButton.isToggled) //if this wasn't updated here, the button clicks wouldn't respond to this setting
        main.settings.put("volume", slider.valueF)
        main.settings.put("group colour", groupColour.isToggled)
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