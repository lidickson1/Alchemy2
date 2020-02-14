package main.variations;

import main.Entity;
import main.buttons.Element;
import main.buttons.Pack;
import main.variations.appearances.Appearance;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public abstract class Variation extends Entity {

    JSONObject json;
    Element element;

    Variation(JSONObject json, Element element) {
        this.json = json;
        this.element = element;
        //can't load images in the constructor because it won't be in the image threading process
    }

    public PImage getImage() {
        PImage image = this.getAppearance().getImage();
        return image == null ? this.element.getImage() : image;
    }

    public abstract void loadImages();

    public Element getElement() {
        return this.element;
    }

    public String getName() {
        return this.getAppearance().getName();
    }

    ArrayList<Appearance> loadAppearances() {
        ArrayList<Appearance> list = new ArrayList<>();
        JSONArray textures = this.json.getJSONArray("textures");
        for (int i = 0; i < textures.size(); i++) {
            Object object = textures.get(i);
            if (object instanceof String) {
                JSONObject json = new JSONObject();
                json.put("texture", object);
                list.add(Appearance.getAppearance(this, json));
            } else {
                list.add(Appearance.getAppearance(this, (JSONObject) object));
            }
        }
        return list;
    }

    public abstract Appearance getAppearance();

    public abstract ArrayList<ImmutablePair<PImage, String>> getPairs();

    public static Variation getVariation(JSONObject json, Element element, Pack pack) {
        switch (json.getString("type")) {
            case "random":
                return new RandomVariation(json, element);
            case "combo":
                return new ComboVariation(json, element);
            case "month":
                return new MonthVariation(json, element);
            case "week":
                return new WeekVariation(json, element);
            case "inherit":
                return new InheritVariation(json, element, pack);
            case "animation":
                return new AnimationVariation(json, element);
            default:
                return null;
        }
    }

}
