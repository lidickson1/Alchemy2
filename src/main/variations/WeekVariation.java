package main.variations;

import main.Element;
import main.variations.appearances.Appearance;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class WeekVariation extends Variation {

    private Appearance[] images = new Appearance[7];

    WeekVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getPairs() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (Appearance appearance : this.images) {
            list.addAll(appearance.getPairs());
        }
        return list;
    }

    @Override
    public void loadImages() {
        ArrayList<Appearance> imageAndNames = this.loadAppearances();
        for (int i = 0; i < 7; i++) {
            if (i < imageAndNames.size()) {
                this.images[i] = imageAndNames.get(i);
            } else {
                break;
            }
        }
    }

    @Override
    public Appearance getAppearance() {
        return this.images[LocalDateTime.now().getDayOfWeek().getValue() - 1];
    }
}
