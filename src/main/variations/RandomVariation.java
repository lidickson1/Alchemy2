package main.variations;

import main.buttons.Button;
import main.buttons.Element;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public class RandomVariation extends Variation {

    private EnumeratedDistribution<ImageAndName> random;

    RandomVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        ArrayList<Pair<ImageAndName, Double>> list = new ArrayList<>();
        double remainingWeight = 1;
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            list.add(new Pair<>(new ImageAndName(object.getString("texture"), object.getString("name")), object.getDouble("weight")));
            remainingWeight -= object.getDouble("weight");
        }
        list.add(new Pair<>(new ImageAndName(null, null), remainingWeight)); //chance of getting the original image
        this.random = new EnumeratedDistribution<>(list);
    }

    @Override
    public ImageAndName getImageAndName() {
        return this.random.sample();
    }

    //even though the null pair indicates we want the original image, it will actually have the fallback image, hence why we need to check and change it
    @Override
    public PImage getImage() {
        PImage image = super.getImage();
        if (image == Button.error) {
            return null;
        } else {
            return image;
        }
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getImages() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (Pair<ImageAndName, Double> pair : this.random.getPmf()) {
            if (pair.getFirst().hasImage()) {
                list.add(pair.getFirst().toPair());
            }
        }
        return list;
    }

    //this must be used so it doesn't get a random name every time, but the name corresponding the chosen image
    public String getName(PImage image) {
        for (Pair<ImageAndName, Double> pair : this.random.getPmf()) {
            if (pair.getFirst().getImage() != null && pair.getFirst().getImage() == image) {
                return pair.getFirst().getName();
            }
        }
        return null;
    }

}
