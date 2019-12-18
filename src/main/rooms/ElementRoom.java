package main.rooms;

import main.Combo;
import main.buttons.Arrow;
import main.buttons.Element;
import main.buttons.Group;
import main.buttons.iconbuttons.Exit;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import processing.core.PConstants;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Objects;

public class ElementRoom extends Room {

    private static final int GAP = 30;
    private static final int MAX = 5;
    private static PImage plus;
    private static PImage equal;

    private static int creationPageNumber;
    private static int creationTotalPages;
    private static int usedPageNumber;
    private static int usedTotalPages;

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

        //TODO: make deep copies! or else in bounds method will fuck up
        this.creation.clear();
        this.used.clear();
        for (Combo combo : main.comboList) {
            if (combo.getElement().equals(this.element.getName()) && main.game.isDiscovered(combo.getA()) && main.game.isDiscovered(combo.getB())) {
                this.creation.add(new ImmutableTriple<>(
                        Objects.requireNonNull(Element.getElement(combo.getA())).deepCopy(),
                        Objects.requireNonNull(Element.getElement(combo.getB())).deepCopy(),
                        Objects.requireNonNull(Element.getElement(combo.getElement())).deepCopy()
                ));
            } else if ((combo.getA().equals(this.element.getName()) || combo.getB().equals(this.element.getName())) && main.game.isDiscovered(combo.getA()) && main.game.isDiscovered(combo.getB()) && main.game.isDiscovered(combo.getElement())) {
                //ingredients must be discovered too
                this.used.add(new ImmutableTriple<>(
                        Objects.requireNonNull(Element.getElement(combo.getA())).deepCopy(),
                        Objects.requireNonNull(Element.getElement(combo.getB())).deepCopy(),
                        Objects.requireNonNull(Element.getElement(combo.getElement())).deepCopy()
                ));
            }
        }

        if (main.multiComboList.containsKey(this.element.getName())) {
            for (ArrayList<String> list : main.multiComboList.get(this.element.getName())) {
                this.creation.addAll(this.toTriples(this.element, list));
            }
        }

        for (String key : main.multiComboList.keySet()) {
            for (ArrayList<String> list : main.multiComboList.get(key)) {
                if (list.contains(this.element.getName())) {
                    boolean allDiscovered = true;
                    for (String element : list) {
                        if (!main.game.isDiscovered(element)) {
                            allDiscovered = false;
                            break;
                        }
                    }
                    if (allDiscovered) {
                        this.used.addAll(this.toTriples(Element.getElement(key), list));
                    }
                }
            }
        }

        creationPageNumber = 0;
        creationTotalPages = (int) Math.ceil((float) this.creation.size() / MAX);
        usedPageNumber = 0;
        usedTotalPages = (int) Math.ceil((float) this.used.size() / MAX);
    }

    private ArrayList<ImmutableTriple<Element, Element, Element>> toTriples(Element element, ArrayList<String> elements) {
        ArrayList<ImmutableTriple<Element, Element, Element>> list = new ArrayList<>();
        int counter = elements.size();
        do {
            MutableTriple<Element, Element, Element> triple;
            if (counter >= 2) {
                triple = new MutableTriple<>(Element.getElement(elements.get(elements.size() - counter)), Element.getElement(elements.get(elements.size() - counter + 1)), null);
                counter -= 2;
            } else {
                triple = new MutableTriple<>(null, Element.getElement(elements.get(elements.size() - 1)), null);
                counter--;
            }
            if (counter == 0) {
                triple.right = element;
            }
            list.add(new ImmutableTriple<>(triple.left, triple.middle, triple.right));
        } while (counter > 0);
        return list;
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
        main.text(main.getLanguageSelected().getLocalizedString("information", "creation"), x, y);

        int length = Element.SIZE + GAP + plus.width + GAP + Element.SIZE + GAP + equal.width + GAP + Element.SIZE;
        int start = x - length / 2;
        y += 40;
        for (int i = creationPageNumber * MAX; i < (creationPageNumber + 1) * MAX; i++) {
            if (i < this.creation.size()) {
                ImmutableTriple<Element, Element, Element> triple = this.creation.get(i);
                x = start;
                if (i > 0 && this.creation.get(i - 1).right == null && triple.left != null) {
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
        }

        //ensures that the button is always drawn at the bottom, even when there are not enough triples
        x = start;
        y = Group.GROUP_Y + Element.HEIGHT + 40 + 40 + (Element.HEIGHT + GAP) * MAX;
        this.creationLeftArrow.draw(x, y);
        this.creationRightArrow.draw(x + length - Arrow.SIZE, y);

        x = (main.screenWidth / 2 - Group.GAP * 2) / 2 + main.screenWidth / 2;
        y = Group.GROUP_Y + Element.HEIGHT + 40;
        main.textAlign(PConstants.CENTER);
        main.fill(255);
        main.textSize(20);
        main.text(main.getLanguageSelected().getLocalizedString("information", "used to create"), x, y);

        start += main.screenWidth / 2;
        y += 40;
        for (int i = usedPageNumber * MAX; i < (usedPageNumber + 1) * MAX; i++) {
            if (i < this.used.size()) {
                ImmutableTriple<Element, Element, Element> triple = this.used.get(i);
                x = start;
                if (i > 0 && this.used.get(i - 1).right == null && triple.left != null) {
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
        }

        Element.drawTooltip();

        x = start;
        y = Group.GROUP_Y + Element.HEIGHT + 40 + 40 + (Element.HEIGHT + GAP) * MAX;
        this.usedLeftArrow.draw(x, y);
        this.usedRightArrow.draw(x + length - Arrow.SIZE, y);

        this.exit.draw();
    }

    public void setElement(Element element) {
        this.element = element;
    }

    @Override
    public void mousePressed() {
        this.creationLeftArrow.mousePressed();
        this.creationRightArrow.mousePressed();
        this.usedLeftArrow.mousePressed();
        this.usedRightArrow.mousePressed();
        this.exit.mousePressed();

        for (int i = creationPageNumber * MAX; i < (creationPageNumber + 1) * MAX; i++) {
            if (i < this.creation.size()) {
                ImmutableTriple<Element, Element, Element> triple = this.creation.get(i);
                if (triple.left != null) {
                    triple.left.mousePressed();
                }
                triple.middle.mousePressed();
                if (triple.right != null) {
                    triple.right.mousePressed();
                }
            }
        }

        for (int i = usedPageNumber * MAX; i < (usedPageNumber + 1) * MAX; i++) {
            if (i < this.used.size()) {
                ImmutableTriple<Element, Element, Element> triple = this.used.get(i);
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

}
