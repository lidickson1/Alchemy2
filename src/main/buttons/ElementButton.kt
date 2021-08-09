package main.buttons

import main.Element
import main.rooms.ElementRoom
import main.rooms.ElementRoom.elementButton
import main.rooms.Game
import main.rooms.Game.discovered
import main.rooms.GameMode
import processing.core.PConstants
import processing.core.PImage
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

class ElementButton(val element: Element) : Button(SIZE, HEIGHT) {

    private var alpha = 255
    private var alphaChange = 0
    private val shortenedDisplayName: String

    init {
        tintOverlay = false

        var displayName = element.displayName
        while (main.textWidth("$displayName...") >= SIZE) {
            displayName = displayName.substring(0, displayName.length - 1)
        }
        shortenedDisplayName = "$displayName..."
    }

    private fun getDrawnImage(): PImage {
        return element.variation?.getImage() ?: element.image
    }

    override fun drawButton() {
        main.image(getDrawnImage(), x, y)
        main.fill(if (main.settings.getBoolean("group colour")) element.group.colour else 255, alpha.toFloat())
        main.textAlign(PConstants.CENTER)
        val drawTooltip: Boolean = main.setFontSize(element.displayName, 20, SIZE)
        if (touching == null && inBounds() && drawTooltip) {
            touching = this
        }
        main.text(if (drawTooltip) shortenedDisplayName else element.displayName, x + SIZE / 2f, y + SIZE + 22)
        main.fill(255)
        if (main.room is Game && (elementButtonSelectedA === this || elementButtonSelectedB === this)) {
            if (failed()) {
                main.stroke(255f, 0f, 0f)
            } else {
                main.stroke(255)
            }
            main.noFill()
            main.rect(x, y, SIZE.toFloat(), HEIGHT.toFloat())
        }

        //if failed timer passed failed time limit and it didn't get reset yet
        if (main.millis() - time > FAILED_TIME && time != -1L) {
            time = -1 //reset timer
            elementButtonSelectedA = null
            elementButtonSelectedB = null
            Element.elementSelectedA = null
            Element.elementSelectedB = null
        }
        main.noStroke()
    }

    override fun clicked() {
        if (main.mouseButton == PConstants.LEFT) {
            if (main.room is Game) {
                if (!failed()) {
                    if (main.keyPressed && main.keyCode == PConstants.SHIFT) {
                        //these conditions must be separate, or else when it reaches max, it does normal select
                        val max = Math.floorDiv(main.screenWidth - Group.groupSelectedX, SIZE + GAP) //determine the maximum amount of elements for multi select (based on screen size for now)
                        if (Element.elementsSelected.size < max) {
                            //clear normal select
                            elementButtonSelectedA = null
                            elementButtonSelectedB = null
                            Element.elementSelectedA = null
                            Element.elementSelectedB = null
                            if (Game.mode == GameMode.PUZZLE) {
                                //in puzzle mode, can't select the same element multiple times, or else it will be duping
                                if (!elementsSelected.contains(this)) {
                                    Element.elementsSelected.add(element)
                                    elementsSelected.add(this)
                                }
                            } else {
                                Element.elementsSelected.add(element)
                                val button = ElementButton(element)
                                button.alpha = alpha
                                button.alphaChange = 0
                                elementsSelected.add(button)
                            }
                        }
                    } else {
                        //deselect  
                        if (elementButtonSelectedA === this) {
                            elementButtonSelectedA = null
                            Element.elementSelectedA = null
                        } else if (elementButtonSelectedB === this) {
                            elementButtonSelectedB = null
                            Element.elementSelectedB = null
                        } else if (elementButtonSelectedA == null) {
                            elementButtonSelectedA = this
                            Element.elementSelectedA = element
                        } else if (elementButtonSelectedB == null) {
                            elementButtonSelectedB = this
                            Element.elementSelectedB = element
                        }
                    }
                }
            } else if (main.room is ElementRoom) {
                elementButton = this
                main.switchRoom(ElementRoom)
            }
        } else {
            elementButton = this
            main.switchRoom(ElementRoom)
        }
    }

    private fun updateAlpha() {
        if (alphaChange > 0) { //fade in
            if (alpha < 255) {
                alpha += alphaChange
            }
            if (alpha > 255) {
                alpha = 255
            }
        } else if (alphaChange < 0) { //fade out
            if (alpha > 0) {
                alpha += alphaChange
            }
            if (alpha < 0) { //completely invisible
                alpha = 0
            }
        }
        main.tint(255, alpha.toFloat())
    }

