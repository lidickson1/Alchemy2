package main.variations.appearances

import main.Entity
import main.variations.Variation
import processing.core.PImage
import processing.data.JSONObject

abstract class Appearance : Entity() {

    abstract fun getName(): String
    abstract fun getImage(): PImage

    //for atlas
    abstract fun getPairs(): List<Pair<PImage, String>>

    companion object {
        @JvmStatic
        fun getAppearance(variation: Variation, json: JSONObject): Appearance {
//            if (json.hasKey("texture")) {
//                return Texture(variation, json)
//            } else
            if (json.hasKey("textures")) {
                return Animation(variation, json)
            }
            return Texture(variation, json)
//            return null
        }
    }
}