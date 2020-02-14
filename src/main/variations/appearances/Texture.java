package main.variations.appearances;

import main.variations.Variation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.util.ArrayList;

public class Texture extends Appearance {

    private PImage image;
    private String path;
    private String name;

    public Texture(Variation variation) {
        super(variation);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Texture(Variation variation, JSONObject json) {
        super(variation);
        this.path = json.getString("texture");
        if (StringUtils.countMatches(this.path, ":") < 2) {
            this.path = this.getVariation().getElement().getName() + ":" + this.path;
        }
        this.image = this.getVariation().getElement().getImageWithoutFallback(this.path);
        this.name = json.getString("name");
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