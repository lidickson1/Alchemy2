package main.variations.appearances

import main.Element
import org.apache.commons.lang3.tuple.ImmutablePair
import processing.core.PImage

class ElementTexture(private val element: Element) : Appearance() {
    override fun getName(): String {
        return element.getDisplayNameWithoutVariation()
    }

    override fun getImage(): PImage {
        return element.image
    }

    override fun getPairs(): List<Pair<PImage, String>> {
        TODO("Not yet implemented")
    }
}