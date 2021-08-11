package main.rooms

import main.Element
import main.Language
import main.buttons.Arrow
import main.buttons.ElementButton
import main.buttons.Group
import main.buttons.iconbuttons.Exit
import main.combos.MultiCombo
import main.combos.NormalCombo
import main.rooms.Game.isDiscovered
import org.apache.commons.lang3.tuple.ImmutableTriple
import processing.core.PConstants
import kotlin.math.ceil
import kotlin.math.min

object ElementRoom : Room() {

    private val creationLeftArrow: Arrow
    private val creationRightArrow: Arrow
    private val usedLeftArrow: Arrow
    private val usedRightArrow: Arrow
    private val exit: Exit = Exit()
    val element: Element get() = elementButton.element
    lateinit var elementButton: ElementButton
    private val creation = mutableListOf<ImmutableTriple<ElementButton?, ElementButton, ElementButton?>>()
    private val used = mutableListOf<ImmutableTriple<ElementButton?, ElementButton, ElementButton?>>()

    //when the element cannot be created, return an empty list
    private val creationTriples: List<ImmutableTriple<ElementButton?, ElementButton, ElementButton?>>
        get() {
            //when the element cannot be created, return an empty list
            if (creationTotalPages == 0) {
                creationPageNumber = 0
                return emptyList()
            }
            if (creationPageNumber >= creationTotalPages) {
                creationPageNumber = creationTotalPages - 1
            }
            return creation.slice(creationPageNumber * max until min(creation.size, (creationPageNumber + 1) * max))
        }

    //when the element has no uses, return an empty list
    private val usedTriples: List<ImmutableTriple<ElementButton?, ElementButton, ElementButton?>>
        get() {
            //when the element has no uses, return an empty list
            if (usedTotalPages == 0) {
                usedPageNumber = 0
                return emptyList()
            }
            if (usedPageNumber >= usedTotalPages) {
                usedPageNumber = usedTotalPages - 1
            }
            return used.slice(usedPageNumber * max until min(used.size, (usedPageNumber + 1) * max))
        }

    private const val GAP = 30

    private var creationPageNumber = 0
    private var creationTotalPages = 0
    private var usedPageNumber = 0
    private var usedTotalPages = 0
    private var max = 0

    init {
        creationLeftArrow = object : Arrow(LEFT) {
            override fun canDraw(): Boolean {
                return creationPageNumber > 0
            }

            override fun clicked() {
                if (canDraw()) {
                    creationPageNumber--
                }
            }
        }
        creationRightArrow = object : Arrow(RIGHT) {
            override fun canDraw(): Boolean {
                return creationPageNumber < creationTotalPages - 1
            }

            override fun clicked() {
                if (canDraw()) {
                    creationPageNumber++
                }
            }
        }
        usedLeftArrow = object : Arrow(LEFT) {
            override fun canDraw(): Boolean {
                return usedPageNumber > 0
            }

            override fun clicked() {
                if (canDraw()) {
                    usedPageNumber--
                }
            }
        }
        usedRightArrow = object : Arrow(RIGHT) {
            override fun canDraw(): Boolean {
                return usedPageNumber < usedTotalPages - 1
            }

            override fun clicked() {
                if (canDraw()) {
                    usedPageNumber++
                }
            }
        }
    }

    override fun setup() {
        //making deep copies or else in bounds method will fuck up
        creation.clear()
        used.clear()
        //no else ifs because the element can be both
        for (combo in main.comboList) {
            if (combo is NormalCombo) {
                if (combo.element == element.id && combo.ingredientsDiscovered()) {
                    creation.addAll(combo.toTriples())
                }
                if ((combo.a == element.id || combo.b == element.id) && combo.ingredientsDiscovered() && isDiscovered(combo.element)) {
                    //ingredients must be discovered too
                    used.addAll(combo.toTriples())
                }
            } else if (combo is MultiCombo) {
                if (combo.element == element.id && combo.ingredientsDiscovered()) {
                    creation.addAll(combo.toTriples())
                }
                if (combo.ingredients.contains(element.id) && combo.ingredientsDiscovered() && isDiscovered(combo.element)) {
                    used.addAll(combo.toTriples())
                }
            }
        }
        for (combo in main.randomCombos) {
            if (combo.isResult(element.id)) {
                creation.addAll(combo.toCreationTriples(element))
            }
            if (combo.isIngredient(element.id)) {
                used.addAll(combo.toUsedTriples(element))
            }
        }
        creationPageNumber = 0
        usedPageNumber = 0
    }

