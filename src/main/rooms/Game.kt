package main.rooms

import main.Element
import main.Language
import main.buttons.*
import main.buttons.ElementButton
import main.buttons.iconbuttons.Exit
import main.buttons.iconbuttons.IconButton
import main.buttons.iconbuttons.Save
import main.combos.Combo
import main.rooms.HintRoom.NoHintAvailable
import main.rooms.PacksRoom.loadedPacks
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.time.DurationFormatUtils
import processing.core.PConstants
import processing.data.JSONObject
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.LocalDateTime
import javax.swing.JOptionPane
import kotlin.collections.ArrayList
import kotlin.math.min

object Game : Room() {

    val discovered = sortedMapOf<Group, ArrayList<Element>>()
    val history = mutableListOf<Combo>()
    var saveFile: SaveFile? = null
    private val success: Pane
    private val hint: Pane
    private val save: Save
    private val exit: Exit
    private val historyButton: IconButton
    private val hintButton: IconButton
    private val undoButton: IconButton
    private val groupLeftArrow: Arrow
    private val groupRightArrow: Arrow
    private val elementAUpArrow: Arrow
    private val elementADownArrow: Arrow
    private val elementBUpArrow: Arrow
    private val elementBDownArrow: Arrow
    private lateinit var hintTime: LocalDateTime //time of next hint
    var gameLoaded = false
    private var hintElement: ElementButton? = null

    @JvmField
    var mode = GameMode.NORMAL
    val isHintReady: Boolean
        get() = Duration.between(LocalDateTime.now(), hintTime).isZero || Duration.between(LocalDateTime.now(), hintTime).isNegative

    //TODO: maybe separate the update time logic?
    fun getTimeString(): String {
        var duration = Duration.between(LocalDateTime.now(), hintTime)
        if (duration.isNegative) {
            duration = Duration.ZERO
        }
        return DurationFormatUtils.formatDuration(duration.toMillis(), "mm:ss")
    }

    private val numberOfElements: Int
         get() {
            var sum = 0
            for (list in discovered.values) {
                sum += list.size
            }
            return sum
        }

    private const val gap: String = "          "
    private val isShiftHeld: Boolean get() = main.keyPressed && main.keyCode == PConstants.SHIFT

    init {
        success = object : Pane() {
            override fun getText(): String {
                return Language.getLanguageSelected().getLocalizedString("game", "you created")
            }
        }
        hint = object : Pane() {
            override fun getText(): String {
                return Language.getLanguageSelected().getLocalizedString("game", "element hint")
            }
        }
        save = Save()
        exit = Exit()
        groupLeftArrow = object : Arrow(LEFT) {
            override fun canDraw(): Boolean {
                return Group.pageNumber > 0
            }

            override fun clicked() {
                if (canDraw()) {
                    Group.pageNumber--
                }
            }
        }
        groupRightArrow = object : Arrow(RIGHT) {
            override fun canDraw(): Boolean {
                return Group.pageNumber < Group.totalPages - 1
            }

            override fun clicked() {
                if (canDraw()) {
                    Group.pageNumber++
                }
            }
        }
        elementAUpArrow = object : Arrow(UP) {
            override fun canDraw(): Boolean {
                return Group.groupSelectedA != null && ElementButton.pageNumberA > 0
            }

            override fun clicked() {
                if (canDraw()) {
                    ElementButton.pageNumberA--
                }
            }
        }
        elementADownArrow = object : Arrow(DOWN) {
            override fun canDraw(): Boolean {
                return Group.groupSelectedA != null && ElementButton.pageNumberA < ElementButton.totalPagesA - 1
            }

            override fun clicked() {
                if (canDraw()) {
                    ElementButton.pageNumberA++
                }
            }
        }
        elementBUpArrow = object : Arrow(UP) {
            override fun canDraw(): Boolean {
                return Group.groupSelectedB != null && ElementButton.pageNumberB > 0
            }

            override fun clicked() {
                if (canDraw()) {
                    ElementButton.pageNumberB--
                }
            }
        }
        elementBDownArrow = object : Arrow(DOWN) {
            override fun canDraw(): Boolean {
                return Group.groupSelectedB != null && ElementButton.pageNumberB < ElementButton.totalPagesB - 1
            }

            override fun clicked() {
                if (canDraw()) {
                    ElementButton.pageNumberB++
                }
            }
        }
        historyButton = object : IconButton("resources/images/history_button.png") {
            override fun clicked() {
                main.switchRoom(HistoryRoom)
            }
        }
        hintButton = object : IconButton("resources/images/hint_button.png") {
            override fun clicked() {
                main.switchRoom(HintRoom)
            }
        }
        undoButton = object : IconButton("resources/images/undo_button.png") {
            override fun clicked() {
                undo()
            }
        }
    }

