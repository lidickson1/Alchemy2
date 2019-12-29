package main.variations;

import main.buttons.Element;
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
            PImage image = this.element.getImage(object.getString("texture"));
            image.resize(Element.SIZE, Element.SIZE);
            list.add(new Pair<>(new ImageAndName(image, object.getString("name")), object.getDouble("weight")));
            remainingWeight -= object.getDouble("weight");
        }
        list.add(new Pair<>(new ImageAndName(this.element.getImage(), null), remainingWeight)); //chance of getting the original image
        this.random = new EnumeratedDistribution<>(list);
    }

    @Override
    public ImageAndName getImageAndName() {
        return this.random.sample();
    }

    //this must be used so it doesn't get a random name every time, but the name corresponding the chosen image
    public String getName(PImage image) {
        for (Pair<ImageAndName, Double> pair : this.random.getPmf()) {
            if (pair.getFirst().getImage().equals(image)) {
                return pair.getFirst().getName();
            }
        }
        return null;
    }

}
