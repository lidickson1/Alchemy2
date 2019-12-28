package main.rooms;

import main.*;
import main.buttons.Arrow;
import main.buttons.Element;
import main.buttons.Group;
import main.buttons.iconbuttons.Exit;
import main.combos.Combo;
import main.combos.MultiCombo;
import main.combos.NormalCombo;
import main.combos.RandomCombo;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import processing.core.PConstants;
import processing.core.PImage;

import java.util.ArrayList;

public class ElementRoom extends Room {

    private static final int GAP = 30;
    //private static final int MAX = 5;
    private static PImage plus;
    private static PImage equal;

    private static int creationPageNumber;
    private static int creationTotalPages;
    private static int usedPageNumber;
    private static int usedTotalPages;
    private static int max;

    private Arrow creationLeftArrow;
    private Arrow creationRightArrow;
    private Arrow usedLeftArrow;
    private Arrow usedRightArrow;

    private Exit exit;

    private Element element;
    private ArrayList<ImmutableTriple<Element, Element, Element>> creation = new ArrayList<>();
    private ArrayList<ImmutableTriple<Element, Element, Element>> used = new ArrayList<>();

    public ElementRoom() {
        this.exit = new Exit();

        this.creationLeftArrow = new Arrow(Arrow.LEFT) {
            @Override
            protected boolean canDraw() {
                return ElementRoom.creationPageNumber > 0;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    ElementRoom.creationPageNumber--;
                }
            }
        };

