package main.variations;

import main.buttons.Element;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public class RandomVariation extends Variation {

    private EnumeratedDistribution<PImage> random;

    RandomVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public PImage getImage() {
        return this.random.sample();
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        ArrayList<Pair<PImage, Double>> list = new ArrayList<>();
        double remainingWeight = 1;
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            PImage image = this.element.getImage(object.getString("texture"));
            image.resize(Element.SIZE, Element.SIZE);
            list.add(new Pair<>(image, object.getDouble("weight")));
            remainingWeight -= object.getDouble("weight");
        }
        list.add(new Pair<>(this.element.getImage(), remainingWeight)); //chance of getting the original image
        this.random = new EnumeratedDistribution<>(list);
    }

}
