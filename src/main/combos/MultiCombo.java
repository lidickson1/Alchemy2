package main.combos;

import main.Element;
import main.buttons.ElementButton;
import main.buttons.Group;
import main.rooms.Game;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class MultiCombo extends Combo {

    private final ArrayList<String> ingredients;

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
            if (!Game.INSTANCE.isDiscovered(ingredient)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(String element) {
        return this.ingredients.contains(element) || this.getElement().equals(element);
    }

    @Override
    public boolean canCreate() {
        return this.ingredientsDiscovered();
    }

    boolean isIngredient(Element element) {
        return this.ingredients.contains(element.getId());
    }

    @Override
    public ArrayList<ImmutableTriple<ElementButton, ElementButton, ElementButton>> toTriples() {
        ArrayList<ImmutableTriple<ElementButton, ElementButton, ElementButton>> list = new ArrayList<>();
        int counter = this.ingredients.size();
        do {
            MutableTriple<ElementButton, ElementButton, ElementButton> triple;
            if (counter >= 2) {
                triple = new MutableTriple<>(new ElementButton(Element.Companion.getElement(this.ingredients.get(this.ingredients.size() - counter))),
                        new ElementButton(Element.Companion.getElement(this.ingredients.get(this.ingredients.size() - counter + 1))),
                        null);
                counter -= 2;
            } else {
                triple = new MutableTriple<>(null,
                        new ElementButton(Element.Companion.getElement(this.ingredients.get(this.ingredients.size() - 1))),
                        null);
                counter--;
            }
            if (counter == 0) {
                triple.right = new ElementButton(Element.Companion.getElement(this.getElement()));
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
