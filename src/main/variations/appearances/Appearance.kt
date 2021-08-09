package main.variations.appearances

import main.Entity
import main.variations.Variation
import org.apache.commons.lang3.tuple.ImmutablePair
import processing.core.PImage
import processing.data.JSONObject
import java.util.*

abstract class Appearance(val variation: Variation) : Entity() {

    abstract fun getName(): String?
    abstract fun getImage(): PImage?
    //for atlas
    abstract fun getPairs(): List<ImmutablePair<PImage, String>>

    companion object {
        //TODO: fix this up a bit
        @JvmStatic
        fun getAppearance(variation: Variation, json: JSONObject): Appearance? {
            if (json.hasKey("texture")) {
                return Texture(variation, json)
            } else if (json.hasKey("textures")) {
                return Animation(variation, json)
            }
            return null
        }
    }
}