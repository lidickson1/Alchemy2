package main.variations;

import main.Element;
import main.LoadElements;
import main.combos.Combo;
import main.variations.appearances.Appearance;
import main.variations.appearances.Texture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;

public class ComboVariation extends Variation {

    private final ArrayList<ImmutablePair<Combo, Appearance>> pairs = new ArrayList<>();
    private Appearance current;

    ComboVariation(JSONObject json, Element element) {
        super(json, element);
    }

    public void setCurrentImage(Combo combo) {
        for (ImmutablePair<Combo, Appearance> pair : this.pairs) {
            if (combo.equals(pair.left)) {
                this.current = pair.right;
                return;
            }
        }
    }

    @Override
    public void loadImages() {
        JSONArray array = this.getJson().getJSONArray("textures");
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            JSONArray comboArray = object.getJSONArray("combos");
            for (int j = 0; j < comboArray.size(); j++) {
                ArrayList<Combo> combos = LoadElements.getCombo(comboArray.getJSONObject(j), this.getElement());
                for (Combo combo : combos) {
                    this.pairs.add(new ImmutablePair<>(combo, Appearance.getAppearance(this, object)));
                }
            }
        }
        this.current = new Texture(this, this.getElement());
    }

    @Override
    public Appearance getAppearance() {
        return this.current;
    }

    @Override
    public ArrayList<ImmutablePair<PImage, String>> getPairs() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (ImmutablePair<Combo, Appearance> pair : this.pairs) {
            list.addAll(pair.right.getPairs());
        }
        return list;
    }

}
