package main.variations;

import main.buttons.Element;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.time.LocalDateTime;

public class WeekVariation extends Variation {

    private ImageAndName[] images = new ImageAndName[7];

    WeekVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public ImageAndName getImageAndName() {
        return this.images[LocalDateTime.now().getDayOfWeek().getValue() - 1];
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        JSONArray names = this.json.getJSONArray("names");
        for (int i = 0; i < 7; i++) {
            String name = null;
            if (names != null && i < names.size()) {
                name = names.getString(i);
            }
            this.images[i] = new ImageAndName(this.element.getImage(array.getString(i)), name);
        }
    }
}
