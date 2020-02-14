package main.variations.appearances;

import main.Entity;
import main.variations.Variation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.util.ArrayList;

public abstract class Appearance extends Entity {

    private Variation variation;

    public Appearance(Variation variation) {
        this.variation = variation;
    }

    public abstract String getName();

    Variation getVariation() {
        return this.variation;
    }

    public abstract PImage getImage();

    //for atlas
    public abstract ArrayList<ImmutablePair<PImage, String>> getPairs();

    //TODO: fix this up a bit
    public static Appearance getAppearance(Variation variation, JSONObject json) {
        if (json.hasKey("texture")) {
            return new Texture(variation, json);
        } else if (json.hasKey("textures")) {
            return new Animation(variation, json);
        }

        return null;
    }

}