package main.combos;

import main.buttons.Element;
import main.buttons.Group;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class MultiCombo extends Combo {

    private ArrayList<String> ingredients;

    public MultiCombo(String element, ArrayList<String> ingredients) {
        super(element);
        this.ingredients = ingredients;
        this.ingredients.sort(String::compareTo);
    }

    @Override
    public ArrayList<String> getIngredients() {
        return this.ingredients;
    }

    @Override
    public boolean ingredientsDiscovered() {
        for (String ingredient : this.ingredients) {
            if (!main.game.isDiscovered(ingredient)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canCreate() {
        return this.ingredientsDiscovered();
    }

    boolean isIngredient(Element element) {
        return this.ingredients.contains(element.getName());
    }

    @Override
    public ArrayList<ImmutableTriple<Element, Element, Element>> toTriples() {
        ArrayList<ImmutableTriple<Element, Element, Element>> list = new ArrayList<>();
        int counter = this.ingredients.size();
        do {
            MutableTriple<Element, Element, Element> triple;
            if (counter >= 2) {
                triple = new MutableTriple<>(Objects.requireNonNull(Element.getElement(this.ingredients.get(this.ingredients.size() - counter))).deepCopy(), Objects.requireNonNull(Element.getElement(this.ingredients.get(this.ingredients.size() - counter + 1))).deepCopy(), null);
                counter -= 2;
            } else {
                triple = new MutableTriple<>(null, Objects.requireNonNull(Element.getElement(this.ingredients.get(this.ingredients.size() - 1))).deepCopy(), null);
                counter--;
            }
            if (counter == 0) {
                triple.right = Objects.requireNonNull(Element.getElement(this.getElement())).deepCopy();
            }
            list.add(new ImmutableTriple<>(triple.left, triple.middle, triple.right));
        } while (counter > 0);
        return list;
    }

    public boolean ingredientsInTwoGroups() {
        HashSet<Group> groups = new HashSet<>();
        for (String element : this.ingredients) {
            groups.add(main.elements.get(element));
            if (groups.size() > 2) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MultiCombo)) {
            return false;
        }
        MultiCombo multiCombo = (MultiCombo) obj;
        return CollectionUtils.isEqualCollection(this.ingredients, multiCombo.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getElement(), this.ingredients);
    }
}
