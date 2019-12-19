package main.rooms;

import main.Combo;
import main.buttons.Element;
import main.buttons.Group;
import main.buttons.iconbuttons.Exit;
import main.buttons.iconbuttons.IconButton;
import processing.core.PConstants;

import java.util.ArrayList;
import java.util.Collections;

public class Hint extends Room {

    private IconButton elementHint;
    private IconButton groupHint;
    private Exit exit;

    public Hint() {
        this.elementHint = new IconButton("resources/images/element_hint_button.png") {
            @Override
            public void clicked() {
                Hint.this.getElementHint();
                main.game.resetHint();
            }
        };
        this.groupHint = new IconButton("resources/images/group_hint_button.png") {
            @Override
            public void clicked() {
                Hint.this.getGroupHint();
                main.game.resetHint();
            }
        };
        this.exit = new Exit();
    }

    @Override
    public void setup() {

    }

    @Override
    public void draw() {
        this.drawTitle("hint", "hints");

        main.fill(255);
        main.textAlign(PConstants.CENTER);
        main.textSize(30);
        main.text(main.getLanguageSelected().getLocalizedString("hint", "time remaining"), main.screenWidth / 2F, 250);
        main.text(main.game.getTimeString(), main.screenWidth / 2F, 300);

        final int gap = 300;
        final float width = (main.screenWidth - gap * 3) / 2F;
        main.textLeading(20);
        main.textSize(20);

        main.fill(255);
        main.text(main.getLanguageSelected().getLocalizedString("hint", "element hint"), main.screenWidth / 2F - gap / 2F - width / 2, 500);
        this.elementHint.draw(main.screenWidth / 2F - gap / 2F - width / 2 - IconButton.SIZE / 2F, 530);
        this.elementHint.setDisabled(!main.game.isHintReady());

        main.fill(255); //need to reset colour because button in bounds cause colour change
        main.text(main.getLanguageSelected().getLocalizedString("hint", "group hint"), main.screenWidth / 2F + gap / 2F + width / 2, 500);
        this.groupHint.draw(main.screenWidth / 2F + gap / 2F + width / 2 - IconButton.SIZE / 2F, 530);
        this.groupHint.setDisabled(!main.game.isHintReady());

        this.exit.draw();
    }

    @Override
    public void mousePressed() {
        this.elementHint.mousePressed();
        this.groupHint.mousePressed();
        this.exit.mousePressed();
    }

    //TODO: this is not private for debug reasons
    void getElementHint() {
        ArrayList<String> possibleElements = new ArrayList<>();
        for (Combo combo : main.comboList) {
            if (!main.game.isDiscovered(combo.getElement()) && main.game.isDiscovered(combo.getA()) && main.game.isDiscovered(combo.getB())) {
                possibleElements.add(combo.getElement());
            }
        }
        for (String key : main.multiComboList.keySet()) {
            if (!main.game.isDiscovered(key)) {
                for (ArrayList<String> list : main.multiComboList.get(key)) {
                    boolean discovered = true;
                    for (String element : list) {
                        if (!main.game.isDiscovered(element)) {
                            discovered = false;
                            break;
                        }
                    }
                    if (discovered) {
                        possibleElements.add(key);
                        break;
                    }
                }
            }
        }
        Collections.shuffle(possibleElements);
        main.game.setHintElement(Element.getElement(possibleElements.get(0)));
        main.switchRoom(main.game);
    }

    private void getGroupHint() {
        ArrayList<Combo> possibleCombos = new ArrayList<>();
        for (Combo combo : main.comboList) {
            if (!main.game.isDiscovered(combo.getElement()) && main.game.isDiscovered(combo.getA()) && main.game.isDiscovered(combo.getB())) {
                possibleCombos.add(combo);
            }
        }
        Collections.shuffle(possibleCombos);
        Group.setHintGroups(main.elements.get(possibleCombos.get(0).getA()), main.elements.get(possibleCombos.get(0).getB()));
        main.switchRoom(main.game);
    }

}
