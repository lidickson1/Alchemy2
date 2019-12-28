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

    private ArrayList<ImmutablePair<Combo, PImage>> pairs = new ArrayList<>();
    private PImage currentImage;

    ComboVariation(JSONObject json, Element element) {
        super(json, element);
    }

    @Override
    public PImage getImage() {
        return this.currentImage;
    }

    public void setCurrentImage(Combo combo) {
        for (ImmutablePair<Combo, PImage> pair : this.pairs) {
            if (combo.equals(pair.left)) {
                this.currentImage = pair.right;
                return;
            }
        }
        this.currentImage = this.element.getImage();
    }

    @Override
    public void loadImages() {
        JSONArray array = this.json.getJSONArray("textures");
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            PImage image = this.element.getImage(object.getString("texture"));
            image.resize(Element.SIZE, Element.SIZE);
            JSONArray comboArray = object.getJSONArray("combos");
            for (int j = 0;j < comboArray.size();j++) {
                ArrayList<Combo> combos = LoadElements.getCombo(comboArray.getJSONObject(j), this.element);
                for (Combo combo : combos) {
                    this.pairs.add(new ImmutablePair<>(combo, image));
                }
            }
        }
        this.currentImage = this.element.getImage();
    }
}
