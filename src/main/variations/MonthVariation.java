package main.variations;

import main.buttons.ElementButton;
import main.variations.appearances.Appearance;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class MonthVariation extends Variation {

    private Appearance[] images = new Appearance[12];

    MonthVariation(JSONObject json, ElementButton element) {
        super(json, element);
    }

    @Override
    public void loadImages() {
        ArrayList<Appearance> imageAndNames = this.loadAppearances();
        for (int i = 0;i < 12;i++) {
            if (i < imageAndNames.size()) {
                this.images[i] = imageAndNames.get(i);
            } else {
                break;
            }
        }
    }

    @Override
    public Appearance getAppearance() {
        return this.images[LocalDateTime.now().getMonthValue() - 1];
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getPairs() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (Appearance appearance : this.images) {
            list.addAll(appearance.getPairs());
        }
        return list;
    }
}
