package main.rooms;

import main.Language;
import main.buttons.*;
import main.buttons.iconbuttons.Exit;
import main.buttons.iconbuttons.IconButton;
import main.buttons.iconbuttons.Save;
import main.combos.Combo;
import org.apache.commons.collections4.CollectionUtils;
import processing.core.PConstants;
import processing.data.JSONArray;
import processing.data.JSONObject;

import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Game extends Room {

    private final TreeMap<Group, ArrayList<Element>> discovered = new TreeMap<>(Group::compareTo);
    private final ArrayList<Combo> history = new ArrayList<>();

    private SaveFile saveFile;

    private final Pane success;
    private final Pane hint;
    private final Save save;
    private final Exit exit;
    private final IconButton historyButton;
    private final IconButton hintButton;
    private final IconButton undoButton;

    private final Arrow groupLeftArrow;
    private final Arrow groupRightArrow;
    private final Arrow elementAUpArrow;
    private final Arrow elementADownArrow;
    private final Arrow elementBUpArrow;
    private final Arrow elementBDownArrow;

    private LocalDateTime hintTime; //time of next hint

    private boolean gameLoaded = false;
    private Element hintElement;

    public String mode;

    public Game() {
        this.success = new Pane() {
            @Override
            protected String getText() {
                return Language.getLanguageSelected().getLocalizedString("game", "you created");
            }
        };
        this.hint = new Pane() {
            @Override
            protected String getText() {
                return Language.getLanguageSelected().getLocalizedString("game", "element hint");
            }
        };
        this.save = new Save();
        this.exit = new Exit();

        this.groupLeftArrow = new Arrow(Arrow.LEFT) {
            @Override
            protected boolean canDraw() {
                return Group.pageNumber > 0;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    Group.pageNumber--;
                }
            }
        };

        this.groupRightArrow = new Arrow(Arrow.RIGHT) {
            @Override
            protected boolean canDraw() {
                return Group.pageNumber < Group.totalPages - 1;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    Group.pageNumber++;
                }
            }
        };

        this.elementAUpArrow = new Arrow(Arrow.UP) {
            @Override
            protected boolean canDraw() {
                return Group.groupSelectedA != null && Element.pageNumberA > 0;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    Element.pageNumberA--;
                }
            }
        };

        this.elementADownArrow = new Arrow(Arrow.DOWN) {
            @Override
            protected boolean canDraw() {
                return Group.groupSelectedA != null && Element.pageNumberA < Element.totalPagesA - 1;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    Element.pageNumberA++;
                }
            }
        };

        this.elementBUpArrow = new Arrow(Arrow.UP) {
            @Override
            protected boolean canDraw() {
                return Group.groupSelectedB != null && Element.pageNumberB > 0;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    Element.pageNumberB--;
                }
            }
        };

        this.elementBDownArrow = new Arrow(Arrow.DOWN) {
            @Override
            protected boolean canDraw() {
                return Group.groupSelectedB != null && Element.pageNumberB < Element.totalPagesB - 1;
            }

            @Override
            public void clicked() {
                if (this.canDraw()) {
                    Element.pageNumberB++;
                }
            }
        };

        this.historyButton = new IconButton("resources/images/history_button.png") {
            @Override
            public void clicked() {
                main.switchRoom(main.historyRoom);
            }
        };

        this.hintButton = new IconButton("resources/images/hint_button.png") {
            @Override
            public void clicked() {
                main.switchRoom(main.hintRoom);
            }
        };

        this.undoButton = new IconButton("resources/images/undo_button.png") {
            @Override
            public void clicked() {
                Game.this.undo();
            }
        };
    }

    @Override
    public void setup() {
        //this ensures that the following code only runs when initially entering game
        if (!this.gameLoaded) {
            this.discovered.clear();
            this.history.clear();

            Group.reset();
            Element.reset();

            if (this.saveFile == null) { //new game
                this.mode = "normal";
                ArrayList<Element> elements = new ArrayList<>();
                for (Pack pack : main.packsRoom.getLoadedPacks()) {
                    pack.getStartingElements(elements);
                }
                for (Element element : elements) {
                    this.addElement(element);
                }
                this.hintTime = LocalDateTime.now().plusMinutes(3);
            } else {
                this.mode = this.saveFile.getJson().hasKey("mode") ? this.saveFile.getJson().getString("mode") : "normal";
                JSONArray array = this.saveFile.getJson().getJSONArray("elements");
                for (int i = 0; i < array.size(); i++) {
                    if (!this.addElement(array.getString(i))) {
                        System.err.println("Error: " + array.getString(i) + " could not be loaded from save!");
                        main.loadGame.failed();
                        return;
                    }
                }
                this.hintTime = this.saveFile.getJson().hasKey("hint time") ? LocalDateTime.now().plus(Duration.parse(this.saveFile.getJson().getString("hint time"))) : LocalDateTime.now().plusMinutes(3);
            }

            this.gameLoaded = true;
            this.success.setActive(false);
            this.hint.setActive(false);
        }
    }

    public void exitGame() {
        if (this.saveFile != null && this.saveFile.getJson().getJSONArray("elements").size() < this.getNumberOfElements()) {
            int result = JOptionPane.showConfirmDialog(null, Language.getLanguageSelected().getLocalizedString("game", "not saved"), Language.getLanguageSelected().getLocalizedString("misc", "warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        main.switchRoom(main.menu);
    }

    @Override
    public void draw() {
        main.noStroke();
        main.fill(0);
        main.rect(0, 0, main.screenWidth, main.screenHeight);

        main.textFont(main.font, 20);

        main.fill(255);
        main.textAlign(PConstants.LEFT, PConstants.TOP);
        main.text(Language.getLanguageSelected().getLocalizedString("game", "elements") + ": " + this.getNumberOfElements() + getGap() +
                Language.getLanguageSelected().getLocalizedString("game", "groups") + ": " + this.discovered.keySet().size() + getGap() +
                Language.getLanguageSelected().getLocalizedString("game", "hint timer") + ": " + this.getTimeString(), 10, 10);

        Group.drawGroups();
        Element.drawElements();

        this.groupLeftArrow.draw(Group.GROUP_X, Group.GROUP_Y + (Group.SIZE + Group.GAP) * Group.groupCountY);
        this.groupRightArrow.draw((Group.SIZE + Group.GAP) * Group.groupCountX - Arrow.SIZE, Group.GROUP_Y + (Group.SIZE + Group.GAP) * Group.groupCountY);

        this.elementAUpArrow.draw(main.screenWidth - 20 - Arrow.SIZE, Group.groupSelectedAY);
        this.elementADownArrow.draw(main.screenWidth - 20 - Arrow.SIZE, Group.groupSelectedBY - Group.GAP - Arrow.SIZE);

        this.elementBUpArrow.draw(main.screenWidth - 20 - Arrow.SIZE, Group.groupSelectedBY);
        this.elementBDownArrow.draw(main.screenWidth - 20 - Arrow.SIZE, Group.groupSelectedBY * 2 - Group.GAP - Arrow.SIZE - Group.GROUP_Y);

        if (this.success.isActive()) {
            this.success.draw(main.screenWidth / 2F - this.success.getWidth() / 2F, main.screenHeight / 2F - this.success.getHeight() / 2F);
            Element.drawCreatedElements();
        }

        if (this.hint.isActive()) {
            this.hint.draw(main.screenWidth / 2F - this.hint.getWidth() / 2F, main.screenHeight / 2F - this.hint.getHeight() / 2F);
            Element.drawHintElement(this.hintElement);
        }

        //clear multi-select
        if (!isShiftHeld()) {
            if (Element.getElementsSelected().size() > 0) {
                Element.checkForMultiCombos();
            }
            Element.getElementsSelected().clear();

            int x = main.screenWidth - IconButton.GAP - IconButton.SIZE;
            int y = main.screenHeight - IconButton.GAP - IconButton.SIZE;
            this.exit.draw();
            this.save.draw(x - (IconButton.SIZE + IconButton.GAP), y);
            this.historyButton.draw(x - (IconButton.SIZE + IconButton.GAP) * 2, y);
            this.hintButton.draw(x - (IconButton.SIZE + IconButton.GAP) * 3, y);
            if (this.mode.equals("puzzle")) {
                this.undoButton.draw(x - (IconButton.SIZE + IconButton.GAP) * 4, y);
            }
        }
    }

    public void setSaveFile(SaveFile saveFile) {
        this.saveFile = saveFile;
        this.gameLoaded = false;
    }

    public void saveGame() {
        if (this.saveFile == null) {
            this.saveFile = new SaveFile(main.saveRoom.getSaveName(), new JSONObject());
        }

        JSONArray array = new JSONArray();
        for (ArrayList<Element> list : this.discovered.values()) {
            for (Element element : list) {
                array.append(element.getName());
            }
        }
        this.saveFile.getJson().put("elements", array);

        this.saveFile.getJson().put("last modified", LocalDateTime.now().format(main.formatter));
        this.saveFile.getJson().put("hint time", Duration.between(LocalDateTime.now(), this.hintTime).toString());

        main.saveJSONObject(this.saveFile.getJson(), "resources/saves/" + this.saveFile.getName() + ".json", "indent=4");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isHintReady() {
        return Duration.between(LocalDateTime.now(), this.hintTime).isZero() || Duration.between(LocalDateTime.now(), this.hintTime).isNegative();
    }

    String getTimeString() {
        //TODO: maybe separate the update time logic?
        Duration duration = Duration.between(LocalDateTime.now(), this.hintTime);
        if (duration.isNegative()) {
            duration = Duration.ZERO;
        }
        int minutes = (int) duration.getSeconds() / 60;
        String minutesString;
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = Integer.toString(minutes);
        }
        int seconds = (int) duration.getSeconds() - minutes * 60;
        String secondsString;
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = Integer.toString(seconds);
        }
        return minutesString + ":" + secondsString;
    }

    private int getNumberOfElements() {
        int sum = 0;
        for (ArrayList<Element> list : this.discovered.values()) {
            sum += list.size();
        }
        return sum;
    }

    private static String getGap() {
        return "          ";
    }

    private boolean addElement(String name) {
        Element element = Element.getElement(name);
        if (element == null) {
            return false;
        }
        this.addElement(element);
        return true;
    }

    public boolean isDiscovered(String element) {
        Group group = main.elements.get(element);
        if (!this.discovered.containsKey(group)) {
            return false;
        }
        for (Element e : this.discovered.get(group)) {
            if (e.getName().equals(element)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Combo> getHistory() {
        return this.history;
    }

    public TreeMap<Group, ArrayList<Element>> getDiscovered() {
        return this.discovered;
    }

    private void undo() {
        if (this.history.size() > 0) {
            ArrayList<String> ingredients = this.history.get(this.history.size() - 1).getIngredients();
            ArrayList<String> created = new ArrayList<>();
            //we need to get all combos that had the same ingredients, because the same ingredients can trigger multiple combos
            int i;
            for (i = this.history.size() - 1; i >= 0; i--) {
                Combo combo = this.history.get(i);
                if (CollectionUtils.isEqualCollection(ingredients, combo.getIngredients())) {
                    for (int j = 0; j < combo.getAmount(); j++) {
                        created.add(combo.getElement());
                    }
                } else {
                    break;
                }
            }
            //remove from history
            for (int j = this.history.size() - 1; j > i; j--) {
                this.history.remove(this.history.size() - 1);
            }
            for (String ingredient : ingredients) {
                if (!Objects.requireNonNull(Element.getElement(ingredient)).isPersistent()) {
                    this.addElement(ingredient);
                }
            }
            for (String element : created) {
                this.removeElement(element);
            }
            Element.updateA();
            Element.updateB();
        }
    }

    public void addElement(Element element) {
        Group group = element.getGroup();
        boolean addElement = false;
        if (this.mode.equals("normal")) {
            addElement = !this.discovered.containsKey(group) || !this.isDiscovered(element.getName());
        } else if (this.mode.equals("puzzle")) {
            addElement = !(element.isPersistent() && this.discovered.containsKey(group) && this.isDiscovered(element.getName()));
        }
        if (addElement) {
            if (!this.discovered.containsKey(group)) {
                this.discovered.put(group, new ArrayList<>(Collections.singletonList(element)));
            } else {
                this.discovered.get(group).add(element);
            }
        }
        this.discovered.get(group).sort(Element::compareTo);
    }

    //direct Element objects cannot be used because they are copies
    public void removeElement(String string) {
        Group group = main.elements.get(string);
        //using iterator here because we are only removing one occurrence
        Iterator<Element> iterator = this.discovered.get(group).iterator();
        while (iterator.hasNext()) {
            Element element = iterator.next();
            if (element.getName().equals(string)) {
                iterator.remove();
                break;
            }
        }
        if (this.discovered.get(group).size() == 0) {
            this.discovered.remove(group);
        }
    }

    @Override
    public void mousePressed() {
        this.exit.mousePressed();
        this.save.mousePressed();
        this.historyButton.mousePressed();
        this.hintButton.mousePressed();
        this.undoButton.mousePressed();

        this.groupLeftArrow.mousePressed();
        this.groupRightArrow.mousePressed();

        this.elementAUpArrow.mousePressed();
        this.elementADownArrow.mousePressed();
        this.elementBUpArrow.mousePressed();
        this.elementBDownArrow.mousePressed();

        if (!this.success.isActive() && !this.hint.isActive()) {
            for (Group group : Group.getGroups()) {
                group.mousePressed();
            }

            if (Group.groupSelectedA != null) {
                Group.groupSelectedA.mousePressed();
                for (Element element : Element.getElementsA()) {
                    element.mousePressed();
                }
            }

            if (Group.groupSelectedB != null) {
                Group.groupSelectedB.mousePressed();
                for (Element element : Element.getElementsB()) {
                    element.mousePressed();
                }
            }

            Element.checkForCombos();
        } else {
            this.success.mousePressed();
            this.hint.mousePressed();
        }
    }

    public SaveFile getSaveFile() {
        return this.saveFile;
    }

    private static boolean isShiftHeld() {
        return main.keyPressed && main.keyCode == PConstants.SHIFT;
    }

    @Override
    public void keyPressed() {
        if (main.key == PConstants.CODED) {
            if (main.keyCode == PConstants.LEFT) {
                this.groupLeftArrow.clicked();
            } else if (main.keyCode == PConstants.RIGHT) {
                this.groupRightArrow.clicked();
            } else if (main.keyCode == PConstants.UP) {
                try {
                    main.hintRoom.getElementHint();
                } catch (Hint.NoHintAvailable ignored) {
                }
            }
        } else if (main.key == 'c') {
            for (String element : main.elements.keySet()) {
                this.addElement(element);
            }
        }
    }

    public void success() {
        this.success.setActive(true);
    }

    void setHintElement(Element hintElement) {
        this.hintElement = hintElement;
        this.hint.setActive(true);
    }

    void resetHint() {
        this.hintTime = LocalDateTime.now().plusMinutes(3);
    }

}
