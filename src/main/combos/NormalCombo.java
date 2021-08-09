package main.combos;

import main.Element;
import main.buttons.ElementButton;
import main.rooms.Game;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class NormalCombo extends Combo {

    private String a;
    private String b;

    public NormalCombo(String element, String a, String b) {
        super(element);
        this.a = a;
        this.b = b;
    }

    public String getA() {
        return this.a;
    }

    public String getB() {
        return this.b;
    }

    @Override
    public boolean canCreate() {
        return Game.INSTANCE.isDiscovered(this.a) && Game.INSTANCE.isDiscovered(this.b);
    }

    @Override
    public ArrayList<ImmutableTriple<ElementButton, ElementButton, ElementButton>> toTriples() {
        return new ArrayList<>(Collections.singletonList(new ImmutableTriple<>(
                new ElementButton(Element.Companion.getElement(this.a)),
                new ElementButton(Element.Companion.getElement(this.b)),
                new ElementButton(Element.Companion.getElement(this.getElement())))));
    }

    @Override
    public ArrayList<String> getIngredients() {
        return new ArrayList<>(Arrays.asList(this.a, this.b));
    }

    @Override
    public boolean ingredientsDiscovered() {
        return this.canCreate();
    }

    @Override
    public boolean contains(String element) {
        return this.a.equals(element) || this.b.equals(element) || this.getElement().equals(element);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NormalCombo)) {
            return false;
        }
        NormalCombo normalCombo = (NormalCombo) obj;
        return (this.a.equals(normalCombo.a) && this.b.equals(normalCombo.b)) || (this.a.equals(normalCombo.b) || this.b.equals(normalCombo.a));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getElement(), this.a, this.b);
    }
}
