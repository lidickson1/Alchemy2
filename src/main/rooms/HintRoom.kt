package main.rooms

import main.Element
import main.Language
import main.buttons.ElementButton
import main.buttons.Group
import main.buttons.iconbuttons.Exit
import main.buttons.iconbuttons.IconButton
import main.combos.NormalCombo
import processing.core.PConstants
import java.util.*
import javax.swing.JOptionPane

object HintRoom : Room() {

    private val elementHint: IconButton
    private val groupHint: IconButton
    private val exit: Exit

    init {
        elementHint = object : IconButton("resources/images/element_hint_button.png") {
            override fun clicked() {
                try {
                    getElementHint()
                    Game.resetHint()
                } catch (noHintAvailable: NoHintAvailable) {
                    showDialog()
                }
            }
        }
        groupHint = object : IconButton("resources/images/group_hint_button.png") {
            override fun clicked() {
                try {
                    getGroupHint()
                    Game.resetHint()
                } catch (noHintAvailable: NoHintAvailable) {
                    showDialog()
                }
            }
        }
        exit = Exit()
    }

    private fun showDialog() {
        JOptionPane.showMessageDialog(null, Language.getLanguageSelected().getLocalizedString("hint", "all discovered"), Language.getLanguageSelected().getLocalizedString("misc", "information"), JOptionPane.INFORMATION_MESSAGE)
    }

    override fun setup() {}

    override fun draw() {
        drawTitle("hint", "hints")
        main.fill(255)
        main.textAlign(PConstants.CENTER)
        main.textSize(30f)
        main.text(Language.getLanguageSelected().getLocalizedString("hint", "time remaining"), main.screenWidth / 2f, 250f)
        main.text(Game.timeString, main.screenWidth / 2f, 300F)
        val gap = 300
        val width: Float = (main.screenWidth - gap * 3) / 2f
        main.textLeading(20f)
        main.textSize(20f)
        main.fill(255)
        main.text(Language.getLanguageSelected().getLocalizedString("hint", "element hint"), main.screenWidth / 2f - gap / 2f - width / 2, 500f)
        elementHint.draw(main.screenWidth / 2f - gap / 2f - width / 2 - IconButton.SIZE / 2f, 530f)
        elementHint.setDisabled(!Game.isHintReady)
        main.fill(255) //need to reset colour because button in bounds cause colour change
        main.text(Language.getLanguageSelected().getLocalizedString("hint", "group hint"), main.screenWidth / 2f + gap / 2f + width / 2, 500f)
        groupHint.draw(main.screenWidth / 2f + gap / 2f + width / 2 - IconButton.SIZE / 2f, 530f)
        groupHint.setDisabled(!Game.isHintReady)
        exit.draw()
    }

    override fun mousePressed() {
        elementHint.mousePressed()
        groupHint.mousePressed()
        exit.mousePressed()
    }

    //TODO: this is not private for debug reasons
    @Throws(NoHintAvailable::class)
    fun getElementHint() {
        val possibleElements = ArrayList<String>()
        for (combo in main.comboList) {
            if (combo.canCreate() && !Game.isDiscovered(combo.element)) {
                possibleElements.add(combo.element)
            }
        }
        for (randomCombo in main.randomCombos) {
            if (randomCombo.canCreate()) {
                possibleElements.addAll(randomCombo.allResults.filter { !Game.isDiscovered(it) })
            }
        }
        if (possibleElements.isEmpty()) {
            throw NoHintAvailable()
        }
        Game.setHintElement(Element.getElement(possibleElements.random())!!)
        main.switchRoom(Game)
    }

    //TODO: MultiCombo with ingredients that just happen to only be in 2 groups
    @Throws(NoHintAvailable::class)
    private fun getGroupHint() {
        val possibleCombos = ArrayList<NormalCombo>()
        for (combo in main.comboList) {
            if (combo is NormalCombo && combo.canCreate() && !Game.isDiscovered(combo.getElement())) {
                possibleCombos.add(combo)
            }
        }
        for (randomCombo in main.randomCombos) {
            if (randomCombo.canCreate() && randomCombo.notAllResultsDiscovered()) {
                possibleCombos.addAll(randomCombo.canCreate)
            }
        }
        if (possibleCombos.isEmpty()) {
            throw NoHintAvailable()
        }
        val randomCombo = possibleCombos.random()
        Group.setHintGroups(main.elements[randomCombo.a]!!, main.elements[randomCombo.b]!!)
        main.switchRoom(Game)
    }

    internal class NoHintAvailable : Exception()
}