package main.rooms

import main.buttons.Arrow
import main.buttons.ElementButton
import main.buttons.iconbuttons.Exit
import org.apache.commons.lang3.tuple.ImmutableTriple
import kotlin.math.ceil
import kotlin.math.min

object HistoryRoom : Room() {

    private val exit: Exit = Exit()
    private val leftArrow: Arrow
    private val rightArrow: Arrow
    private val triples = mutableListOf<ImmutableTriple<ElementButton?, ElementButton, ElementButton?>>()
    private const val GAP = 30
    private var pageNumber = 0
    private var totalPages = 0

    init {
        leftArrow = object : Arrow(LEFT) {
            override fun canDraw(): Boolean {
                return pageNumber > 0
            }

            override fun clicked() {
                if (canDraw()) {
                    pageNumber--
                }
            }
        }
        rightArrow = object : Arrow(RIGHT) {
            override fun canDraw(): Boolean {
                return pageNumber < totalPages - 1
            }

            override fun clicked() {
                if (canDraw()) {
                    pageNumber++
                }
            }
        }
    }

    override fun setup() {
        pageNumber = 0
        triples.clear()
        for (combo in Game.history) {
            triples.addAll(combo.toTriples())
        }
    }

    override fun draw() {
        drawTitle("history", "history")
        val length = ElementButton.SIZE + GAP + plus.width + GAP + ElementButton.SIZE + GAP + equal.width + GAP + ElementButton.SIZE
        var x: Int
        var y = 150
        val max = Math.floorDiv(main.screenHeight - y - 40 - Arrow.SIZE, ElementButton.HEIGHT + GAP)
        totalPages = ceil((triples.size.toFloat() / max).toDouble()).toInt()
        if (totalPages == 0) {
            pageNumber = 0
        } else if (pageNumber >= totalPages) {
            pageNumber = totalPages - 1
        }
        for ((index, triple) in triples.subList(pageNumber * max, min(triples.size, (pageNumber + 1) * max)).withIndex()) {
            x = main.screenWidth / 2 - length / 2
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
        x = main.screenWidth / 2 - length / 2
        leftArrow.draw(x.toFloat(), (main.screenHeight - Arrow.SIZE - 30).toFloat())
        rightArrow.draw((x + length - Arrow.SIZE).toFloat(), (main.screenHeight - Arrow.SIZE - 30).toFloat())
        exit.draw()
    }

    override fun mousePressed() {
        leftArrow.mousePressed()
        rightArrow.mousePressed()
        exit.mousePressed()
    }
}