    override fun draw() {
        ElementButton.touching = null
        var y = Group.GROUP_Y
        elementButton.draw(Group.GROUP_X.toFloat(), y.toFloat())
        if (element.description != null) {
            val x = Group.GROUP_X + ElementButton.SIZE + 40
            val width = main.screenWidth - Group.GROUP_X - x
            main.textAlign(PConstants.LEFT)
            main.textLeading(20f)
            main.fill(255)
            main.text(element.description, x.toFloat(), (y + 10).toFloat(), width.toFloat(), main.screenHeight.toFloat())
        }
        y += ElementButton.HEIGHT + 40
        var x = (main.screenWidth / 2 - Group.GAP * 2) / 2
        main.textAlign(PConstants.CENTER)
        main.fill(255)
        main.textSize(20f)
        main.text(Language.getLanguageSelected().getLocalizedString("information", "creation"), x.toFloat(), y.toFloat())
        y += 40
        max = Math.floorDiv(main.screenHeight - y - 40 - Arrow.SIZE, ElementButton.HEIGHT + GAP)
        val length = ElementButton.SIZE + GAP + plus.width + GAP + ElementButton.SIZE + GAP + equal.width + GAP + ElementButton.SIZE
        var start = x - length / 2
        creationTotalPages = ceil((creation.size.toFloat() / max).toDouble()).toInt()
        usedTotalPages = ceil((used.size.toFloat() / max).toDouble()).toInt()
        var triples = creationTriples
        for ((index, triple) in triples.withIndex()) {
            x = start
            if (index > 0 && triples[index - 1].right == null && triple.left != null) {
                main.image(plus, (x - ElementButton.SIZE - GAP).toFloat(), y.toFloat())
            }
            triple.left?.draw(x.toFloat(), y.toFloat())
            x += ElementButton.SIZE + GAP
            main.image(plus, x.toFloat(), y.toFloat())
            x += plus.width + GAP
            triple.middle.draw(x.toFloat(), y.toFloat())
            x += ElementButton.SIZE + GAP
            if (triple.right != null) {
                main.image(equal, x.toFloat(), y.toFloat())
                x += equal.width + GAP
                triple.right!!.draw(x.toFloat(), y.toFloat())
            }
            y += ElementButton.HEIGHT + GAP
        }

        //ensures that the button is always drawn at the bottom, even when there are not enough triples
        x = start
        y = Group.GROUP_Y + ElementButton.HEIGHT + 40 + 40 + (ElementButton.HEIGHT + GAP) * max
        creationLeftArrow.draw(x.toFloat(), y.toFloat())
        creationRightArrow.draw((x + length - Arrow.SIZE).toFloat(), y.toFloat())
        x = (main.screenWidth / 2 - Group.GAP * 2) / 2 + main.screenWidth / 2
        y = Group.GROUP_Y + ElementButton.HEIGHT + 40
        main.textAlign(PConstants.CENTER)
        main.fill(255)
        main.textSize(20f)
        main.text(Language.getLanguageSelected().getLocalizedString("information", "used to create"), x.toFloat(), y.toFloat())
        start += main.screenWidth / 2
        y += 40
        triples = usedTriples
        for ((index, triple) in triples.withIndex()) {
            x = start
            if (index > 0 && triples[index - 1].right == null && triple.left != null) {
                main.image(plus, (x - ElementButton.SIZE - GAP).toFloat(), y.toFloat())
            }
            triple.left?.draw(x.toFloat(), y.toFloat())
            x += ElementButton.SIZE + GAP
            main.image(plus, x.toFloat(), y.toFloat())
            x += plus.width + GAP
            triple.middle.draw(x.toFloat(), y.toFloat())
            x += ElementButton.SIZE + GAP
            if (triple.right != null) {
                main.image(equal, x.toFloat(), y.toFloat())
                x += equal.width + GAP
                triple.right!!.draw(x.toFloat(), y.toFloat())
            }
            y += ElementButton.HEIGHT + GAP
        }
        ElementButton.drawTooltip()
        x = start
        y = Group.GROUP_Y + ElementButton.HEIGHT + 40 + 40 + (ElementButton.HEIGHT + GAP) * max
        usedLeftArrow.draw(x.toFloat(), y.toFloat())
        usedRightArrow.draw((x + length - Arrow.SIZE).toFloat(), y.toFloat())
        exit.draw()
    }

    override fun mousePressed() {
        creationLeftArrow.mousePressed()
        creationRightArrow.mousePressed()
        usedLeftArrow.mousePressed()
        usedRightArrow.mousePressed()
        exit.mousePressed()
        for (triple in creationTriples) {
            triple.left?.mousePressed()
            triple.middle.mousePressed()
            triple.right?.mousePressed()
        }
        for (triple in usedTriples) {
            triple.left?.mousePressed()
            triple.middle.mousePressed()
            triple.right?.mousePressed()
        }
    }
}