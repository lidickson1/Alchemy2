package main.rooms

import main.Language
import main.TextField
import main.buttons.iconbuttons.Exit
import main.buttons.iconbuttons.Save
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

object SaveRoom : Room() {
    private lateinit var textField: TextField
    private val save: Save = Save()
    private val exit: Exit = Exit()
    private var text: String = ""
    override fun setup() {
        //for hiding the G4P announcement
        val originalStream = System.out
        val dummyStream = PrintStream(object : OutputStream() {
            override fun write(b: Int) {}
        })
        System.setOut(dummyStream)
        textField = TextField(main, 400F, 30F)
        System.setOut(originalStream)
    }

    override fun draw() {
        save.setDisabled(!updateText())
        drawTitle("save", "save game")
        main.textSize(20f)
        main.text(Language.getLanguageSelected().getLocalizedString("save", "enter name"), main.screenWidth / 2f, main.screenHeight / 2f - 60)
        textField.moveTo(main.screenWidth / 2f - textField.width / 2, main.screenHeight / 2f - textField.height / 2)
        textField.draw()
        if (text != "") {
            main.textSize(20f)
            main.text(Language.getLanguageSelected().getLocalizedString("save", text), main.screenWidth / 2f, main.screenHeight / 2f + 60)
        }
        main.stroke(255)
        main.noFill()
        main.rect(textField.x, textField.y, textField.width, textField.height + 1)
        save.draw(main.screenWidth / 2f - 15 - save.width, (main.screenHeight - 30 - save.height).toFloat())
        exit.draw(main.screenWidth / 2f + 15, (main.screenHeight - 30 - exit.height).toFloat())
    }

    override fun end() {
        textField.dispose()
    }

    override fun mousePressed() {
        save.mousePressed()
        exit.mousePressed()
    }

    private fun updateText(): Boolean {
        if (saveName.isEmpty()) {
            text = "name empty"
            return false
        }
        val names = mutableListOf<String>()
        File("resources/saves/").listFiles()?.let {
            for (file in it) {
                if (file.isFile && file.extension == "json") {
                    names.add(file.nameWithoutExtension)
                }
            }
        }
        if (names.contains(saveName)) {
            text = "already exists"
            return false
        }
        text = ""
        return true
    }

    val saveName: String get() = textField.text.trim()

}