package main.rooms;

import main.Combo;
import main.Language;
import main.buttons.Element;
import main.buttons.Group;
import main.buttons.iconbuttons.Exit;
import main.buttons.iconbuttons.IconButton;
import processing.core.PConstants;

import javax.swing.*;
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
                try {
                    Hint.this.getElementHint();
                    main.game.resetHint();
                } catch (NoHintAvailable noHintAvailable) {
                    Hint.this.showDialog();
                }
            }
        };
        this.groupHint = new IconButton("resources/images/group_hint_button.png") {
            @Override
            public void clicked() {
                try {
                    Hint.this.getGroupHint();
                    main.game.resetHint();
                } catch (NoHintAvailable noHintAvailable) {
                    Hint.this.showDialog();
                }
            }
        };
        this.exit = new Exit();
    }

    private void showDialog() {
        JOptionPane.showMessageDialog(null, Language.getLanguageSelected().getLocalizedString("hint","all discovered"), Language.getLanguageSelected().getLocalizedString("misc", "information"), JOptionPane.INFORMATION_MESSAGE);
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
        main.text(Language.getLanguageSelected().getLocalizedString("hint", "time remaining"), main.screenWidth / 2F, 250);
        main.text(main.game.getTimeString(), main.screenWidth / 2F, 300);

        final int gap = 300;
        final float width = (main.screenWidth - gap * 3) / 2F;
        main.textLeading(20);
        main.textSize(20);

        main.fill(255);
        main.text(Language.getLanguageSelected().getLocalizedString("hint", "element hint"), main.screenWidth / 2F - gap / 2F - width / 2, 500);
        this.elementHint.draw(main.screenWidth / 2F - gap / 2F - width / 2 - IconButton.SIZE / 2F, 530);
        this.elementHint.setDisabled(!main.game.isHintReady());

        main.fill(255); //need to reset colour because button in bounds cause colour change
        main.text(Language.getLanguageSelected().getLocalizedString("hint", "group hint"), main.screenWidth / 2F + gap / 2F + width / 2, 500);
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
    void getElementHint() throws NoHintAvailable {
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

        if (possibleElements.size() == 0) {
            throw new NoHintAvailable();
        }

        Collections.shuffle(possibleElements);
        main.game.setHintElement(Element.getElement(possibleElements.get(0)));
        main.switchRoom(main.game);
    }

    private void getGroupHint() throws NoHintAvailable {
        ArrayList<Combo> possibleCombos = new ArrayList<>();
        for (Combo combo : main.comboList) {
            if (!main.game.isDiscovered(combo.getElement()) && main.game.isDiscovered(combo.getA()) && main.game.isDiscovered(combo.getB())) {
                possibleCombos.add(combo);
            }
        }

        if (possibleCombos.size() == 0) {
            throw new NoHintAvailable();
        }

        Collections.shuffle(possibleCombos);
        Group.setHintGroups(main.elements.get(possibleCombos.get(0).getA()), main.elements.get(possibleCombos.get(0).getB()));
        main.switchRoom(main.game);
    }

    static class NoHintAvailable extends Exception {

    }

}
