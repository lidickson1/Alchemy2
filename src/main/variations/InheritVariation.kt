package main.variations

import main.Element
import main.buttons.Pack
import main.variations.appearances.Appearance
import main.variations.appearances.ElementTexture
import processing.core.PImage
import processing.data.JSONObject

class InheritVariation internal constructor(json: JSONObject, element: Element, private val pack: Pack) : Variation(
    json, element
) {
    private val inheritedElement: Element by lazy { Element.getElement(pack.getNamespacedName(json.getString("texture")))!! }
    private val inheritedAppearance by lazy {
        inheritedElement.variation?.getAppearance() ?: ElementTexture(inheritedElement)
    }

    override fun getImage(): PImage {
        return inheritedElement.image
    }

    override fun loadImages() {
        //can't load images here because this variation uses pre-existing images, which have not been loaded yet
    }

    override fun getName(): String {
        return inheritedElement.getDisplayName()
    }

    override fun getAppearance(): Appearance {
        return inheritedAppearance
    }

    override fun getPairs(): List<Pair<PImage, String>> {
        //since this uses pre-existing textures, no need to add it to atlas
        return emptyList()
    }
}