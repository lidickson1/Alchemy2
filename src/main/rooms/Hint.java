package main.rooms;

import main.*;
import main.buttons.Element;
import main.buttons.Group;
import main.buttons.iconbuttons.Exit;
import main.buttons.iconbuttons.IconButton;
import main.combos.Combo;
import main.combos.NormalCombo;
import main.combos.RandomCombo;
import processing.core.PConstants;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

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
            if (combo.canCreate() && !main.game.isDiscovered(combo.getElement())) {
                possibleElements.add(combo.getElement());
            }
        }
        for (RandomCombo randomCombo : main.randomCombos) {
            if (randomCombo.canCreate()) {
                HashSet<String> elements = randomCombo.getAllResults();
                elements.removeIf(e -> main.game.isDiscovered(e));
                possibleElements.addAll(elements);
            }
        }

        if (possibleElements.size() == 0) {
            throw new NoHintAvailable();
        }

        Collections.shuffle(possibleElements);
        main.game.setHintElement(Element.getElement(possibleElements.get(0)));
        main.switchRoom(main.game);
    }

    //TODO: MultiCombo with ingredients that just happen to only be in 2 groups
    private void getGroupHint() throws NoHintAvailable {
        ArrayList<NormalCombo> possibleCombos = new ArrayList<>();
        for (Combo combo : main.comboList) {
            if (combo instanceof NormalCombo && combo.canCreate() && !main.game.isDiscovered(combo.getElement())) {
                possibleCombos.add((NormalCombo) combo);
            }
        }
        for (RandomCombo randomCombo : main.randomCombos) {
            if (randomCombo.canCreate() && randomCombo.notAllResultsDiscovered()) {
                possibleCombos.addAll(randomCombo.getCanCreate());
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
