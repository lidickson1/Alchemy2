package main

import main.buttons.Button
import main.buttons.ElementButton
import main.buttons.Group
import main.buttons.Pack
import main.combos.MultiCombo
import main.combos.NormalCombo
import main.rooms.Game
import main.rooms.Game.addElement
import main.rooms.Game.history
import main.rooms.Game.removeElement
import main.rooms.Game.success
import main.rooms.GameMode
import main.rooms.Loading.updateProgress
import main.rooms.PacksRoom.loadedPacks
import main.variations.ComboVariation
import main.variations.Variation
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import processing.core.PImage
import java.io.File
import java.util.*

class Element(val id: String, val group: Group, val pack: Pack) : Entity(), Comparable<Element> {

    val tags = ArrayList<String>()
    var description: String? = null
    var isPersistent = false
    var variation: Variation? = null
    //    val randomAppearance: Appearance? = null
    lateinit var image: PImage
    val isImageInitialized: Boolean get() = this::image.isInitialized
    private val namespace: String = id.split(":")[0]
    private val localId: String = id.split(":")[1]

    fun getDisplayName(): String {
        //display names must be computed
        //imagine we have a pack on top of the default pack
        //and its language modifies the display name of an element that is in the default pack
        //since packs load both languages and elements together
        //the default pack gets loaded, so the element gets loaded, and its name is set
        //by the time we load the next pack, the changes made to the language will not reflect in the element's name anymore
        //also animation variation might have a different name per frame
        val variationName: String? = if (variation == null) {
            null
        } else {
            Language.getLanguageSelected().getElementLocalizedString(namespace, variation!!.getName())
        }
        return variationName ?: getDisplayNameWithoutVariation()
    }

    fun getDisplayNameWithoutVariation(): String {
        val displayName = Language.getLanguageSelected().getElementLocalizedString(namespace, localId)
        if (displayName == null) {
            pack.generateEnglish(localId)
        }
        return displayName ?: id
    }

    //TODO: for atlas
    fun getImages(): List<Pair<PImage, String>> {
        val list = ArrayList<Pair<PImage, String>>()
        if (image !== Button.error) {
            list.add(image to id)
        }
        variation?.let {
            list.addAll(it.getPairs())
        }
        return list
    }

    override fun compareTo(other: Element): Int {
        return id.compareTo(other.id)
    }

    //file name without extension
    fun loadImage(fileName: String): PImage {
        //check if a pack has the image, from top to bottom
        for (pack in loadedPacks) {
            //check for atlas first
            if (pack.getAtlasImage(fileName) != null) {
                return pack.getAtlasImage(fileName)
            }
            //fileName could be in the form of pack:element:variation because of variations
            val id = if (StringUtils.countMatches(fileName, ":") == 2) fileName.split(":")[2] else fileName
            if (pack.name == "Alchemy" && this.pack.name == "Alchemy") {
                //if the element is of the default pack and we are in the default pack right now, load default location
                val defaultPath = "resources/elements/alchemy/${group.localId}/$id"
                val getImage = getImageFromPath(defaultPath)
                if (getImage == null) {
                    image = Button.error
                } else {
                    getImage.resize(ElementButton.SIZE, 0)
                    return getImage
                }
            } else {
                val packPath = "${pack.path}/elements/${group.pack.namespace}/${group.localId}/$id"
                getImageFromPath(packPath)?.let {
                    it.resize(ElementButton.SIZE, 0)
                    return it
                }
            }
        }
        return Button.error
    }

    private fun getImageFromPath(path: String): PImage? {
        val png = "$path.png"
        if (File(png).exists()) {
            return main.loadImage(png)
        }
        //https://stackoverflow.com/questions/54443002/java-splitting-gif-image-in-bufferedimages-gives-malformed-images
        //TODO: gif -> animated frames -> AnimationVariation
        //problem is AnimationVariation itself also calls this method lmao, recursive animation?? lol
        val gif = "$path.gif"
        return if (File(gif).exists()) {
            main.loadImage(gif)
        } else null
    }

    fun getImageWithoutFallback(fileName: String): PImage? {
        val image = loadImage(fileName)
        return if (image === Button.error) {
            null
        } else {
            image
        }
    }

    fun addTag(tag: String) {
        tags.add(tag)
    }

    fun isInPack(pack: Pack): Boolean {
        val prefix = pack.namespace + ":"
        return id.length >= prefix.length && id.startsWith(prefix)
    }

