package main.rooms

import main.Language
import main.Main
import main.buttons.iconbuttons.IconButton
import main.buttons.menubuttons.LoadGameButton
import main.buttons.menubuttons.MenuButton
import main.buttons.menubuttons.NewGameButton
import main.buttons.menubuttons.PacksButton
import processing.core.PConstants
import processing.core.PFont

object Menu : Room() {

    private val loadGame: LoadGameButton = LoadGameButton()
    private val newGame: NewGameButton = NewGameButton()
    private val packs: PacksButton = PacksButton()
    private val achievements: MenuButton = MenuButton("achievements")
    private val settings: IconButton
    private val titleFont: PFont = Main.createFont("resources/fonts/Alchemy Gold.ttf", 128f)

    init {
        settings = object : IconButton("resources/images/settings_button.png") {
            override fun clicked() {
                Main.switchRoom(SettingsRoom)
            }
        }
    }

    override fun setup() {}

    override fun draw() {
        Main.textFont(titleFont, 220f)
        Main.textAlign(PConstants.CENTER, PConstants.TOP)
        Main.fill(255)
        Main.text(Language.languageSelected.getLocalizedString("menu", "alchemy"), Main.screenWidth / 2f, 20f)
        var y = 340
        val gap = 10
        loadGame.draw(Main.screenWidth / 2f - MenuButton.WIDTH / 2f, y.toFloat())
        y += MenuButton.HEIGHT + gap
        newGame.draw(Main.screenWidth / 2f - MenuButton.WIDTH / 2f, y.toFloat())
        y += MenuButton.HEIGHT + gap
        packs.draw(Main.screenWidth / 2f - MenuButton.WIDTH / 2f, y.toFloat())
        y += MenuButton.HEIGHT + gap
        achievements.draw(Main.screenWidth / 2f - MenuButton.WIDTH / 2f, y.toFloat())
        settings.draw(30f, (Main.screenHeight - 30 - IconButton.SIZE).toFloat())
    }

    override fun mousePressed() {
        loadGame.mousePressed()
        newGame.mousePressed()
        packs.mousePressed()
        achievements.mousePressed()
        settings.mousePressed()
    }
}