package main.variations;

import main.buttons.Element;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public class AnimationVariation extends Variation {

    private ArrayList<ImageAndName> pairs = new ArrayList<>();
    private int time; //time between frames
    private int lastTime;
    private int index;

    AnimationVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public ImageAndName getImageAndName() {
        if (this.lastTime + this.time <= main.millis()) {
            this.lastTime = main.millis();
            this.index++;
            if (this.index >= this.pairs.size()) {
                this.index = 0;
            }
        }
        return this.pairs.get(this.index);
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        JSONArray names = this.json.getJSONArray("names");
        for (int i = 0; i < array.size(); i++) {
            String name = null;
            if (names != null && i < names.size()) {
                name = names.getString(i);
            }
            this.pairs.add(new ImageAndName(this.element.getImage(array.getString(i)), name));
        }
        this.time = this.json.hasKey("time") ? this.json.getInt("time") : 1000;
    }

}
