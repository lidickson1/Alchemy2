package main.variations

import main.Element
import main.lateVal
import main.variations.appearances.Appearance
import processing.core.PImage
import processing.data.JSONObject
import java.time.LocalDateTime

class WeekVariation internal constructor(json: JSONObject, element: Element) : Variation(json, element) {
    private var images: List<Appearance> by lateVal()
    override fun getPairs(): List<Pair<PImage, String>> {
        return images.map { it.getPairs() }.flatten()
    }

    override fun loadImages() {
        val appearances = loadAppearances()
        images = appearances.subList(0, appearances.size.coerceAtMost(7))
    }

    override fun getAppearance(): Appearance {
        return images[LocalDateTime.now().dayOfWeek.value - 1]
    }
}