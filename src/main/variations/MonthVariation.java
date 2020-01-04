package main.variations;

import main.buttons.Element;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class MonthVariation extends Variation {

    private ImageAndName[] images = new ImageAndName[12];

    MonthVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public void loadImages() {
        ArrayList<ImageAndName> imageAndNames = this.loadImageAndNames();
        for (int i = 0;i < 12;i++) {
            if (i < imageAndNames.size()) {
                this.images[i] = imageAndNames.get(i);
            } else {
                break;
            }
        }
    }

    @Override
    public ImageAndName getImageAndName() {
        return this.images[LocalDateTime.now().getMonthValue() - 1];
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getImages() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (ImageAndName imageAndName : this.images) {
            if (imageAndName.hasImage()) {
                list.add(imageAndName.toPair());
            }
        }
        return list;
    }
}
