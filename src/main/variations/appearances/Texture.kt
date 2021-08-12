package main.variations.appearances

import main.Element
import main.variations.Variation
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.ImmutablePair
import processing.core.PImage
import processing.data.JSONObject

class Texture(val variation: Variation, json: JSONObject) : Appearance() {

    private val image: PImage
    private val path: String
    private val name: String

    init {
        var path = json.getString("texture")
        if (StringUtils.countMatches(path, ":") < 2) {
            path = "${this.variation.element.id}:$path"
        }
        this.path = path
        image = this.variation.element.getImageWithoutFallback(path)!!
        name = json.getString("name", "null")
    }

    override fun getName(): String {
        return name
    }

    override fun getImage(): PImage {
        return image
    }

    override fun getPairs(): List<Pair<PImage, String>> {
        return listOf(image to path)
    }
}