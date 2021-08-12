package main.variations.appearances

import main.variations.Variation
import org.apache.commons.lang3.StringUtils
import processing.core.PImage
import processing.data.JSONObject
import java.util.*

class Animation(val variation: Variation, json: JSONObject) : Appearance() {
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

    override fun getPairs(): List<Pair<PImage, String>> {
        //TODO what is the path for strip animation textures?
        return images.zip(paths)
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
        time = json.getInt("time", 1000)
    }
}