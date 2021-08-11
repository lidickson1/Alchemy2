package main.variations

import main.Element
import main.variations.appearances.Animation
import main.variations.appearances.Appearance
import org.apache.commons.lang3.tuple.ImmutablePair
import processing.core.PImage
import processing.data.JSONObject

class AnimationVariation internal constructor(json: JSONObject, element: Element) : Variation(json, element) {

    private lateinit var animation: Animation

    override fun loadImages() {
        animation = Animation(this, json)
    }

    override fun getAppearance(): Appearance = animation

    override val pairs: List<ImmutablePair<PImage, String>>
        get() = animation.getPairs()
}