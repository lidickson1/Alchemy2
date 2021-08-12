package main.variations

import main.Element
import main.lateVal
import main.variations.appearances.Appearance
import processing.core.PImage
import processing.data.JSONObject
import java.time.LocalDateTime

class MonthVariation internal constructor(json: JSONObject, element: Element) : Variation(json, element) {
    private var images: List<Appearance> by lateVal()
    override fun loadImages() {
        val appearances = loadAppearances()
        images = appearances.subList(0, appearances.size.coerceAtMost(12))
    }

    override fun getAppearance(): Appearance {
        return images[LocalDateTime.now().monthValue - 1]
    }

    override fun getPairs(): List<Pair<PImage, String>> {
        return images.map { it.getPairs() }.flatten()
    }
}