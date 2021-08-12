package main.variations

import main.Element
import main.Entity
import main.buttons.Pack
import main.variations.appearances.Appearance
import main.variations.appearances.Appearance.Companion.getAppearance
import processing.core.PImage
import processing.data.JSONObject
import java.util.*

abstract class Variation  //can't load images in the constructor because it won't be in the image threading process
internal constructor(val json: JSONObject, val element: Element) : Entity() {
    open fun getImage(): PImage = getAppearance().getImage()

    abstract fun loadImages()

    open fun getName(): String = getAppearance().getName()

    fun loadAppearances(): ArrayList<Appearance> {
        val list = ArrayList<Appearance>()
        val textures = json.getJSONArray("textures")
        for (i in 0 until textures.size()) {
            val `object` = textures[i]
            if (`object` is String) {
                val json = JSONObject()
                json.put("texture", `object`)
                list.add(getAppearance(this, json))
            } else {
                list.add(getAppearance(this, (`object` as JSONObject)))
            }
        }
        return list
    }

    abstract fun getAppearance(): Appearance

    abstract fun getPairs(): List<Pair<PImage, String>>

    companion object {
        @JvmStatic
        fun getVariation(json: JSONObject, element: Element, pack: Pack): Variation? {
            return when (json.getString("type")) {
                "random" -> RandomVariation(json, element)
                "combo" -> ComboVariation(json, element)
                "month" -> MonthVariation(json, element)
                "week" -> WeekVariation(json, element)
                "inherit" -> InheritVariation(json, element, pack)
                "animation" -> AnimationVariation(json, element)
                else -> null
            }
        }
    }
}