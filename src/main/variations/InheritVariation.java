package main.variations;

import main.buttons.Element;
import main.variations.appearances.Appearance;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.util.ArrayList;

//TODO: what if I want to inherit an animated texture?
public class InheritVariation extends Variation {

    InheritVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public PImage getImage() {
        Element element = Element.getElement(this.json.getString("texture"));
        if (element == null) {
            return null;
        } else {
            return element.getImage();
        }
    }

    @Override
    public void loadImages() {
        //can't load images here because this variation uses pre-existing images, which have not been loaded yet
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Appearance getAppearance() {
        return null;
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getPairs() {
        //since this uses pre-existing textures, no need to add it to atlas
        return new ArrayList<>();
    }
}
