package main.variations.appearances;

import main.variations.Variation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public class Animation extends Appearance {

    private ArrayList<PImage> images = new ArrayList<>();
    private int time; //time between frames
    private int lastTime;
    private int index;
    private ArrayList<String> paths = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();

    public Animation(Variation variation, JSONObject json) {
        super(variation);
        JSONArray textures = json.getJSONArray("textures");
        for (int i = 0;i < textures.size();i++) {
            String path = textures.getString(i);
            if (StringUtils.countMatches(path, ":") < 2) {
                path = this.getVariation().getElement().getName() + ":" + path;
            }
            this.paths.add(path);
            this.images.add(this.getVariation().getElement().getImage(path));
        }
        JSONArray names = json.getJSONArray("names");
        if (names != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (i < names.size()) {
                    this.names.add(names.getString(i));
                }
            }
        }
        this.time = json.hasKey("time") ? json.getInt("time") : 1000;
    }

    @Override
    public String getName() {
        return this.index < this.names.size() ? this.names.get(this.index) : null;
    }

    @Override
    public PImage getImage() {
        if (this.lastTime + this.time <= main.millis()) {
            this.lastTime = main.millis();
            this.index++;
            if (this.index >= this.images.size()) {
                this.index = 0;
            }
        }
        return this.images.get(this.index);
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getPairs() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (int i = 0;i < this.images.size();i++) {
            if (this.images.get(i) != null) {
                list.add(new ImmutablePair<>(this.images.get(i), this.paths.get(i)));
            }
        }
        return list;
    }
}