    override fun setup() {
        //this ensures that the following code only runs when initially entering game
        if (!gameLoaded) {
            discovered.clear()
            history.clear()
            Group.reset()
            Element.reset()
            ElementButton.reset()
            if (saveFile == null) { //new game
                mode = GameMode.NORMAL
                val elements = ArrayList<Element>()
                for (pack in loadedPacks) {
                    pack.getStartingElements(elements)
                }
                for (element in elements) {
                    this.addElement(element)
                }
                hintTime = LocalDateTime.now().plusMinutes(3)
            } else {
                mode = if (saveFile!!.json.hasKey("mode")) {
                    try {
                        GameMode.valueOf(saveFile!!.json.getString("mode"))
                    } catch (e: IllegalArgumentException) {
                        GameMode.NORMAL
                    }
                } else {
                    GameMode.NORMAL
                }
                val array = saveFile!!.json.getJSONArray("elements").stringArray
                for (element in array) {
                    if (!this.addElement(element)) {
                        System.err.println("Error: $element could not be loaded from save!")
                        LoadGame.failed()
                        return
                    }
                }
                hintTime = if (saveFile!!.json.hasKey("hint time")) LocalDateTime.now().plus(Duration.parse(saveFile!!.json.getString("hint time"))) else LocalDateTime.now().plusMinutes(3)
            }
            gameLoaded = true
            success.isActive = false
            hint.isActive = false
        }
    }

