package main;

import main.buttons.Element;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class RandomCombo extends Entity {

    private EnumeratedDistribution<ArrayList<String>> elements;
    private ArrayList<Combo> combos = new ArrayList<>();

    RandomCombo(JSONArray array) {
        ArrayList<Pair<ArrayList<String>, Double>> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            JSONArray elements = jsonObject.getJSONArray("elements");
            for (int j = 0; j < elements.size(); j++) {
                ArrayList<String> elementsList = new ArrayList<>();
                Object object = elements.get(j);
                if (object instanceof String) {
                    if (Element.getElement((String) object) == null) {
                        System.err.println("Error with random combo: " + object + " doesn't exist!");
                        continue;
                    }
                    elementsList.add((String) object);
                } else if (object instanceof JSONObject) {
                    String name = ((JSONObject) object).getString("element");
                    int amount = ((JSONObject) object).getInt("amount");
                    if (Element.getElement(name) == null) {
                        System.err.println("Error with random combo: " + name + " doesn't exist!");
                        continue;
                    }
                    for (int k = 0; k < amount; k++) {
                        elementsList.add(name);
                    }
                }
                list.add(new Pair<>(elementsList, jsonObject.getDouble("weight")));
            }
        }
        this.elements = new EnumeratedDistribution<>(list);
    }

    void removeElement(String element) {
        for (Pair<ArrayList<String>, Double> pair : this.elements.getPmf()) {
            pair.getKey().remove(element);
        }
    }

    void addCombo(Combo combo) {
        this.combos.add(combo);
    }

    public boolean canCreate() {
        for (Combo combo : this.combos) {
            if (combo.canCreate()) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<NormalCombo> getCanCreate() {
        ArrayList<NormalCombo> list = new ArrayList<>();
        for (Combo combo : this.combos) {
            if (combo instanceof NormalCombo && combo.canCreate()) {
                list.add((NormalCombo) combo);
            }
        }
        return list;
    }

    //returns the normal combo that was triggered (if any)
    public NormalCombo canCreate(Element a, Element b) {
        for (Combo combo : this.combos) {
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                if (normalCombo.getA().equals(a.getName()) && normalCombo.getB().equals(b.getName()) || (normalCombo.getA().equals(b.getName()) && normalCombo.getB().equals(a.getName()))) {
                    return normalCombo;
                }
            }
        }
        return null;
    }

    public MultiCombo canCreate(ArrayList<String> elements) {
        for (Combo combo : this.combos) {
            if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (CollectionUtils.isEqualCollection(multiCombo.getIngredients(), elements)) {
                    return multiCombo;
                }
            }
        }
        return null;
    }

    public ArrayList<Element> getElements() {
        ArrayList<Element> elements = new ArrayList<>();
        ArrayList<String> list = this.elements.sample();
        for (String string : list) {
            elements.add(Element.getElement(string));
        }
        return elements;
    }

    public boolean isIngredient(String element) {
        for (Combo combo : this.combos) {
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                if (normalCombo.getA().equals(element) || normalCombo.getB().equals(element)) {
                    return true;
                }
            } else if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (multiCombo.getIngredients().contains(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean notAllResultsDiscovered() {
        for (String element : this.getAllResults()) {
            if (!main.game.isDiscovered(element)) {
                return true;
            }
        }
        return false;
    }

    public boolean isResult(String element) {
        for (Pair<ArrayList<String>, Double> pair : this.elements.getPmf()) {
            if (pair.getKey().contains(element)) {
                return true;
            }
        }
        return false;
    }

    public HashSet<String> getAllResults() {
        HashSet<String> set = new HashSet<>();
        for (Pair<ArrayList<String>, Double> pair : this.elements.getPmf()) {
            set.addAll(pair.getKey());
        }
        return set;
    }

    public ArrayList<ImmutableTriple<Element, Element, Element>> toCreationTriples(Element element) {
        ArrayList<ImmutableTriple<Element, Element, Element>> list = new ArrayList<>();
        for (Combo combo : this.combos) {
            //cannot use combo's toTriple because the element field is null
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                Element a = Objects.requireNonNull(Element.getElement(normalCombo.getA())).deepCopy();
                Element b = Objects.requireNonNull(Element.getElement(normalCombo.getB())).deepCopy();
                if (normalCombo.ingredientsDiscovered()) {
                    list.add(new ImmutableTriple<>(a, b, element.deepCopy()));
                }
            } else if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (multiCombo.ingredientsDiscovered()) {
                    int counter = multiCombo.getIngredients().size();
                    do {
                        MutableTriple<Element, Element, Element> triple;
                        if (counter >= 2) {
                            triple = new MutableTriple<>(Objects.requireNonNull(Element.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - counter))).deepCopy(), Objects.requireNonNull(Element.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - counter + 1))).deepCopy(), null);
                            counter -= 2;
                        } else {
                            triple = new MutableTriple<>(null, Objects.requireNonNull(Element.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - 1))).deepCopy(), null);
                            counter--;
                        }
                        if (counter == 0) {
                            triple.right = element.deepCopy();
                        }
                        list.add(new ImmutableTriple<>(triple.left, triple.middle, triple.right));
                    } while (counter > 0);
                }
            }
        }
        return list;
    }

    public ArrayList<ImmutableTriple<Element, Element, Element>> toUsedTriples(Element element) {
        ArrayList<ImmutableTriple<Element, Element, Element>> list = new ArrayList<>();
        for (Combo combo : this.combos) {
            //cannot use combo's toTriple because the element field is null
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                Element a = Objects.requireNonNull(Element.getElement(normalCombo.getA())).deepCopy();
                Element b = Objects.requireNonNull(Element.getElement(normalCombo.getB())).deepCopy();
                if (a.getName().equals(element.getName()) || b.getName().equals(element.getName())) {
                    for (String string : this.getAllResults()) {
                        if (normalCombo.ingredientsDiscovered()) {
                            list.add(new ImmutableTriple<>(a, b, Element.getElement(string)));
                        }
                    }
                }
            } else if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (multiCombo.isIngredient(element)) {
                    for (String string : this.getAllResults()) {
                        if (multiCombo.ingredientsDiscovered()) {
                            int counter = multiCombo.getIngredients().size();
                            do {
                                MutableTriple<Element, Element, Element> triple;
                                if (counter >= 2) {
                                    triple = new MutableTriple<>(Objects.requireNonNull(Element.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - counter))).deepCopy(), Objects.requireNonNull(Element.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - counter + 1))).deepCopy(), null);
                                    counter -= 2;
                                } else {
                                    triple = new MutableTriple<>(null, Objects.requireNonNull(Element.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - 1))).deepCopy(), null);
                                    counter--;
                                }
                                if (counter == 0) {
                                    triple.right = Objects.requireNonNull(Element.getElement(string)).deepCopy();
                                }
                                list.add(new ImmutableTriple<>(triple.left, triple.middle, triple.right));
                            } while (counter > 0);
                        }
                    }
                }
            }
        }
        return list;
    }

    void removeCombo(String a, String b) {
        this.removeCombo(new ArrayList<>(Arrays.asList(a, b)));
    }

    void removeCombo(ArrayList<String> ingredients) {
        this.combos.removeIf(e -> CollectionUtils.isEqualCollection(e.getIngredients(), ingredients));
    }

    boolean isEmpty() {
        return this.combos.size() == 0;
    }

}
