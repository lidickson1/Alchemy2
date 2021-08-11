package main.combos;

import main.Element;
import main.Entity;
import main.buttons.ElementButton;
import main.rooms.Game;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.*;

public class RandomCombo extends Entity {

    private final EnumeratedDistribution<ArrayList<String>> elements;
    private final ArrayList<Combo> combos = new ArrayList<>();

    public RandomCombo(JSONArray array) {
        ArrayList<Pair<ArrayList<String>, Double>> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            JSONArray elements = jsonObject.getJSONArray("elements");
            for (int j = 0; j < elements.size(); j++) {
                ArrayList<String> elementsList = new ArrayList<>();
                Object object = elements.get(j);
                if (object instanceof String) {
                    if (Element.Companion.getElement((String) object) == null) {
                        System.err.println("Error with random combo: " + object + " doesn't exist!");
                        continue;
                    }
                    elementsList.add((String) object);
                } else if (object instanceof JSONObject) {
                    String name = ((JSONObject) object).getString("element");
                    int amount = ((JSONObject) object).getInt("amount");
                    if (Element.Companion.getElement(name) == null) {
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

    public void removeElement(String element) {
        for (Pair<ArrayList<String>, Double> pair : this.elements.getPmf()) {
            pair.getKey().remove(element);
        }
    }

    public void addCombo(Combo combo) {
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
                if (normalCombo.getA().equals(a.getId()) && normalCombo.getB().equals(b.getId()) || (normalCombo.getA().equals(b.getId()) && normalCombo.getB().equals(a.getId()))) {
                    return normalCombo;
                }
            }
        }
        return null;
    }

    public MultiCombo canCreate(List<String> elements) {
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
            elements.add(Element.Companion.getElement(string));
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
            if (!Game.INSTANCE.isDiscovered(element)) {
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

    public ArrayList<ImmutableTriple<ElementButton, ElementButton, ElementButton>> toCreationTriples(Element element) {
        ArrayList<ImmutableTriple<ElementButton, ElementButton, ElementButton>> list = new ArrayList<>();
        for (Combo combo : this.combos) {
            //cannot use combo's toTriple because the element field is null
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                if (normalCombo.ingredientsDiscovered()) {
                    list.add(new ImmutableTriple<>(new ElementButton(Element.Companion.getElement(normalCombo.getA())),
                            new ElementButton(Element.Companion.getElement(normalCombo.getB())),
                            new ElementButton(element)));
                }
            } else if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (multiCombo.ingredientsDiscovered()) {
                    int counter = multiCombo.getIngredients().size();
                    do {
                        MutableTriple<ElementButton, ElementButton, ElementButton> triple;
                        if (counter >= 2) {
                            triple = new MutableTriple<>(
                                    new ElementButton(Element.Companion.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - counter))),
                                    new ElementButton(Element.Companion.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - counter + 1))),
                                    null);
                            counter -= 2;
                        } else {
                            triple = new MutableTriple<>(
                                    null,
                                    new ElementButton(Element.Companion.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - 1))),
                                    null);
                            counter--;
                        }
                        if (counter == 0) {
                            triple.right = new ElementButton(element);
                        }
                        list.add(new ImmutableTriple<>(triple.left, triple.middle, triple.right));
                    } while (counter > 0);
                }
            }
        }
        return list;
    }

    public ArrayList<ImmutableTriple<ElementButton, ElementButton, ElementButton>> toUsedTriples(Element element) {
        ArrayList<ImmutableTriple<ElementButton, ElementButton, ElementButton>> list = new ArrayList<>();
        for (Combo combo : this.combos) {
            //cannot use combo's toTriple because the element field is null
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                Element a = Element.Companion.getElement(normalCombo.getA());
                Element b = Element.Companion.getElement(normalCombo.getB());
                if (a == element || b == element) {
                    for (String string : this.getAllResults()) {
                        if (normalCombo.ingredientsDiscovered()) {
                            list.add(new ImmutableTriple<>(new ElementButton(a), new ElementButton(b), new ElementButton(Element.Companion.getElement(string))));
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
                                MutableTriple<ElementButton, ElementButton, ElementButton> triple;
                                if (counter >= 2) {
                                    triple = new MutableTriple<>(new ElementButton(Element.Companion.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - counter))),
                                            new ElementButton(Element.Companion.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - counter + 1))),
                                            null);
                                    counter -= 2;
                                } else {
                                    triple = new MutableTriple<>(null,
                                            new ElementButton(Element.Companion.getElement(multiCombo.getIngredients().get(multiCombo.getIngredients().size() - 1))),
                                            null);
                                    counter--;
                                }
                                if (counter == 0) {
                                    triple.right = new ElementButton(Element.Companion.getElement(string));
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

    public void removeCombo(String a, String b) {
        this.removeCombo(new ArrayList<>(Arrays.asList(a, b)));
    }

    public void removeCombo(ArrayList<String> ingredients) {
        this.combos.removeIf(e -> CollectionUtils.isEqualCollection(e.getIngredients(), ingredients));
    }

    public boolean isEmpty() {
        return this.combos.size() == 0;
    }

    public boolean contains(String element) {
        for (Combo combo : this.combos) {
            if (combo.contains(element)) {
                return true;
            }
        }
        return this.getAllResults().contains(element);
    }

}