    fun exitGame() {
        if (saveFile != null && saveFile!!.json.getJSONArray("elements").size() < numberOfElements) {
            val result = JOptionPane.showConfirmDialog(null, Language.getLanguageSelected().getLocalizedString("game", "not saved"), Language.getLanguageSelected().getLocalizedString("misc", "warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)
            if (result == JOptionPane.CANCEL_OPTION) {
                return
            }
        }
        main.switchRoom(Menu)
    }

    override fun draw() {
        main.textFont(main.font, 20f)
        main.fill(255)
        main.textAlign(PConstants.LEFT, PConstants.TOP)
        main.text(Language.getLanguageSelected().getLocalizedString("game", "elements") + ": " + numberOfElements + gap +
                Language.getLanguageSelected().getLocalizedString("game", "groups") + ": " + discovered.keys.size + gap +
                Language.getLanguageSelected().getLocalizedString("game", "hint timer") + ": " + getTimeString(), 10f, 10f)
        Group.drawGroups()
        ElementButton.drawElements()
        groupLeftArrow.draw(Group.GROUP_X.toFloat(), (Group.GROUP_Y + (Group.SIZE + Group.GAP) * Group.groupCountY).toFloat())
        groupRightArrow.draw(((Group.SIZE + Group.GAP) * Group.groupCountX - Arrow.SIZE).toFloat(), (Group.GROUP_Y + (Group.SIZE + Group.GAP) * Group.groupCountY).toFloat())
        elementAUpArrow.draw((main.screenWidth - 20 - Arrow.SIZE).toFloat(), Group.groupSelectedAY.toFloat())
        elementADownArrow.draw((main.screenWidth - 20 - Arrow.SIZE).toFloat(), (Group.groupSelectedBY - Group.GAP - Arrow.SIZE).toFloat())
        elementBUpArrow.draw((main.screenWidth - 20 - Arrow.SIZE).toFloat(), Group.groupSelectedBY.toFloat())
        elementBDownArrow.draw((main.screenWidth - 20 - Arrow.SIZE).toFloat(), (Group.groupSelectedBY * 2 - Group.GAP - Arrow.SIZE - Group.GROUP_Y).toFloat())
        if (success.isActive) {
            success.draw(main.screenWidth / 2f - success.width / 2f, main.screenHeight / 2f - success.height / 2f)
            ElementButton.drawCreatedElements()
        }
        if (hint.isActive) {
            hint.draw(main.screenWidth / 2f - hint.width / 2f, main.screenHeight / 2f - hint.height / 2f)
            ElementButton.drawHintElement(hintElement!!)
        }

        //clear multi-select
        if (!isShiftHeld) {
            if (Element.elementsSelected.isNotEmpty()) {
                Element.checkForMultiCombos()
            }
            ElementButton.elementsSelected.clear()
            Element.elementsSelected.clear()
            val x = main.screenWidth - IconButton.GAP - IconButton.SIZE
            val y = main.screenHeight - IconButton.GAP - IconButton.SIZE
            exit.draw()
            save.draw((x - (IconButton.SIZE + IconButton.GAP)).toFloat(), y.toFloat())
            historyButton.draw((x - (IconButton.SIZE + IconButton.GAP) * 2).toFloat(), y.toFloat())
            hintButton.draw((x - (IconButton.SIZE + IconButton.GAP) * 3).toFloat(), y.toFloat())
            if (mode == GameMode.PUZZLE) {
                undoButton.draw((x - (IconButton.SIZE + IconButton.GAP) * 4).toFloat(), y.toFloat())
            }
        }
    }

    fun saveGame() {
        if (saveFile == null) {
            saveFile = SaveFile(SaveRoom.saveName, JSONObject())
        }
        saveFile!!.json.put("elements", discovered.values.flatten().map { it.id }.toTypedArray())
        saveFile!!.json.put("last modified", LocalDateTime.now().format(main.formatter))
        saveFile!!.json.put("hint time", Duration.between(LocalDateTime.now(), hintTime).toString())
        main.saveJSONObject(saveFile!!.json, "resources/saves/${saveFile!!.name}.json", "indent=4")
    }

    private fun addElement(name: String): Boolean {
        val element: Element = Element.getElement(name) ?: return false
        this.addElement(element)
        return true
    }

    fun isDiscovered(id: String): Boolean {
        val group: Group = main.elements[id]!!
        if (!discovered.containsKey(group)) {
            return false
        }
        for (e in discovered[group]!!) {
            if (e.id == id) {
                return true
            }
        }
        return false
    }

    private fun undo() {
        if (history.isNotEmpty()) {
            val ingredients = history.last().ingredients
            val created = ArrayList<String>()
            //we need to get all combos that had the same ingredients, because the same ingredients can trigger multiple combos
            var i = history.lastIndex
            for ((index, combo) in history.reversed().withIndex()) {
                if (CollectionUtils.isEqualCollection(ingredients, combo.ingredients)) {
                    for (j in 0 until combo.amount) {
                        created.add(combo.element)
                    }
                } else {
                    i = index
                    break
                }
            }
            //remove from history
            for (j in history.lastIndex..i) {
                history.removeLast()
            }
            for (ingredient in ingredients) {
                if (!Element.getElement(ingredient)!!.isPersistent) {
                    this.addElement(ingredient)
                }
            }
            for (element in created) {
                removeElement(element)
            }
            ElementButton.updateA()
            ElementButton.updateB()
        }
    }

    fun addElement(element: Element) {
        val group = element.group
        val addElement = if (mode == GameMode.NORMAL) {
           !discovered.containsKey(group) || !isDiscovered(element.id)
        } else {
            !(element.isPersistent && discovered.containsKey(group) && isDiscovered(element.id))
        }
        if (addElement) {
            if (!discovered.containsKey(group)) {
                discovered[group] = ArrayList()
            }
            discovered[group]!!.add(element)
        }
        discovered[group]!!.sort()
    }

    //direct Element objects cannot be used because they are copies
    fun removeElement(id: String) {
        val group: Group = main.elements[id]!!
        //edge case: if the elements used are the same element, and they are the last remaining elements of the group
        //then when the first element is removed, the group will be removed, so when we call this method for the second element, the group will be removed already
        discovered[group]?.let {
            //using iterator here because we are only removing one occurrence
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                val element = iterator.next()
                if (element.id == id) {
                    iterator.remove()
                    break
                }
            }
            if (it.isEmpty()) {
                discovered.remove(group)
            }
        }
    }

    override fun mousePressed() {
        exit.mousePressed()
        save.mousePressed()
        historyButton.mousePressed()
        hintButton.mousePressed()
        undoButton.mousePressed()
        groupLeftArrow.mousePressed()
        groupRightArrow.mousePressed()
        elementAUpArrow.mousePressed()
        elementADownArrow.mousePressed()
        elementBUpArrow.mousePressed()
        elementBDownArrow.mousePressed()
        if (!success.isActive && !hint.isActive) {
            for (group in Group.getCurrentPageGroups()) {
                group.mousePressed()
            }
            Group.groupSelectedA?.let {
                it.mousePressed()
                for (element in ElementButton.getCurrentPageElements(ElementButton.elementButtonsA, ElementButton.pageNumberA)) {
                    element.mousePressed()
                }
            }
            Group.groupSelectedB?.let {
                it.mousePressed()
                for (element in ElementButton.getCurrentPageElements(ElementButton.elementButtonsB, ElementButton.pageNumberB)) {
                    element.mousePressed()
                }
            }
            ElementButton.checkForCombos()
        } else {
            success.mousePressed()
            hint.mousePressed()
        }
    }

    override fun keyPressed() {
        if (main.key.toInt() == PConstants.CODED) {
            if (main.keyCode == PConstants.LEFT) {
                groupLeftArrow.clicked()
            } else if (main.keyCode == PConstants.RIGHT) {
                groupRightArrow.clicked()
            } else if (main.keyCode == PConstants.UP) {
                try {
                    HintRoom.getElementHint()
                } catch (ignored: NoHintAvailable) {
                }
            }
        } else if (main.key == 'c') {
            for (element in main.elements.keys) {
                this.addElement(element)
            }
        }
    }

    fun success() {
        success.isActive = true
    }

    fun setHintElement(element: Element) {
        this.hintElement = ElementButton(element)
        hint.isActive = true
    }

    fun resetHint() {
        hintTime = LocalDateTime.now().plusMinutes(3)
    }

}

enum class GameMode {
    NORMAL,
    PUZZLE
}