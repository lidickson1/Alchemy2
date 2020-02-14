package main.variations;

import main.buttons.Element;
import main.variations.appearances.Animation;
import main.variations.appearances.Appearance;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.util.ArrayList;

public class AnimationVariation extends Variation {

    private Animation animation;

    AnimationVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public void loadImages() {
        this.animation = new Animation(this, this.json);
    }

    @Override
    public Appearance getAppearance() {
        return this.animation;
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getPairs() {
        return this.animation.getPairs();
    }
}
