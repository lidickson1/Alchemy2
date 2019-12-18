package main.rooms;

import main.buttons.Element;
import main.buttons.Group;
import main.buttons.Pane;
import main.buttons.SaveFile;
import main.buttons.arrows.Arrow;
import main.buttons.iconbuttons.Exit;
import main.buttons.iconbuttons.IconButton;
import main.buttons.iconbuttons.Save;
import main.exceptions.NoElementException;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import processing.core.PConstants;
import processing.data.JSONArray;
import processing.data.JSONObject;

import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class Game extends Room {

    private TreeMap<Group, ArrayList<Element>> discovered = new TreeMap<>(Group::compareTo);
    private ArrayList<ImmutableTriple<Element, Element, Element>> history = new ArrayList<>();

    private SaveFile saveFile;

    private Pane success;
    private Pane hint;
    private Save save;
    private Exit exit;
    private IconButton historyButton;
    private IconButton hintButton;

    private Arrow groupLeftArrow;
    private Arrow groupRightArrow;
    private Arrow elementAUpArrow;
    private Arrow elementADownArrow;
    private Arrow elementBUpArrow;
    private Arrow elementBDownArrow;

    private LocalDateTime hintTime; //time of next hint

    private boolean gameLoaded = false;
    private Element hintElement;

    public Game() {
        this.success = new Pane() {
            @Override
            protected String getText() {
                return main.getLanguageSelected().getLocalizedString("game", "you created");
            }
        };
        this.hint = new Pane() {
            @Override
            protected String getText() {
                return main.getLanguageSelected().getLocalizedString("game", "element hint");
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
                return Group.getGroupSelectedA() != null && Element.pageNumberA > 0;
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
                return Group.getGroupSelectedA() != null && Element.pageNumberA < Element.totalPagesA - 1;
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
                return Group.getGroupSelectedB() != null && Element.pageNumberB > 0;
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
                return Group.getGroupSelectedB() != null && Element.pageNumberB < Element.totalPagesB - 1;
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
    }

    @Override
    public void setup() {
        //this ensures that the following code only runs when initially entering game
        if (!this.gameLoaded) {
            this.discovered.clear();
            this.history.clear();

            Group.reset();
            Element.reset();

            if (this.saveFile == null) {
                //new game
                try {
                    this.addElement("alchemy:air");
                    this.addElement("alchemy:earth");
                    this.addElement("alchemy:fire");
                    this.addElement("alchemy:water");
                } catch (NoElementException e) {
                    System.err.println("Error: Starting elements could not be added");
                }
                this.hintTime = LocalDateTime.now().plusMinutes(3);
            } else {
                JSONArray array = this.saveFile.getJson().getJSONArray("elements");
                for (int i = 0; i < array.size(); i++) {
                    try {
                        this.addElement(array.getString(i));
                    } catch (NoElementException e) {
                        System.err.println("Error: " + array.getString(i) + " could not be loaded from save!");
                        main.loadGame.failed();
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
            int result = JOptionPane.showConfirmDialog(null, main.getLanguageSelected().getLocalizedString("game", "not saved"), main.getLanguageSelected().getLocalizedString("misc", "warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
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
        main.text(main.getLanguageSelected().getLocalizedString("game", "elements") + ": " + this.getNumberOfElements() + getGap() +
                main.getLanguageSelected().getLocalizedString("game", "groups") + ": " + this.discovered.keySet().size() + getGap() +
                main.getLanguageSelected().getLocalizedString("game", "hint timer") + ": " + this.getTimeString(), 0, 2);

        Group.drawGroups();
        Element.drawElements();

        this.groupLeftArrow.draw(Group.GROUP_X, Group.GROUP_Y + (Group.SIZE + Group.GAP) * Group.groupCountY);
        this.groupRightArrow.draw((Group.SIZE + Group.GAP) * Group.groupCountX - Arrow.SIZE, Group.GROUP_Y + (Group.SIZE + Group.GAP) * Group.groupCountY);

        this.elementAUpArrow.draw(main.screenWidth - 20 - Arrow.SIZE, Group.groupSelectedAY);
        this.elementADownArrow.draw(main.screenWidth - 20 - Arrow.SIZE, Group.groupSelectedBY - Group.GAP - Arrow.SIZE);

        this.elementBUpArrow.draw(main.screenWidth - 20 - Arrow.SIZE, Group.groupSelectedBY);
        this.elementBDownArrow.draw(main.screenWidth - 20 - Arrow.SIZE, Group.groupSelectedBY * 2 - Group.GAP - Arrow.SIZE - Group.GROUP_Y);

        if (this.success.isActive()) {
            this.success.draw(main.screenWidth / 2 - this.success.getWidth() / 2, main.screenHeight / 2 - this.success.getHeight() / 2);
            Element.drawCreatedElements();
        }

        if (this.hint.isActive()) {
            this.hint.draw(main.screenWidth / 2 - this.hint.getWidth() / 2, main.screenHeight / 2 - this.hint.getHeight() / 2);
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

    private void addElement(String name) throws NoElementException {
        Element element = Element.getElement(name);
        if (element == null) {
            throw new NoElementException(name);
        }
        this.addElement(element);
    }

    boolean isDiscovered(String element) {
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

    public ArrayList<ImmutableTriple<Element, Element, Element>> getHistory() {
        return this.history;
    }

    public TreeMap<Group, ArrayList<Element>> getDiscovered() {
        return this.discovered;
    }

    public void addElement(Element element) {
        Group group = element.getGroup();
        if (!this.discovered.containsKey(group)) {
            this.discovered.put(group, new ArrayList<>(Collections.singletonList(element)));
        } else if (!this.discovered.get(group).contains(element)) { //TODO: remove this check for puzzle mode
            this.discovered.get(group).add(element);
        }
        this.discovered.get(group).sort(Element::compareTo);
    }

    @Override
    public void mousePressed() {
        this.exit.mousePressed();
        this.save.mousePressed();
        this.historyButton.mousePressed();
        this.hintButton.mousePressed();

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

            if (Group.getGroupSelectedA() != null) {
                Group.getGroupSelectedA().mousePressed();
                for (Element element : Element.getElementsA()) {
                    element.mousePressed();
                }
            }

            if (Group.getGroupSelectedB() != null) {
                Group.getGroupSelectedB().mousePressed();
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
                main.hintRoom.getElementHint();
            }
        } else if (main.key == 'c') {
            for (String element : main.elements.keySet()) {
                try {
                    this.addElement(element);
                } catch (NoElementException ignored) {
                }
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
