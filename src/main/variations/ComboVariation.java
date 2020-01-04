package main.variations;

import main.LoadElements;
import main.buttons.Element;
import main.combos.Combo;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public class ComboVariation extends Variation {

    private ArrayList<ImmutablePair<Combo, ImageAndName>> pairs = new ArrayList<>();
    private ImageAndName current;

    ComboVariation(JSONObject json, Element element) {
        super(json, element);
    }

    public void setCurrentImage(Combo combo) {
        for (ImmutablePair<Combo, ImageAndName> pair : this.pairs) {
            if (combo.equals(pair.left)) {
                this.current = pair.right;
                return;
            }
        }
        this.current = null;
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            JSONArray comboArray = object.getJSONArray("combos");
            for (int j = 0;j < comboArray.size();j++) {
                ArrayList<Combo> combos = LoadElements.getCombo(comboArray.getJSONObject(j), this.element);
                for (Combo combo : combos) {
                    this.pairs.add(new ImmutablePair<>(combo, new ImageAndName(object.getString("texture"), object.getString("name"))));
                }
            }
        }
        this.current = null;
    }

    @Override
    public ImageAndName getImageAndName() {
        return this.current;
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getImages() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (ImmutablePair<Combo, ImageAndName> pair : this.pairs) {
            if (pair.right.hasImage()) {
                list.add(pair.right.toPair());
            }
        }
        return list;
    }

}
