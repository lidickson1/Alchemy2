package main.variations;

import main.buttons.Element;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.time.LocalDateTime;

public class WeekVariation extends Variation {

    private PImage[] images = new PImage[7];

    WeekVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public PImage getImage() {
        return this.images[LocalDateTime.now().getDayOfWeek().getValue() - 1];
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        for (int i = 0; i < 7; i++) {
            this.images[i] = this.element.getImage(array.getString(i));
        }
    }
}
