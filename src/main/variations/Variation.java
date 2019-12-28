package main.variations;

import main.Entity;
import main.buttons.Element;
import processing.core.PImage;
import processing.data.JSONObject;

public abstract class Variation extends Entity {

    JSONObject json;
    Element element;

    Variation(JSONObject json, Element element) {
        this.json = json;
        this.element = element;
        //can't load images in the constructor because it won't be in the image threading process
    }

    public abstract PImage getImage();

    public abstract void loadImages();

    public static Variation getVariation(JSONObject json, Element element) {
        switch (json.getString("type")) {
            case "random":
                return new RandomVariation(json, element);
            case "combo":
                return new ComboVariation(json, element);
            case "month":
                return new MonthVariation(json, element);
            case "week":
                return new WeekVariation(json, element);
            case "animation":
                return new AnimationVariation(json, element);
            default:
                return null;
        }
    }

}