        this.creationRightArrow = new Arrow(Arrow.RIGHT) {
            @Override
            protected boolean canDraw() {
                return ElementRoom.creationPageNumber < ElementRoom.creationTotalPages - 1;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    ElementRoom.creationPageNumber++;
                }
            }
        };

        this.usedLeftArrow = new Arrow(Arrow.LEFT) {
            @Override
            protected boolean canDraw() {
                return ElementRoom.usedPageNumber > 0;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    ElementRoom.usedPageNumber--;
                }
            }
        };

        this.usedRightArrow = new Arrow(Arrow.RIGHT) {
            @Override
            protected boolean canDraw() {
                return ElementRoom.usedPageNumber < ElementRoom.usedTotalPages - 1;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    ElementRoom.usedPageNumber++;
                }
            }
        };
    }

    @Override
    public void setup() {
        if (plus == null) {
            plus = main.loadImage("resources/images/plus.png");
        }

        if (equal == null) {
            equal = main.loadImage("resources/images/equal.png");
        }

        //making deep copies or else in bounds method will fuck up
        this.creation.clear();
        this.used.clear();
        //no else ifs because the element can be both
        for (Combo combo : main.comboList) {
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                if (normalCombo.getElement().equals(this.element.getName()) && combo.ingredientsDiscovered()) {
                    this.creation.addAll(normalCombo.toTriples());
                }
                if ((normalCombo.getA().equals(this.element.getName()) || normalCombo.getB().equals(this.element.getName())) && combo.ingredientsDiscovered() && main.game.isDiscovered(normalCombo.getElement())) {
                    //ingredients must be discovered too
                    this.used.addAll(normalCombo.toTriples());
                }
            } else if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (multiCombo.getElement().equals(this.element.getName()) && combo.ingredientsDiscovered()) {
                    this.creation.addAll(multiCombo.toTriples());
                }
                if (multiCombo.getIngredients().contains(this.element.getName()) && combo.ingredientsDiscovered() && main.game.isDiscovered(multiCombo.getElement())) {
                    this.used.addAll(multiCombo.toTriples());
                }
            }
        }

        for (RandomCombo combo : main.randomCombos) {
            if (combo.isResult(this.element.getName())) {
                this.creation.addAll(combo.toCreationTriples(this.element));
            }
            if (combo.isIngredient(this.element.getName())) {
                this.used.addAll(combo.toUsedTriples(this.element));
            }
        }

        creationPageNumber = 0;
        usedPageNumber = 0;

    }

    @Override
    public void draw() {
        main.noStroke();
        main.fill(0);
        main.rect(0, 0, main.screenWidth, main.screenHeight);

        Element.touching = null;

        int y = Group.GROUP_Y;
        this.element.draw(Group.GROUP_X, y);

        if (this.element.getDescription() != null) {
            int x = Group.GROUP_X + Element.SIZE + 40;
            int width = main.screenWidth - Group.GROUP_X - x;
            main.textAlign(PConstants.LEFT);
            main.textLeading(20);
            main.fill(255);
            main.text(this.element.getDescription(), x, y + 10, width, main.screenHeight);
        }
        y += Element.HEIGHT + 40;

        int x = (main.screenWidth / 2 - Group.GAP * 2) / 2;
        main.textAlign(PConstants.CENTER);
        main.fill(255);
        main.textSize(20);
        main.text(Language.getLanguageSelected().getLocalizedString("information", "creation"), x, y);

        y += 40;
        max = Math.floorDiv(main.screenHeight - y - 40 - Arrow.SIZE, Element.HEIGHT + GAP);
        int length = Element.SIZE + GAP + plus.width + GAP + Element.SIZE + GAP + equal.width + GAP + Element.SIZE;
        int start = x - length / 2;

        creationTotalPages = (int) Math.ceil((float) this.creation.size() / max);
        usedTotalPages = (int) Math.ceil((float) this.used.size() / max);

        ArrayList<ImmutableTriple<Element, Element, Element>> triples = this.getCreationTriples();
        for (int i = 0; i < triples.size(); i++) {
            ImmutableTriple<Element, Element, Element> triple = triples.get(i);
            x = start;
            if (i > 0 && triples.get(i - 1).right == null && triple.left != null) {
                main.image(plus, x - Element.SIZE - GAP, y);
            }
            if (triple.left != null) {
                triple.left.draw(x, y);
            }
            x += Element.SIZE + GAP;
            main.image(plus, x, y);
            x += plus.width + GAP;
            triple.middle.draw(x, y);
            x += Element.SIZE + GAP;
            if (triple.right != null) {
                main.image(equal, x, y);
                x += equal.width + GAP;
                triple.right.draw(x, y);
            }
            y += Element.HEIGHT + GAP;
        }

        //ensures that the button is always drawn at the bottom, even when there are not enough triples
        x = start;
        y = Group.GROUP_Y + Element.HEIGHT + 40 + 40 + (Element.HEIGHT + GAP) * max;
        this.creationLeftArrow.draw(x, y);
        this.creationRightArrow.draw(x + length - Arrow.SIZE, y);

        x = (main.screenWidth / 2 - Group.GAP * 2) / 2 + main.screenWidth / 2;
        y = Group.GROUP_Y + Element.HEIGHT + 40;
        main.textAlign(PConstants.CENTER);
        main.fill(255);
        main.textSize(20);
        main.text(Language.getLanguageSelected().getLocalizedString("information", "used to create"), x, y);

        start += main.screenWidth / 2;
        y += 40;
        triples = this.getUsedTriples();
        for (int i = 0; i < triples.size(); i++) {
            ImmutableTriple<Element, Element, Element> triple = triples.get(i);
            x = start;
            if (i > 0 && triples.get(i - 1).right == null && triple.left != null) {
                main.image(plus, x - Element.SIZE - GAP, y);
            }
            if (triple.left != null) {
                triple.left.draw(x, y);
            }
            x += Element.SIZE + GAP;
            main.image(plus, x, y);
            x += plus.width + GAP;
            triple.middle.draw(x, y);
            x += Element.SIZE + GAP;
            if (triple.right != null) {
                main.image(equal, x, y);
                x += equal.width + GAP;
                triple.right.draw(x, y);
            }
            y += Element.HEIGHT + GAP;
        }

        Element.drawTooltip();

        x = start;
        y = Group.GROUP_Y + Element.HEIGHT + 40 + 40 + (Element.HEIGHT + GAP) * max;
        this.usedLeftArrow.draw(x, y);
        this.usedRightArrow.draw(x + length - Arrow.SIZE, y);

        this.exit.draw();
    }

    public void setElement(Element element) {
        this.element = element;
    }

    private ArrayList<ImmutableTriple<Element, Element, Element>> getCreationTriples() {
        ArrayList<ImmutableTriple<Element, Element, Element>> list = new ArrayList<>();
        //when the element cannot be created, return an empty list
        if (creationTotalPages == 0) {
            creationPageNumber = 0;
            return list;
        }
        if (creationPageNumber >= creationTotalPages) {
            creationPageNumber = creationTotalPages - 1;
        }
        for (int i = creationPageNumber * max; i < (creationPageNumber + 1) * max; i++) {
            if (i < this.creation.size()) {
                list.add(this.creation.get(i));
            }
        }
        return list;
    }

    private ArrayList<ImmutableTriple<Element, Element, Element>> getUsedTriples() {
        ArrayList<ImmutableTriple<Element, Element, Element>> list = new ArrayList<>();
        //when the element has no uses, return an empty list
        if (usedTotalPages == 0) {
            usedPageNumber = 0;
            return list;
        }
        if (usedPageNumber >= usedTotalPages) {
            usedPageNumber = usedTotalPages - 1;
        }
        for (int i = usedPageNumber * max; i < (usedPageNumber + 1) * max; i++) {
            if (i < this.used.size()) {
                list.add(this.used.get(i));
            }
        }
        return list;
    }

    @Override
    public void mousePressed() {
        this.creationLeftArrow.mousePressed();
        this.creationRightArrow.mousePressed();
        this.usedLeftArrow.mousePressed();
        this.usedRightArrow.mousePressed();
        this.exit.mousePressed();

        for (ImmutableTriple<Element, Element, Element> triple : this.getCreationTriples()) {
            if (triple.left != null) {
                triple.left.mousePressed();
            }
            triple.middle.mousePressed();
            if (triple.right != null) {
                triple.right.mousePressed();
            }
        }

        for (ImmutableTriple<Element, Element, Element> triple : this.getUsedTriples()) {
            if (triple.left != null) {
                triple.left.mousePressed();
            }
            triple.middle.mousePressed();
            if (triple.right != null) {
                triple.right.mousePressed();
            }
        }
    }

}
