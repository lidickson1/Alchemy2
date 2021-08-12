package main.variations

import main.Element
import main.lateVal
import main.variations.appearances.Appearance
import main.variations.appearances.Appearance.Companion.getAppearance
import main.variations.appearances.ElementTexture
import processing.core.PImage
import processing.data.JSONObject
import kotlin.random.Random

class RandomVariation internal constructor(json: JSONObject, element: Element) : Variation(json, element) {

    private var appearances: List<Appearance> by lateVal()
    private var random: WeightedRandom<Appearance> by lateVal()

    override fun loadImages() {
        val array = json.getJSONArray("textures")
        val map = mutableMapOf<Appearance, Double>()
        var remainingWeight = 1.0
        for (i in 0 until array.size()) {
            val `object` = array.getJSONObject(i)
            map[getAppearance(this, `object`)] =  `object`.getDouble("weight")
            remainingWeight -= `object`.getDouble("weight")
        }
        map[ElementTexture(element)] =  remainingWeight //chance of getting the original image
        appearances = map.keys.toList()
        random = WeightedRandom(map)
    }

    override fun getAppearance(): Appearance {
        return random.get()
    }

    override fun getPairs(): List<Pair<PImage, String>> {
        return appearances.map { it.getPairs() }.flatten()
    }

    //this must be used so it doesn't get a random name every time, but the name corresponding the chosen image
//    fun getName(image: PImage): String? {
//        for (pair in random.pmf) {
//            if (pair.first.getImage() === image) {
//                return pair.first.getName()
//            }
//        }
//        return null
//    }
}

class WeightedRandom<T>(map: Map<T, Number>) {

    private val list = mutableListOf<Triple<Double, Double, T>>()

    init {
        var i = 0.0
        for ((key, value) in map) {
            list.add(Triple(i, i + value.toDouble(), key))
            i += value.toDouble()
        }
    }

    fun get(): T {
        val random = Random.nextDouble(list.last().second)
        for (triple in list) {
            if (random >= triple.first && random < triple.second) {
                return triple.third
            }
        }
        return list.first().third
    }

}