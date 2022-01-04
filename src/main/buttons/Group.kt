package main.buttons

import main.Element
import main.Main
import main.buttons.ElementButton.Companion.hidePagesA
import main.buttons.ElementButton.Companion.hidePagesB
import main.buttons.ElementButton.Companion.resetA
import main.buttons.ElementButton.Companion.resetB
import main.rooms.Game
import main.rooms.Game.discovered
import main.rooms.GameMode
import main.rooms.Loading.removeAllGroups
import main.rooms.Loading.removeGroup
import main.rooms.Loading.updateProgress
import main.rooms.PacksRoom.loadedPacks
import processing.core.PImage
import processing.data.JSONArray
import processing.data.JSONObject
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class Group(
    val id: String,
    val colour: Int,
    image: PImage,
    val pack: Pack,
    private var alpha: Int = 255,
    private var alphaChange: Int = 0
) : Button(SIZE, SIZE, image), Comparable<Group> {

    private var done = false
    val localId = getLocalId(id)

    private fun isSelected() = x >= groupSelectedX

    //just added an extra !moving condition
    override fun inBounds(): Boolean {
        return Main.mouseX >= x && Main.mouseX <= x + SIZE && Main.mouseY >= y && Main.mouseY <= y + SIZE && !moving
    }

    override fun compareTo(other: Group): Int {
        return if (localId != other.localId) {
            localId.compareTo(other.localId)
        } else {
            val thisPack = loadedPacks.indexOf(pack)
            val oPack = loadedPacks.indexOf(other.pack)
            thisPack.compareTo(oPack)
        }
    }

    override fun drawButton() {
        updateAlpha()
        Main.image(image, x, y)
    }

    private fun updateAlpha() {
        if (alphaChange > 0) { //fade in
            if (alpha < 255) {
                alpha += alphaChange
            }
            if (alpha >= 255) {
                alpha = 255
            }
        } else if (alphaChange < 0) { //fade out
            if (alpha > 0) {
                alpha += alphaChange
            }
            if (alpha <= 0) { //completely invisible
                alpha = 0
                done = true
            }
        }
        Main.tint(255, alpha.toFloat())
    }

    fun exists() = discovered.keys.any { it.id == id }

    override fun clicked() {
        if (!moving) {
            if (!isSelected()) {
                //in puzzle mode, you cannot open the same group twice
                if (Game.mode == GameMode.PUZZLE && (this.id == groupSelectedA?.id || this.id == groupSelectedB?.id)) {
                    return
                }
                //the two blocks of code should be identical
                if (groupSelectedA == null) {
                    groupSelectedA = Group(id, colour, image, pack, 0, ALPHA_CHANGE)
                    groupSelectedA!!.x = x
                    groupSelectedA!!.y = y
                    moving = true
                    deltaX = groupSelectedX - groupSelectedA!!.x
                    deltaY = groupSelectedAY - groupSelectedA!!.y
                    resetA()
                } else if (groupSelectedB == null) {
                    groupSelectedB = Group(id, colour, image, pack, 0, ALPHA_CHANGE)
                    groupSelectedB!!.x = x
                    groupSelectedB!!.y = y
                    moving = true
                    deltaX = groupSelectedX - groupSelectedB!!.x
                    deltaY = groupSelectedBY - groupSelectedB!!.y
                    resetB()
                }
            } else {
                if (this === groupSelectedA) {
                    groupSelectedA?.alphaChange = -ALPHA_CHANGE
                    hidePagesA()
                } else if (this === groupSelectedB) {
                    groupSelectedB?.alphaChange = -ALPHA_CHANGE
                    hidePagesB()
                }
            }
        }
    }

    companion object {
        const val SIZE = 128
        const val GAP = 30
        const val GROUP_X: Int = GAP
        const val GROUP_Y: Int = GAP + 20
        private const val GROUP_MOVING_RATE = 15f
        private const val ALPHA_CHANGE = 20
        var groupCountX = 0
        var groupCountY = 0
        private var maxGroups = 0
        var totalPages = 0
        var pageNumber = 0
        var groupSelectedX = 0
        var groupSelectedAY = 0
        var groupSelectedBY = 0
        var groupSelectedA: Group? = null
        var groupSelectedB: Group? = null
        private var deltaX = 0f
        private var deltaY = 0f
        private var moving = false

        fun reset() {
            pageNumber = 0
            groupSelectedA = null
            groupSelectedB = null
        }

        fun loadGroups(array: JSONArray, pack: Pack) {
            for (i in 0 until array.size()) {
                val json = array.getJSONObject(i)
                if (json.hasKey("remove")) {
                    if (json.getString("remove") == "all") {
                        removeAllGroups()
                        Main.groups.clear()
                    } else {
                        val group = getGroup(pack.getNamespacedName(json.getString("remove")))
                        if (group == null) {
                            System.err.println(pack.getNamespacedName(json.getString("remove")) + " group not found!")
                        } else {
                            removeGroup(group)
                            Main.groups.remove(group)
                        }
                    }
                } else {
                    Main.groups[createGroup(json, pack)] = HashSet<Element>()
                }
                updateProgress()
//                Thread.sleep(500)
            }
        }

        private fun getLocalId(id: String) = id.split(":")[1]

        private fun createGroup(json: JSONObject, pack: Pack): Group {
            val id = pack.getNamespacedName(json.getString("name"))
            val colourArray = json.getJSONArray("colour")
            lateinit var image: PImage

            //check if a pack has the image, from top to bottom
            for (loadedPack in loadedPacks) {
                if (loadedPack.name == "Alchemy" && pack.name == "Alchemy") {
                    //if the element is of the default pack and we are in the default pack right now, load default location
                    image = Main.loadImage("resources/groups/alchemy/${getLocalId(id)}.png")
                    break
                } else {
                    val packPath = "${loadedPack.path}/groups/${pack.namespace}/${getLocalId(id)}.png"
                    if (File(packPath).exists()) {
                        image = Main.loadImage(packPath)
                        break
                    }
                }
            }
            return Group(
                id,
                Main.color(colourArray.getInt(0), colourArray.getInt(1), colourArray.getInt(2)),
                image,
                pack
            )
        }

        @JvmStatic
        fun getGroup(id: String): Group? = Main.groups.keys.find { it.id == id }

        fun drawGroups() {
            var x = GROUP_X
            var y = GROUP_Y
            val maxX = (Main.screenWidth / 2f * 0.6).roundToInt() //maximum X value to draw the group grid
            groupSelectedX = maxX + 100
            groupSelectedAY = y
            groupSelectedBY = (Main.screenHeight * 0.44).roundToInt()
            val maxY: Int = Main.screenHeight - 60 //maximum Y value to draw the group grid
            //determine how many groups to draw horizontally
            groupCountX = Math.floorDiv(maxX - x, SIZE + GAP)
            //determine how many groups to draw vertically
            groupCountY = Math.floorDiv(maxY - y, SIZE + GAP)
            //number of groups on a page
            maxGroups = groupCountX * groupCountY
            totalPages = ceil(discovered.keys.size.toFloat() / maxGroups).roundToInt()
            if (pageNumber >= totalPages) {
                pageNumber = totalPages - 1
            }
            for ((index, group) in getCurrentPageGroups().withIndex()) {
                group.draw(x.toFloat(), y.toFloat())
                x += SIZE + GAP
                if ((index + 1) % groupCountX == 0) {
                    x = GROUP_X
                    y += SIZE + GAP
                }
            }
            groupSelectedA?.let {
                if (it.x < groupSelectedX) {
                    it.x += deltaX / GROUP_MOVING_RATE
                    it.y += deltaY / GROUP_MOVING_RATE
                    //having the if statement here ensures that it only gets run once
                    if (it.x >= groupSelectedX) {
                        //need to set it so it doesn't go past
                        it.x = groupSelectedX.toFloat()
                        it.y = groupSelectedAY.toFloat()
                        moving = false
                    }
                }
                //needs to be constantly set due to screen resizing
                if (!moving) {
                    it.x = groupSelectedX.toFloat()
                    it.y = groupSelectedAY.toFloat()
                }
                it.draw()
            }
            if (groupSelectedA?.done == true) {
                groupSelectedA = null
            }
            groupSelectedB?.let {
                if (it.x < groupSelectedX) {
                    it.x += deltaX / GROUP_MOVING_RATE
                    it.y += deltaY / GROUP_MOVING_RATE
                    //having the if statement here ensures that it only gets run once
                    if (it.x >= groupSelectedX) {
                        it.x = groupSelectedX.toFloat()
                        it.y = groupSelectedBY.toFloat()
                        moving = false
                    }
                }
                //needs to be constantly set due to screen resizing
                if (!moving) {
                    it.x = groupSelectedX.toFloat()
                    it.y = groupSelectedBY.toFloat()
                }
                it.draw()
            }
            if (groupSelectedB?.done == true) {
                groupSelectedB = null
            }
            Main.tint(255, 255f)
        }

        fun setHintGroups(a: Group, b: Group) {
            groupSelectedA = Group(a.id, a.colour, a.image, a.pack)
            groupSelectedB = Group(b.id, b.colour, b.image, b.pack)
            resetA()
            resetB()
        }

        fun getCurrentPageGroups(): List<Group> {
            return discovered.keys.toList()
                .subList(pageNumber * maxGroups, min((pageNumber + 1) * maxGroups, discovered.keys.size))
        }
    }
}