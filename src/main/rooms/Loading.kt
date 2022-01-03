package main.rooms

import main.*
import main.buttons.ElementButton
import main.buttons.Group
import processing.core.PConstants
import processing.data.JSONArray
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

object Loading : Room() {
    private const val GENERATE_ATLAS = false
    private const val PRINT_GENERATIONS = true

    private val progress = AtomicInteger(0)
    private var total = 0
    private var splashText = ""

    override fun setup() {
        splashText = getSplashText()
        progress.set(0)
        total = 0
        PacksRoom.setup() //reads the pack list
        Main.elements.clear()
        Main.groups.clear()
        Main.comboList.clear()
        Main.randomCombos.clear()

        //packs are loaded from bottom to top, for loop in reverse
        val thread = Thread {
            for (pack in PacksRoom.loadedPacks.reversed()) {
                pack.loadLanguages()
                //TODO: this to read from settings
                Language.languageSelected = Language.getLanguage("english")!!
                var groupsArray = JSONArray()
                var elementsArray = JSONArray()
                if (pack.name == "Alchemy") {
                    groupsArray = Main.loadJSONArray("resources/groups.json")
                    elementsArray = Main.loadJSONArray("resources/elements.json")
                    //Compressor.toBinary(elementsArray);
                    //Compressor.fromBinary(new File("elements.bin"));
                } else {
                    if (File(pack.path + "/groups.json").exists()) {
                        groupsArray = Main.loadJSONArray(pack.path + "/groups.json")
                    }
                    if (File(pack.path + "/elements.json").exists()) {
                        elementsArray = Main.loadJSONArray(pack.path + "/elements.json")
                    }
                }
                total += if (pack.hasAtlas()) {
                    groupsArray.size() + elementsArray.size() + 1
                } else {
                    groupsArray.size() + elementsArray.size() * 2
                }
                Group.loadGroups(groupsArray, pack)
                LoadElements.loadElements(elementsArray, pack)
                if (pack.hasAtlas()) {
                    pack.loadAtlas()
                }
            }
            if (PRINT_GENERATIONS) {
                val set = Main.groups.values.flatten().map { it.id }.toSet()
                Generation.generate(set, Main.comboList)
            }

            //textures are loaded here, after we have defined all of the elements
            val size = 50 //size of each buffer
            var buffer = ArrayList<Element>()
            var index = 0
            for (group in Main.groups.keys) {
                for (element in Main.groups[group]!!) {
                    if (!element.isImageInitialized || element.variation != null) { //some images might already be loaded from an atlas
                        if (index != 0 && index % size == 0) {
                            Element.loadImage(buffer)
                            buffer = ArrayList()
                        }
                        buffer.add(element)
                        index++
                    }
                }
            }
            if (buffer.isNotEmpty()) {
               Element.loadImage(buffer)
            }
        }
        thread.isDaemon = true
        thread.start()
        Main.textFont(Main.font, 20f)
    }

    override fun draw() {
        // Loading screen
        Main.image(Main.icon, Main.screenWidth / 2f - Main.icon.width / 2f, Main.screenHeight / 2f - Main.icon.height / 2f - 90)
        val width = 900
        Main.noFill()
        Main.stroke(255)
        Main.rect(Main.screenWidth / 2f - 450, Main.screenHeight / 2f + 50, width.toFloat(), 10f)
        Main.fill(255)
        Main.noStroke()
        val length = if (total == 0) 0 else (progress.get().toFloat() / total * width).roundToInt()
        Main.rect(Main.screenWidth / 2f - 450, Main.screenHeight / 2f + 50, length.toFloat(), 10f)
        Main.textAlign(PConstants.CENTER)
        Main.text(splashText, Main.screenWidth / 2f, Main.screenHeight / 2f + 100)
        if (total > 0 && progress.get() >= total) {
            //generate atlases, this can only be executed when all images are loaded
            if (GENERATE_ATLAS) {
                for (pack in PacksRoom.loadedPacks) {
                    pack.generateAtlas()
                }
            }
            //Language.validateEnglish();
            Main.switchRoom(Menu)
        }
    }

    private fun getSplashText(): String {
        return File("resources/splash.txt").readLines().random().replace("#", "\n")
    }

    fun updateProgress() {
        progress.incrementAndGet()
    }

    fun elementFailed() {
        //remove its counter from total
        total -= 2
        checkEverythingFailed()
    }

    fun removeAllElements(except: Int) {
        //remove its counter from total (json loading is successful, no need to load image)
        total--
        for (list in Main.groups.values) {
            //we only subtract 1 for each because the json loading is already done, only need to remove the progress for loading the images
            total -= list.size
        }
        total += except
        checkEverythingFailed()
    }

    private fun checkEverythingFailed() {
        //if everything fails
        if (total == 0 && progress.get() == 0) {
            total = 1
            progress.set(1)
        }
    }

    fun removeElement() {
        total -= 2 //remove both the image of the remove and the actual element
        checkEverythingFailed()
    }

    fun removeCombo() {
        total--
        checkEverythingFailed()
    }

    fun removeGroup(group: Group) {
        total -= Main.groups[group]!!.size
        checkEverythingFailed()
    }

    fun removeAllGroups() {
        for (group in Main.groups.keys) {
            total -= Main.groups[group]!!.size
        }
        checkEverythingFailed()
    }

    fun randomCombo() {
        total--
        checkEverythingFailed()
    }

    fun modifyElement() {
        total--
        checkEverythingFailed()
    }

}