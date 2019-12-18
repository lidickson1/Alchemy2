package main.rooms;

import main.buttons.Element;
import main.buttons.arrows.Arrow;
import main.buttons.iconbuttons.Exit;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import processing.core.PImage;

public class HistoryRoom extends Room {

    private static final int GAP = 30;
    private static final int MAX = 6;
    private static PImage plus;
    private static PImage equal;

    private static int pageNumber;
    private static int totalPages;

    private Exit exit;
    private Arrow leftArrow;
    private Arrow rightArrow;

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
        totalPages = (int) Math.ceil((float) main.game.getHistory().size() / MAX);
    }

    @Override
    public void draw() {
        this.drawTitle("history", "history");

        int length = Element.SIZE + GAP + plus.width + GAP + Element.SIZE + GAP + equal.width + GAP + Element.SIZE;
        int x;
        int y = 150;
        for (int i = pageNumber * MAX; i < (pageNumber + 1) * MAX; i++) {
            if (i < main.game.getHistory().size()) {
                ImmutableTriple<Element, Element, Element> triple = main.game.getHistory().get(i);
                x = main.screenWidth / 2 - length / 2;
                if (i > 0 && main.game.getHistory().get(i - 1).right == null && triple.left != null) {
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
