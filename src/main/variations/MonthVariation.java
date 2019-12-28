package main.variations;

import main.buttons.Element;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.time.LocalDateTime;

public class MonthVariation extends Variation {

    private PImage[] images = new PImage[12];

    MonthVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public PImage getImage() {
        return this.images[LocalDateTime.now().getMonthValue() - 1];
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        for (int i = 0; i < 12; i++) {
            this.images[i] = this.element.getImage(array.getString(i));
        }
    }
}
