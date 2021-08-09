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
import main.variations.RandomVariation
import main.variations.Variation
import main.variations.appearances.Appearance
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.ImmutablePair
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

    val displayName: String

    init {
        val variationName: String? = if (variation == null) {
            null
        } else {
            Language.getLanguageSelected().getElementLocalizedString(namespace, variation!!.getName())
        }
        val displayName = Language.getLanguageSelected().getElementLocalizedString(namespace, localId)
        if (displayName == null) {
            pack.generateEnglish(localId)
        }
        this.displayName = variationName ?: displayName ?: id
    }

    //TODO: for atlas
    val images: List<ImmutablePair<PImage, String>>
        get() {
            val list = ArrayList<ImmutablePair<PImage, String>>()
            if (image !== Button.error) {
                list.add(ImmutablePair(image, id))
            }
            variation?.let {
                list.addAll(it.pairs)
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
                val defaultPath = "resources/elements/alchemy/" + group.localId + "/" + id
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

    private fun isDepleted(selected: List<Element>): Boolean {
//        var count = 0
//        for (element in discovered[group]!!) {
//            if (element.id == id) {
//                count++
//            }
//        }
//        for (element in selected) {
//            if (element != null && element.id == id) {
//                count--
//            }
//        }
//        return count <= 0
        return false
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
            val elementsSelectedString = elementsSelected.map{it.id}
            elementsCreated.clear()
            for (combo in main.comboList) {
                if (combo is MultiCombo) {
                    if (CollectionUtils.isEqualCollection(combo.ingredients, elementsSelectedString)) {
                        val element: Element = getElement(combo.element)!!
                        if (element.variation is ComboVariation) {
                            (element.variation as ComboVariation?)!!.setCurrentImage(combo)
                        }
                        for (i in 0 until combo.amount) {
                            elementsCreated.add(element)
                        }
                        history.add(combo)
                    }
                }
            }
            for (randomCombo in main.randomCombos) {
                val multiCombo = randomCombo.canCreate(elementsSelectedString)
                if (multiCombo != null) {
                    val randomElements: ArrayList<Element> = randomCombo.elements
                    elementsCreated.addAll(randomElements)
                    for (element in randomElements) {
                        if (element.variation is ComboVariation) {
                            (element.variation as ComboVariation?)!!.setCurrentImage(multiCombo)
                        }
                        history.add(MultiCombo(element.id, multiCombo.ingredients))
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

                //need to update because affected groups might be already selected
                updateA()
                updateB()
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
                            val element: Element = getElement(combo.element)!!
                            if (Objects.requireNonNull(element).variation is ComboVariation) {
                                (element.variation as ComboVariation?)!!.setCurrentImage(combo)
                            }
                            for (i in 0 until combo.getAmount()) {
                                elementsCreated.add(element)
                            }
                            history.add(combo)
                        }
                    }
                }
                for (randomCombo in main.randomCombos) {
                    val normalCombo = randomCombo.canCreate(elementSelectedA, elementSelectedB)
                    if (normalCombo != null) {
                        val randomElements: ArrayList<Element> = randomCombo.elements
                        elementsCreated.addAll(randomElements)
                        for (element in randomElements) {
                            if (element.variation is ComboVariation) {
                                (element.variation as ComboVariation?)!!.setCurrentImage(normalCombo)
                            }
                            history.add(NormalCombo(element.id, normalCombo.a, normalCombo.b))
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

                    //need to update because affected groups might be already selected
                    updateA()
                    updateB()
                    success()
                    elementSelectedA = null
                    elementSelectedB = null
                    return true
                } else {
                    return false
                }
        }

        fun updateA() {
//            main.Element.Companion.elementsA.clear()
//            //group might be gone if we are in puzzle mode
//            if (main.buttons.Group.groupSelectedA != null && main.buttons.Group.groupSelectedA.exists()) {
//                for (element in discovered[main.buttons.Group.groupSelectedA]!!) {
//                    main.Element.Companion.elementsA.add(element.deepCopy(main.buttons.Group.groupSelectedA, 255, 0))
//                }
//            } else {
//                main.buttons.Group.groupSelectedA = null
//            }
        }

        fun resetA() {
//            pageNumberA = 0
//            main.Element.Companion.elementsA.clear()
//            for (i in discovered[main.buttons.Group.groupSelectedA]!!.indices) {
//                val element: Element = discovered[main.buttons.Group.groupSelectedA]!![i]
//                if (i < maxElements) {
//                    main.Element.Companion.elementsA.add(element.deepCopy(main.buttons.Group.groupSelectedA, 0, ALPHA_CHANGE))
//                } else {
//                    //elements that are not on the first page don't need to fade in
//                    main.Element.Companion.elementsA.add(element.deepCopy(main.buttons.Group.groupSelectedA, 255, 0))
//                }
//            }
//            totalPagesA = Math.ceil(discovered[main.buttons.Group.groupSelectedA]!!.size.toFloat() / maxElements).toInt()
        }

        fun updateB() {
//            main.Element.Companion.elementsB.clear()
//            //group might be gone if we are in puzzle mode
//            if (main.buttons.Group.groupSelectedB != null && main.buttons.Group.groupSelectedB.exists()) {
//                for (element in discovered[main.buttons.Group.groupSelectedB]!!) {
//                    main.Element.Companion.elementsB.add(element.deepCopy(main.buttons.Group.groupSelectedB, 255, 0))
//                }
//            } else {
//                main.buttons.Group.groupSelectedB = null
//            }
        }

        fun resetB() {
//            pageNumberB = 0
//            main.Element.Companion.elementsB.clear()
//            for (i in discovered[main.buttons.Group.groupSelectedB]!!.indices) {
//                val element: Element = discovered[main.buttons.Group.groupSelectedB]!![i]
//                if (i < maxElements) {
//                    main.Element.Companion.elementsB.add(element.deepCopy(main.buttons.Group.groupSelectedB, 0, ALPHA_CHANGE))
//                } else {
//                    //elements that are not on the first page don't need to fade in
//                    main.Element.Companion.elementsB.add(element.deepCopy(main.buttons.Group.groupSelectedB, 255, 0))
//                }
//            }
//            totalPagesB = Math.ceil(discovered[main.buttons.Group.groupSelectedB]!!.size.toFloat() / maxElements).toInt()
        }

        fun hidePagesA() {
//            //this is necessary because the first element clicked can be from group B
//            if (main.Element.Companion.elementSelectedA != null && main.Element.Companion.elementSelectedA.group === main.buttons.Group.groupSelectedA) {
//                main.Element.Companion.elementSelectedA = null
//            } else if (main.Element.Companion.elementSelectedB != null && main.Element.Companion.elementSelectedB.group === main.buttons.Group.groupSelectedA) {
//                main.Element.Companion.elementSelectedB = null
//            }
        }

        fun hidePagesB() {
//            if (main.Element.Companion.elementSelectedA != null && main.Element.Companion.elementSelectedA.group === main.buttons.Group.groupSelectedB) {
//                main.Element.Companion.elementSelectedA = null
//            } else if (main.Element.Companion.elementSelectedB != null && main.Element.Companion.elementSelectedB.group === main.buttons.Group.groupSelectedB) {
//                main.Element.Companion.elementSelectedB = null
//            }
        }
    }
}