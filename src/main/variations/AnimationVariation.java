package main.variations;

import main.buttons.Element;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public class AnimationVariation extends Variation {

    private ArrayList<PImage> images = new ArrayList<>();
    private int time; //time between frames
    private int lastTime;
    private int index;

    AnimationVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public PImage getImage() {
        if (this.lastTime + this.time <= main.millis()) {
            this.lastTime = main.millis();
            this.index++;
            if (this.index >= this.images.size()) {
                this.index = 0;
            }
        }
        return this.images.get(this.index);
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        for (int i = 0; i < array.size(); i++) {
            this.images.add(this.element.getImage(array.getString(i)));
        }
        this.time = this.json.hasKey("time") ? this.json.getInt("time") : 1000;
    }
}
