package main.rooms

import main.Language
import main.buttons.Group
import main.buttons.LongButton
import main.buttons.Pack
import main.buttons.iconbuttons.Exit
import main.buttons.iconbuttons.IconButton
import processing.core.PConstants
import processing.data.JSONArray
import processing.data.JSONObject
import processing.data.StringList
import java.io.File

object PacksRoom : Room() {
    private const val GAP = 100
    private val exit: Exit = Exit()
    private val done: IconButton
    private val unloadedPacks = mutableListOf<Pack>()
    val loadedPacks = mutableListOf<Pack>()
    private var movePack: Pack? = null //pack to be moved

    init {
        done = object : IconButton("resources/images/done_button.png") {
            override fun clicked() {
                save()
                main.switchRoom(Loading)
            }
        }
    }

    fun setMovePack(movePack: Pack?) {
        this.movePack = movePack
    }

    override fun setup() {
        unloadedPacks.clear()
        loadedPacks.clear()
        movePack = null
        File("resources/packs/").listFiles()?.let {
            for (folder in it) {
                val jsonFile = File(folder.absolutePath + "/pack.json")
                val icon = File(folder.absolutePath + "/icon.png")
                if (jsonFile.exists() && icon.exists()) {
                    val `object`: JSONObject = main.loadJSONObject(jsonFile.absolutePath)
                    if (`object`.getString("name") == "Alchemy") {
                        System.err.println("Error: pack name cannot be name \"Alchemy\"")
                        continue
                    }
                    unloadedPacks.add(Pack(folder.absolutePath, `object`))
                }
            }
        }
        unloadedPacks.add(Pack()) //add default pack
        unloadedPacks.sort()

        //loaded packs are read from settings file
        for (string in main.settings.getJSONArray("loaded packs").stringArray) {
            var pack: Pack?
            if (string == "Alchemy") {
                pack = getPack("Alchemy")
            } else {
                pack = getPack(string)
                if (pack == null) {
                    System.err.println("Error: can't load pack with name: $string")
                    continue
                }
            }
            unloadedPacks.remove(pack)
            loadedPacks.add(pack!!)
        }
    }

    override fun draw() {
        drawTitle("packs", "packs")
        var x = main.screenWidth / 2 - GAP / 2 - LongButton.WIDTH
        var y = 120
        main.textSize(24f)
        main.textAlign(PConstants.CENTER, PConstants.CENTER)
        main.fill(255)
        main.text(Language.getLanguageSelected().getLocalizedString("packs", "unloaded packs"), x + LongButton.WIDTH / 2f, y.toFloat())
        y += 40
        for (pack in unloadedPacks) {
            pack.draw(x.toFloat(), y.toFloat())
            y += pack.height
        }
        x = main.screenWidth / 2 + GAP / 2
        y = 120
        main.textSize(24f)
        main.textAlign(PConstants.CENTER, PConstants.CENTER)
        main.fill(255)
        main.text(Language.getLanguageSelected().getLocalizedString("packs", "loaded packs"), x + LongButton.WIDTH / 2f, y.toFloat())
        y += 40
        for (pack in loadedPacks) {
            pack.draw(x.toFloat(), y.toFloat())
            y += pack.height
        }
        done.setDisabled(loadedPacks.isEmpty())
        done.draw((main.screenWidth - Group.GAP - IconButton.SIZE - (IconButton.SIZE + Group.GAP)).toFloat(), (main.screenHeight - Group.GAP - IconButton.SIZE).toFloat())
        exit.draw()
    }

    override fun mousePressed() {
        for (pack in unloadedPacks) {
            pack.mousePressed()
        }
        for (pack in loadedPacks) {
            pack.mousePressed()
        }
        movePack?.let {
            if (unloadedPacks.contains(it)) {
                unloadedPacks.remove(it)
                loadedPacks.add(0, it)
            } else {
                loadedPacks.remove(it)
                unloadedPacks.add(it)
            }
            unloadedPacks.sort()
        }
        movePack = null
        done.mousePressed()
        exit.mousePressed()
    }

    private fun save() {
        main.settings.put("loaded packs", JSONArray(StringList( loadedPacks.map { it.name })))
        main.saveSettings()
    }

    private fun getPack(name: String): Pack? {
        for (pack in unloadedPacks) {
            if (pack.name == name) {
                return pack
            }
        }
        return null
    }
}