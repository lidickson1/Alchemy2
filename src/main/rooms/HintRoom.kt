package main.rooms

import main.Element
import main.Language
import main.Main
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
        JOptionPane.showMessageDialog(null, Language.languageSelected.getLocalizedString("hint", "all discovered"), Language.languageSelected.getLocalizedString("misc", "information"), JOptionPane.INFORMATION_MESSAGE)
    }

    override fun setup() {}

    override fun draw() {
        drawTitle("hint", "hints")
        Main.fill(255)
        Main.textAlign(PConstants.CENTER)
        Main.textSize(30f)
        Main.text(Language.languageSelected.getLocalizedString("hint", "time reMaining"), Main.screenWidth / 2f, 250f)
        Main.text(Game.getTimeString(), Main.screenWidth / 2f, 300F)
        val gap = 300
        val width: Float = (Main.screenWidth - gap * 3) / 2f
        Main.textLeading(20f)
        Main.textSize(20f)
        Main.fill(255)
        Main.text(Language.languageSelected.getLocalizedString("hint", "element hint"), Main.screenWidth / 2f - gap / 2f - width / 2, 500f)
        elementHint.draw(Main.screenWidth / 2f - gap / 2f - width / 2 - IconButton.SIZE / 2f, 530f)
        elementHint.setDisabled(!Game.isHintReady())
        Main.fill(255) //need to reset colour because button in bounds cause colour change
        Main.text(Language.languageSelected.getLocalizedString("hint", "group hint"), Main.screenWidth / 2f + gap / 2f + width / 2, 500f)
        groupHint.draw(Main.screenWidth / 2f + gap / 2f + width / 2 - IconButton.SIZE / 2f, 530f)
        groupHint.setDisabled(!Game.isHintReady())
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
        for (combo in Main.comboList) {
            if (combo.canCreate() && !Game.isDiscovered(combo.element)) {
                possibleElements.add(combo.element)
            }
        }
        for (randomCombo in Main.randomCombos) {
            if (randomCombo.canCreate()) {
                possibleElements.addAll(randomCombo.allResults.filter { !Game.isDiscovered(it) })
            }
        }
        if (possibleElements.isEmpty()) {
            throw NoHintAvailable()
        }
        Game.setHintElement(Element.getElement(possibleElements.random())!!)
        Main.switchRoom(Game)
    }

    //TODO: MultiCombo with ingredients that just happen to only be in 2 groups
    @Throws(NoHintAvailable::class)
    private fun getGroupHint() {
        val possibleCombos = ArrayList<NormalCombo>()
        for (combo in Main.comboList) {
            if (combo is NormalCombo && combo.canCreate() && !Game.isDiscovered(combo.getElement())) {
                possibleCombos.add(combo)
            }
        }
        for (randomCombo in Main.randomCombos) {
            if (randomCombo.canCreate() && randomCombo.notAllResultsDiscovered()) {
                possibleCombos.addAll(randomCombo.canCreate)
            }
        }
        if (possibleCombos.isEmpty()) {
            throw NoHintAvailable()
        }
        val randomCombo = possibleCombos.random()
        Group.setHintGroups(Main.elements[randomCombo.a]!!, Main.elements[randomCombo.b]!!)
        Main.switchRoom(Game)
    }

    internal class NoHintAvailable : Exception()
}