    companion object {
        const val SIZE = 64
        const val HEIGHT = SIZE + 30
        private const val GAP = 30
        private const val ALPHA_CHANGE = 10
        private const val FAILED_TIME = 500
        private var maxElements = 0
        val elementButtonsA = ArrayList<ElementButton>()
        val elementButtonsB = ArrayList<ElementButton>()
        private var elementButtonSelectedA: ElementButton? = null
        private var elementButtonSelectedB: ElementButton? = null
        val elementsSelected = ArrayList<ElementButton>() //for selecting more than 2 elements
        var pageNumberA = 0
        var totalPagesA = 0
        var pageNumberB = 0
        var totalPagesB = 0
        private val elementButtonCreated = ArrayList<ElementButton>()
        var touching: ElementButton? = null
        private var time: Long = -1 //timer when combination is wrong

        fun reset() {
            elementButtonsA.clear()
            elementButtonsB.clear()
            elementButtonSelectedA = null
            elementButtonSelectedB = null
            elementsSelected.clear()
            pageNumberA = 0
            pageNumberB = 0
        }

        fun drawElements() {
            //determine how many elements to draw horizontally
            val elementCountX = Math.floorDiv(main.screenWidth - (Group.groupSelectedX + Group.SIZE + Group.GAP + 20 + Arrow.SIZE), SIZE + GAP)
            //determine how many elements to draw vertically
            val elementCountY = Math.floorDiv(Group.groupSelectedBY - Group.groupSelectedAY, HEIGHT + 16)
            maxElements = elementCountX * elementCountY
            touching = null
            var x: Int = Group.groupSelectedX + Group.SIZE + Group.GAP
            var y: Int = Group.groupSelectedAY

            if (Group.groupSelectedA != null) {
                totalPagesA = ceil((discovered[Group.groupSelectedA]!!.size.toFloat() / maxElements).toDouble()).toInt()
                if (pageNumberA >= totalPagesA) {
                    pageNumberA = totalPagesA - 1
                }
                for ((index, element) in getCurrentPageElements(elementButtonsA, pageNumberA).withIndex()) {
                    element.updateAlpha()
                    element.draw(x.toFloat(), y.toFloat())
                    x += SIZE + GAP
                    if ((index + 1) % elementCountX == 0) {
                        x = Group.groupSelectedX + Group.SIZE + Group.GAP
                        y += HEIGHT + 16
                    }
                }
            }
            x = Group.groupSelectedX + Group.SIZE + Group.GAP
            y = Group.groupSelectedBY
            if (Group.groupSelectedB != null) {
                totalPagesB = ceil((discovered[Group.groupSelectedB]!!.size.toFloat() / maxElements).toDouble()).toInt()
                if (pageNumberB >= totalPagesB) {
                    pageNumberB = totalPagesB - 1
                }
                for ((index, element) in getCurrentPageElements(elementButtonsB, pageNumberB).withIndex()) {
                    element.updateAlpha()
                    element.draw(x.toFloat(), y.toFloat())
                    x += SIZE + GAP
                    if ((index + 1) % elementCountX == 0) {
                        x = Group.groupSelectedX + Group.SIZE + Group.GAP
                        y += HEIGHT + 16
                    }
                }
            }

            //reset transparency
            main.tint(255, 255f)

            //draw tooltip, it's done here so it gets drawn on top of all the elements
            drawTooltip()

            //draw multi select
            x = Group.groupSelectedX
            y = main.screenHeight - Group.GAP - HEIGHT
            for (element in elementsSelected) {
                element.draw(x.toFloat(), y.toFloat())
                x += SIZE + GAP
            }
        }

        fun drawTooltip() {
            if (touching != null) {
                //draw tool tip
                main.textSize(20f)
                val padding = 2 //some horizontal padding
                val width: Float = main.textWidth(touching!!.element.displayName) + padding * 2
                val height: Float = main.textAscent() + main.textDescent()
                val offset = 13f //13 pixel offset so it doesn't cover the cursor
                var x: Float = main.mouseX + offset
                if (x + width >= main.screenWidth) {
                    x = main.screenWidth - width - 1 //subtracting 1 here so that the border is shown
                }
                main.stroke(255)
                main.fill(0)
                main.rect(x, main.mouseY.toFloat(), width, height)
                main.textAlign(PConstants.LEFT, PConstants.TOP)
                main.fill(if (main.settings.getBoolean("group colour")) touching!!.element.group.colour else 255, touching!!.alpha.toFloat())
                main.text(touching!!.element.displayName, x + padding, main.mouseY.toFloat())
            }
        }

        fun drawCreatedElements() {
            touching = null
            val length = (SIZE + GAP) * elementButtonCreated.size - GAP
            var x: Int = main.screenWidth / 2 - length / 2
            for (element in elementButtonCreated) {
                element.draw(x.toFloat(), main.screenHeight / 2f - SIZE / 2f)
                x += SIZE + GAP
            }
            drawTooltip()
        }

        fun drawHintElement(element: ElementButton) {
            touching = null
            element.draw(main.screenWidth / 2f - SIZE / 2f, main.screenHeight / 2f - SIZE / 2f)
            drawTooltip()
        }

        private fun failed(): Boolean {
            return time != -1L && main.millis() - time <= FAILED_TIME
        }

        fun checkForMultiCombos() {
            //reset disabled
            if (Group.groupSelectedA != null) {
                for (element in elementButtonsA) {
                    element.setDisabled(false)
                }
            }
            if (Group.groupSelectedB != null) {
                for (element in elementButtonsB) {
                    element.setDisabled(false)
                }
            }
            elementButtonCreated.clear()
            if (Element.checkForMultiCombos()) {
                //need to update because affected groups might be already selected
                updateA()
                updateB()
                elementsSelected.clear()
                for (element in Element.elementsCreated) {
                    elementButtonCreated.add(ElementButton(element))
                }
            } else {
                time = main.millis().toLong()
            }
        }

        fun checkForCombos() {
            if (elementButtonSelectedA != null && elementButtonSelectedB != null) {
                //check for combos
                elementButtonCreated.clear()
                if (Element.checkForCombos()) {
                    //need to update because affected groups might be already selected
                    updateA()
                    updateB()
                    elementButtonSelectedA = null
                    elementButtonSelectedB = null
                    for (element in Element.elementsCreated) {
                        elementButtonCreated.add(ElementButton(element))
                    }
                } else {
                    time = main.millis().toLong()
                }
            }
        }

        fun updateA() {
            elementButtonsA.clear()
            //group might be gone if we are in puzzle mode
            if (Group.groupSelectedA != null && Group.groupSelectedA!!.exists()) {
                for (element in discovered[Group.groupSelectedA]!!) {
                    val button = ElementButton(element)
                    elementButtonsA.add(button)
                }
            } else {
                Group.groupSelectedA = null
            }
        }

        fun resetA() {
            pageNumberA = 0
            elementButtonsA.clear()
            for ((index, element) in discovered[Group.groupSelectedA]!!.withIndex()) {
                val button = ElementButton(element)
                elementButtonsA.add(button)
                //we only fade in for elements that are on the first page
                if (index < maxElements) {
                    button.alpha = 0
                    button.alphaChange = ALPHA_CHANGE
                }
            }
            totalPagesA = ceil((discovered[Group.groupSelectedA]!!.size.toFloat() / maxElements).toDouble()).toInt()
        }

        fun updateB() {
            elementButtonsB.clear()
            //group might be gone if we are in puzzle mode
            if (Group.groupSelectedB != null && Group.groupSelectedB!!.exists()) {
                for (element in discovered[Group.groupSelectedB]!!) {
                    val button = ElementButton(element)
                    elementButtonsB.add(button)
                }
            } else {
                Group.groupSelectedB = null
            }
        }

        fun resetB() {
            pageNumberB = 0
            elementButtonsB.clear()
            for ((index, element) in discovered[Group.groupSelectedB]!!.withIndex()) {
                val button = ElementButton(element)
                elementButtonsB.add(button)
                if (index < maxElements) {
                    button.alpha = 0
                    button.alphaChange = ALPHA_CHANGE
                }
            }
            totalPagesB = ceil((discovered[Group.groupSelectedB]!!.size.toFloat() / maxElements).toDouble()).toInt()
        }

        fun hidePagesA() {
            //this is necessary because the first element clicked can be from group B
            if (Element.elementSelectedA != null && elementButtonSelectedA!!.y < Group.groupSelectedBY) {
                elementButtonSelectedA = null
                Element.elementSelectedA = null
            }
            if (Element.elementSelectedB != null && elementButtonSelectedB!!.y < Group.groupSelectedBY) {
                elementButtonSelectedB = null
                Element.elementSelectedB = null
            }
            for (element in elementButtonsA) {
                element.alphaChange = -ALPHA_CHANGE
            }
        }

        fun hidePagesB() {
            if (Element.elementSelectedA != null && elementButtonSelectedA!!.y >= Group.groupSelectedBY) {
                elementButtonSelectedA = null
                Element.elementSelectedA = null
            }
            if (Element.elementSelectedB != null && elementButtonSelectedB!!.y >= Group.groupSelectedBY) {
                elementButtonSelectedB = null
                Element.elementSelectedB = null
            }
            for (element in elementButtonsB) {
                element.alphaChange = -ALPHA_CHANGE
            }
        }

        fun getCurrentPageElements(buttons: List<ElementButton>, page: Int): List<ElementButton> {
            return buttons.subList(page * maxElements, min((page + 1) * maxElements, buttons.size))
        }
    }
}