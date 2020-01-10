package main.variations.appearances;

import main.variations.Variation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;

import java.util.ArrayList;

public class Texture extends Appearance {

    private PImage image;
    private String path;

    public Texture(Variation variation) {
        super(variation, null);
        this.image = null;
    }

    public Texture(Variation variation, String path, String name) {
        super(variation, name);
        this.path = path;
        if (StringUtils.countMatches(this.path, ":") < 2) {
            this.path = this.getVariation().getElement().getName() + ":" + this.path;
        }
        this.image = this.getVariation().getElement().getImageWithoutFallback(this.path);
    }

    @Override
    public PImage getImage() {
        return this.image;
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getPairs() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        if (this.image != null) {
            list.add(new ImmutablePair<>(this.image, this.path));
        }
        return list;
    }
}