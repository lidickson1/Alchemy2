package main.variations;

import main.buttons.Element;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.util.ArrayList;

public class AnimationVariation extends Variation {

    private ArrayList<ImageAndName> imageAndNames = new ArrayList<>();
    private int time; //time between frames
    private int lastTime;
    private int index;

    AnimationVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public ImageAndName getImageAndName() {
        if (this.lastTime + this.time <= main.millis()) {
            this.lastTime = main.millis();
            this.index++;
            if (this.index >= this.imageAndNames.size()) {
                this.index = 0;
            }
        }
        return this.imageAndNames.get(this.index);
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getImages() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (ImageAndName imageAndName : this.imageAndNames) {
            if (imageAndName.hasImage()) {
                list.add(imageAndName.toPair());
            }
        }
        return list;
    }

    @Override
    public void loadImages() {
        this.imageAndNames = this.loadImageAndNames();
        this.time = this.json.hasKey("time") ? this.json.getInt("time") : 1000;
    }

}
