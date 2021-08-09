package main.variations.appearances

import main.Element
import main.variations.Variation
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.ImmutablePair
import processing.core.PImage
import processing.data.JSONObject

class Texture : Appearance {
    private var image: PImage
    private var path: String
    private var name: String?

    constructor(variation: Variation) : super(variation) {
        image = variation.getImage()
        path = "???" //TODO
        name = variation.getName()
    }

    constructor(variation: Variation, element: Element) : super(variation) {
        image = element.image
        path = "???" //TODO
        name = element.displayName
    }

    override fun getName(): String? {
        return name
    }

    constructor(variation: Variation, json: JSONObject) : super(variation) {
        path = json.getString("texture")
        if (StringUtils.countMatches(path, ":") < 2) {
            path = "${this.variation.element.id}:$path"
        }
        image = this.variation.element.getImageWithoutFallback(path)!!
        name = json.getString("name")
    }

    override fun getImage(): PImage {
        return image
    }

    override fun getPairs(): List<ImmutablePair<PImage, String>> {
        return listOf(ImmutablePair(image, path))
    }
}