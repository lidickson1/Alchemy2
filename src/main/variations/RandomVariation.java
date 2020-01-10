package main.variations;

import main.buttons.Element;
import main.variations.appearances.Appearance;
import main.variations.appearances.Texture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public class RandomVariation extends Variation {

    private EnumeratedDistribution<Appearance> random;

    RandomVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        ArrayList<Pair<Appearance, Double>> list = new ArrayList<>();
        double remainingWeight = 1;
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            list.add(new Pair<>(Appearance.getAppearance(this, object), object.getDouble("weight")));
            remainingWeight -= object.getDouble("weight");
        }
        list.add(new Pair<>(new Texture(this), remainingWeight)); //chance of getting the original image
        this.random = new EnumeratedDistribution<>(list);
    }

    @Override
    public Appearance getAppearance() {
        return this.random.sample();
    }

//    //even though the null pair indicates we want the original image, it will actually have the fallback image, hence why we need to check and change it
//    @Override
//    public PImage getImage() {
//        PImage image = super.getImage();
//        if (image == Button.error) {
//            return null;
//        } else {
//            return image;
//        }
//    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getPairs() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (Pair<Appearance, Double> pair : this.random.getPmf()) {
            list.addAll(pair.getFirst().getPairs());
        }
        return list;
    }

    //this must be used so it doesn't get a random name every time, but the name corresponding the chosen image
    public String getName(PImage image) {
        for (Pair<Appearance, Double> pair : this.random.getPmf()) {
            if (pair.getFirst().getImage() != null && pair.getFirst().getImage() == image) {
                return pair.getFirst().getName();
            }
        }
        return null;
    }

}
