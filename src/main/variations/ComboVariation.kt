package main.variations

import main.Element
import main.LoadElements
import main.combos.Combo
import main.variations.appearances.Appearance
import main.variations.appearances.Appearance.Companion.getAppearance
import main.variations.appearances.ElementTexture
import org.apache.commons.lang3.tuple.ImmutablePair
import processing.core.PImage
import processing.data.JSONObject
import java.util.*

class ComboVariation internal constructor(json: JSONObject, element: Element) : Variation(json, element) {
    private val pairs = ArrayList<ImmutablePair<Combo, Appearance>>()
    private lateinit var current: Appearance

    fun setCurrentImage(combo: Combo) {
        for (pair in pairs) {
            if (combo == pair.left) {
                current = pair.right
                return
            }
        }
    }

    override fun loadImages() {
        val array = json.getJSONArray("textures")
        for (i in 0 until array.size()) {
            val `object` = array.getJSONObject(i)
            val comboArray = `object`.getJSONArray("combos")
            for (j in 0 until comboArray.size()) {
                val combos = LoadElements.getCombo(comboArray.getJSONObject(j), element)
                for (combo in combos) {
                    pairs.add(ImmutablePair(combo, getAppearance(this, `object`)))
                }
            }
        }
        current = ElementTexture(element)
    }

    override fun getAppearance(): Appearance {
        return current
    }

    override fun getPairs(): List<Pair<PImage, String>> {
        return pairs.map { it.right.getPairs() }.flatten()
    }
}