package main.variations;

import main.buttons.Element;
import processing.core.PImage;
import processing.data.JSONObject;

public class InheritVariation extends Variation {

    InheritVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public PImage getImage() {
        Element element = Element.getElement(this.json.getString("texture"));
        if (element == null) {
            return this.element.getImage().copy();
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
    public ImageAndName getImageAndName() {
        return null;
    }
}
