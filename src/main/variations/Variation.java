package main.variations;

import main.Entity;
import main.buttons.Button;
import main.buttons.Element;
import org.apache.commons.lang3.StringUtils;
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
        return this.getImageAndName().getImage();
    }

    public abstract void loadImages();

    public String getName() {
        return this.getImageAndName().getName();
    }

    ArrayList<ImageAndName> loadImageAndNames() {
        ArrayList<ImageAndName> list = new ArrayList<>();
        JSONArray textures = this.json.getJSONArray("textures");
        JSONArray names = this.json.getJSONArray("names");
        if (textures == null && names == null) {
            System.err.println("Error with variation for " + this.element.getName());
            list.add(new ImageAndName(null, null));
            return list;
        }
        int texturesSize = textures == null ? 0 : textures.size();
        int namesSize = names == null ? 0 : names.size();
        for (int i = 0; i < Math.max(texturesSize, namesSize); i++) {
            String name = null;
            if (names != null && i < namesSize) {
                name = names.getString(i);
            }
            String image = null;
            if (textures != null && i < texturesSize) {
                image = textures.getString(i);
            }
            list.add(new ImageAndName(image, name));
        }
        return list;
    }

    public abstract ImageAndName getImageAndName();

    public abstract ArrayList<ImmutablePair<PImage, String>> getImages();

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
            case "inherit":
                return new InheritVariation(json, element);
            default:
                return null;
        }
    }

    class ImageAndName {

        private PImage image;
        private String path;
        private String name;

        public ImageAndName(String path, String name) {
            this.path = path;
            if (StringUtils.countMatches(this.path, ":") < 2) {
                this.path = Variation.this.element.getName() + ":" + this.path;
            }
            this.image = Variation.this.element.getImage(this.path);
            if (this.image != null) {
                this.image.resize(Element.SIZE, Element.SIZE);
            }
            this.name = name;
        }

        PImage getImage() {
            return this.image;
        }

        String getName() {
            return this.name;
        }

        //for atlas
        boolean hasImage() {
            return this.image != null && this.image != Button.error;
        }

        //for atlas
        ImmutablePair<PImage, String> toPair() {
            return new ImmutablePair<>(this.image, Variation.this.element.getName() + ":" + this.path);
        }
    }

}
