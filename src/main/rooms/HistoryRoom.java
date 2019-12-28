package main.rooms;

import main.combos.Combo;
import main.buttons.Arrow;
import main.buttons.Element;
import main.buttons.iconbuttons.Exit;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import processing.core.PImage;

import java.util.ArrayList;

public class HistoryRoom extends Room {

    private static final int GAP = 30;
    private static PImage plus;
    private static PImage equal;

    private static int pageNumber;
    private static int totalPages;

    private Exit exit;
    private Arrow leftArrow;
    private Arrow rightArrow;

    private ArrayList<ImmutableTriple<Element, Element, Element>> triples = new ArrayList<>();

    public HistoryRoom() {
        this.exit = new Exit();
        this.leftArrow = new Arrow(Arrow.LEFT) {
            @Override
            protected boolean canDraw() {
                return HistoryRoom.pageNumber > 0;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    HistoryRoom.pageNumber--;
                }
            }
        };
        this.rightArrow = new Arrow(Arrow.RIGHT) {
            @Override
            protected boolean canDraw() {
                return HistoryRoom.pageNumber < HistoryRoom.totalPages - 1;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    HistoryRoom.pageNumber++;
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

        pageNumber = 0;

        this.triples.clear();
        for (Combo combo : main.game.getHistory()) {
            this.triples.addAll(combo.toTriples());
        }
    }

    @Override
    public void draw() {
        this.drawTitle("history", "history");

        int length = Element.SIZE + GAP + plus.width + GAP + Element.SIZE + GAP + equal.width + GAP + Element.SIZE;
        int x;
        int y = 150;
        int max = Math.floorDiv(main.screenHeight - y - 40 - Arrow.SIZE, Element.HEIGHT + GAP);
        totalPages = (int) Math.ceil((float) this.triples.size() / max);
        if (totalPages == 0) {
            pageNumber = 0;
        } else if (pageNumber >= totalPages) {
            pageNumber = totalPages - 1;
        }

        for (int i = pageNumber * max; i < (pageNumber + 1) * max; i++) {
            if (i < this.triples.size()) {
                ImmutableTriple<Element, Element, Element> triple = this.triples.get(i);
                x = main.screenWidth / 2 - length / 2;
                if (i > 0 && this.triples.get(i - 1).right == null && triple.left != null) {
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

        x = main.screenWidth / 2 - length / 2;
        this.leftArrow.draw(x, main.screenHeight - Arrow.SIZE - 30);
        this.rightArrow.draw(x + length - Arrow.SIZE, main.screenHeight - Arrow.SIZE - 30);

        this.exit.draw();
    }

    @Override
    public void mousePressed() {
        this.leftArrow.mousePressed();
        this.rightArrow.mousePressed();
        this.exit.mousePressed();
    }

}
