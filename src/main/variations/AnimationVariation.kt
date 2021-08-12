package main.variations

import main.Element
import main.lateVal
import main.variations.appearances.Animation
import main.variations.appearances.Appearance
import processing.core.PImage
import processing.data.JSONObject

class AnimationVariation internal constructor(json: JSONObject, element: Element) : Variation(json, element) {

    private var animation: Animation by lateVal()

    override fun loadImages() {
        animation = Animation(this, json)
    }

    override fun getAppearance(): Appearance = animation

    override fun getPairs(): List<Pair<PImage, String>> = animation.getPairs()
}