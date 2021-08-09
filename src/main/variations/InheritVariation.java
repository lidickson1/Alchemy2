package main.variations;

import main.Element;
import main.buttons.Pack;
import main.variations.appearances.Appearance;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.util.ArrayList;

public class InheritVariation extends Variation {

    private Pack pack;

    InheritVariation(JSONObject json, Element element, Pack pack) {
        super(json, element);
        this.pack = pack;
    }

    private Element getInheritedElement() {
        return Element.Companion.getElement(this.pack.getNamespacedName(this.getJson().getString("texture")));
    }

    @Override
    public PImage getImage() {
        return this.getInheritedElement().getImage();
    }

    @Override
    public void loadImages() {
        //can't load images here because this variation uses pre-existing images, which have not been loaded yet
    }

    @Override
    public String getName() {
        if (this.getInheritedElement().getVariation() != null) {
            return this.getInheritedElement().getVariation().getName();
        } else {
            return null;
        }
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
