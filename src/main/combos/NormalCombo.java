package main.combos;

import main.buttons.Element;
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
        return main.game.isDiscovered(this.a) && main.game.isDiscovered(this.b);
    }

    @Override
    public ArrayList<ImmutableTriple<Element, Element, Element>> toTriples() {
        Element a = Objects.requireNonNull(Element.getElement(this.a)).deepCopy();
        Element b = Objects.requireNonNull(Element.getElement(this.b)).deepCopy();
        Element element = Objects.requireNonNull(Element.getElement(this.getElement())).deepCopy();
        return new ArrayList<>(Collections.singletonList(new ImmutableTriple<>(a, b, element)));
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
