package main.variations.appearances

import main.variations.Variation
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.ImmutablePair
import processing.core.PImage
import processing.data.JSONObject
import java.util.*

class Animation(variation: Variation, json: JSONObject) : Appearance(variation) {
    private val images = ArrayList<PImage>()
    private val time //time between frames
            : Int
    private var lastTime = 0
    private var index = 0
    private val paths = ArrayList<String>()
    private val names = ArrayList<String>()
    override fun getName(): String = if (index < names.size) names[index] else "null"
    override fun getImage(): PImage {
        if (lastTime + time <= main.millis()) {
            lastTime = main.millis()
            index++
            if (index >= images.size) {
                index = 0
            }
        }
        return images[index]
    }

    override fun getPairs(): List<ImmutablePair<PImage, String>> {
        //TODO what is the path for strip animation textures?
        val list = ArrayList<ImmutablePair<PImage, String>>()
        for (i in images.indices) {
            list.add(ImmutablePair(images[i], paths[i]))
        }
        return list
    }

    init {
        if (json.hasKey("texture")) {
            val image = variation.element.loadImage(json.getString("texture"))
            for (i in 0 until image.height / image.width) {
                images.add(image.get(0, i * image.width, image.width, image.width))
            }
        } else {
            val textures = json.getJSONArray("textures")
            for (i in 0 until textures.size()) {
                var path = textures.getString(i)
                if (StringUtils.countMatches(path, ":") < 2) {
                    path = "${this.variation.element.id}:$path"
                }
                paths.add(path)
                images.add(this.variation.element.loadImage(path))
            }
            json.getJSONArray("names")?.let {
                for (i in 0 until textures.size()) {
                    if (i < it.size()) {
                        this.names.add(it.getString(i))
                    }
                }
            }
        }
        time = if (json.hasKey("time")) json.getInt("time") else 1000
    }
}