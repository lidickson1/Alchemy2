package main.rooms

import main.Element
import main.Generation
import main.Language
import main.LoadElements
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
        main.elements.clear()
        main.groups.clear()
        main.comboList.clear()
        main.randomCombos.clear()

        //packs are loaded from bottom to top, for loop in reverse
        val thread = Thread {
            for (pack in PacksRoom.loadedPacks.reversed()) {
                pack.loadLanguages()
                //TODO: this to read from settings
                Language.setLanguageSelected("english")
                var groupsArray = JSONArray()
                var elementsArray = JSONArray()
                if (pack.name == "Alchemy") {
                    groupsArray = main.loadJSONArray("resources/groups.json")
                    elementsArray = main.loadJSONArray("resources/elements.json")
                    //Compressor.toBinary(elementsArray);
                    //Compressor.fromBinary(new File("elements.bin"));
                } else {
                    if (File(pack.path + "/groups.json").exists()) {
                        groupsArray = main.loadJSONArray(pack.path + "/groups.json")
                    }
                    if (File(pack.path + "/elements.json").exists()) {
                        elementsArray = main.loadJSONArray(pack.path + "/elements.json")
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
                val set = main.groups.values.flatten().map { it.id }.toSet()
                Generation.generate(set, main.comboList)
            }

            //textures are loaded here, after we have defined all of the elements
            val size = 50 //size of each buffer
            var buffer = ArrayList<Element>()
            var index = 0
            for (group in main.groups.keys) {
                for (element in main.groups[group]!!) {
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
        main.textFont(main.font, 20f)
    }

    override fun draw() {
        // Loading screen
        main.image(main.icon, main.screenWidth / 2f - main.icon.width / 2f, main.screenHeight / 2f - main.icon.height / 2f - 90)
        val width = 900
        main.noFill()
        main.stroke(255)
        main.rect(main.screenWidth / 2f - 450, main.screenHeight / 2f + 50, width.toFloat(), 10f)
        main.fill(255)
        main.noStroke()
        val length = if (total == 0) 0 else (progress.get().toFloat() / total * width).roundToInt()
        main.rect(main.screenWidth / 2f - 450, main.screenHeight / 2f + 50, length.toFloat(), 10f)
        main.textAlign(PConstants.CENTER)
        main.text(splashText, main.screenWidth / 2f, main.screenHeight / 2f + 100)
        if (total > 0 && progress.get() >= total) {
            //generate atlases, this can only be executed when all images are loaded
            if (GENERATE_ATLAS) {
                for (pack in PacksRoom.loadedPacks) {
                    pack.generateAtlas()
                }
            }
            //Language.validateEnglish();
            main.switchRoom(Menu)
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
        for (list in main.groups.values) {
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
        total -= main.groups[group]!!.size
        checkEverythingFailed()
    }

    fun removeAllGroups() {
        for (group in main.groups.keys) {
            total -= main.groups[group]!!.size
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