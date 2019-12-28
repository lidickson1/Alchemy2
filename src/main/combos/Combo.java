package main.combos;

import main.Entity;
import main.buttons.Element;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;

public abstract class Combo extends Entity {

    private String element;
    private int amount = 1;

    Combo(String element) {
        this.element = element;
    }

    public String getElement() {
        return this.element;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public abstract boolean canCreate();

    public abstract ArrayList<ImmutableTriple<Element, Element, Element>> toTriples();

    public abstract ArrayList<String> getIngredients();

    public abstract boolean ingredientsDiscovered();

    public void setElement(String element) {
        this.element = element;
    }
}