    companion object {
        private val elementsA = ArrayList<Element>()
        private val elementsB = ArrayList<Element>()
        var elementSelectedA: Element? = null
        var elementSelectedB: Element? = null
        val elementsSelected = ArrayList<Element>()  //for selecting more than 2 elements
        val elementsCreated = ArrayList<Element>()

        fun reset() {
            elementsA.clear()
            elementsB.clear()
            elementSelectedA = null
            elementSelectedB = null
            elementsSelected.clear()
        }

        fun loadImage(elements: ArrayList<Element>) {
            val thread = Thread {
                for (element in elements) {
                    //load original image
                    if (!element.isImageInitialized) {
                        element.image = element.loadImage(element.localId)
                    }
                    element.variation?.loadImages()
                    updateProgress()
                }
            }
            thread.isDaemon = true
            thread.start()
        }

        fun getElement(name: String): Element? {
            if (!main.elements.containsKey(name)) {
                return null
            }
            if (!main.groups.containsKey(main.elements[name])) {
                System.err.println(main.elements[name]!!.id + " group not found!")
                return null
            }
            for (element in main.groups[main.elements[name]]!!) {
                if (element.id == name) {
                    return element
                }
            }
            return null
        }

        fun checkForMultiCombos(): Boolean {
            //reset disabled
//            if (Group.groupSelectedA != null) {
//                for (element in elementsA) {
//                    element.setDisabled(false)
//                }
//            }
//            if (main.buttons.Group.groupSelectedB != null) {
//                for (element in elementsB) {
//                    element.setDisabled(false)
//                }
//            }
            val elementsSelectedString = elementsSelected.map { it.id }
            elementsCreated.clear()
            for (combo in main.comboList) {
                if (combo is MultiCombo) {
                    if (CollectionUtils.isEqualCollection(combo.ingredients, elementsSelectedString)) {
                        val element: Element = getElement(combo.element)!!
                        (element.variation as? ComboVariation)?.setCurrentImage(combo)
                        for (i in 0 until combo.amount) {
                            elementsCreated.add(element)
                        }
                        history.add(combo)
                    }
                }
            }
            for (randomCombo in main.randomCombos) {
                randomCombo.canCreate(elementsSelectedString)?.let {
                    val randomElements = randomCombo.elements
                    elementsCreated.addAll(randomElements)
                    for (element in randomElements) {
                        (element.variation as? ComboVariation)?.setCurrentImage(it)
                        history.add(MultiCombo(element.id, it.ingredients))
                    }
                }
            }
            if (elementsCreated.isNotEmpty()) {
                for (element in elementsCreated) {
                    addElement(element)
                }
                if (Game.mode === GameMode.PUZZLE) {
                    for (element in elementsSelected) {
                        if (!element.isPersistent) {
                            removeElement(element.id)
                        }
                    }
                }

                success()
                elementsSelected.clear()
                return true
            } else {
                return false
            }
        }

        fun checkForCombos(): Boolean {
            assert(elementSelectedA != null && elementSelectedB != null)
            //check for combos
            elementsCreated.clear()
            for (combo in main.comboList) {
                if (combo is NormalCombo) {
                    if (combo.a == elementSelectedA!!.id && combo.b == elementSelectedB!!.id || combo.a == elementSelectedB!!.id && combo.b == elementSelectedA!!.id) {
                        val element = getElement(combo.element)!!
                        (element.variation as? ComboVariation)?.setCurrentImage(combo)
                        for (i in 0 until combo.getAmount()) {
                            elementsCreated.add(element)
                        }
                        history.add(combo)
                    }
                }
            }
            for (randomCombo in main.randomCombos) {
                randomCombo.canCreate(elementSelectedA, elementSelectedB)?.let {
                    val randomElements = randomCombo.elements
                    elementsCreated.addAll(randomElements)
                    for (element in randomElements) {
                        (element.variation as? ComboVariation)?.setCurrentImage(it)
                        history.add(NormalCombo(element.id, it.a, it.b))
                    }
                }
            }
            if (elementsCreated.isNotEmpty()) {
                for (element in elementsCreated) {
                    //element adding conditions has been refactored into the method
                    addElement(element)
                }
                if (Game.mode === GameMode.PUZZLE) {
                    if (!elementSelectedA!!.isPersistent) {
                        removeElement(elementSelectedA!!.id)
                    }
                    if (!elementSelectedB!!.isPersistent) {
                        removeElement(elementSelectedB!!.id)
                    }
                }

                success()
                elementSelectedA = null
                elementSelectedB = null
                return true
            } else {
                return false
            }
        }

    }
}