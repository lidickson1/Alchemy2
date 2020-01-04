package main.buttons;

import com.sun.istack.internal.Nullable;
import main.Language;
import main.combos.Combo;
import main.combos.MultiCombo;
import main.combos.NormalCombo;
import main.combos.RandomCombo;
import main.rooms.ElementRoom;
import main.rooms.Game;
import main.variations.AnimationVariation;
import main.variations.ComboVariation;
import main.variations.RandomVariation;
import main.variations.Variation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class Element extends Button implements Comparable<Element> {

    public static final int SIZE = 64;
    public static final int HEIGHT = SIZE + 30;
    private static final int GAP = 30;
    private static final int ALPHA_CHANGE = 10;
    private static final int FAILED_TIME = 500;

    private static int maxElements;

    private static ArrayList<Element> elementsA = new ArrayList<>();
    private static ArrayList<Element> elementsB = new ArrayList<>();

    private static Element elementSelectedA;
    private static Element elementSelectedB;
    private static ArrayList<Element> elementsSelected = new ArrayList<>(); //for selecting more than 2 elements

    public static int pageNumberA;
    public static int totalPagesA;
    public static int pageNumberB;
    public static int totalPagesB;

    private static ArrayList<Element> elementsCreated = new ArrayList<>();

    public static Element touching;

    private static long time = -1; //timer when combination is wrong

    private String name;
    private Group group;
    private ArrayList<String> tags = new ArrayList<>();
    private String description;
    private Pack pack;
    private boolean persistent;
    private Variation variation;

    private int alpha = 255;
    private int alphaChange;

    public Element(String name, Group group, Pack pack) {
        super(SIZE, HEIGHT);

        this.name = name;
        this.group = group;
        this.pack = pack;
        this.tintOverlay = false;
    }

    //copy constructor
    public Element(Element other) {
        super(SIZE, HEIGHT);
        this.name = other.name;
        this.group = other.group;
        this.tags = other.tags;
        this.description = other.description;
        this.pack = other.pack;
        this.persistent = other.persistent;
        this.variation = other.variation;
        this.alpha = other.alpha;
        this.alphaChange = other.alphaChange;
        //noinspection IncompleteCopyConstructor
        this.tintOverlay = false;
    }

    public static void reset() {
        elementsA.clear();
        elementsB.clear();
        elementSelectedA = null;
        elementSelectedB = null;
        elementsSelected.clear();
        pageNumberA = 0;
        pageNumberB = 0;
    }

    @Override
    public int compareTo(Element o) {
        return this.name.compareTo(o.name);
    }

    //file name without extension
    public PImage getImage(String fileName) {
        //check if a pack has the image, from top to bottom
        for (Pack pack : main.packsRoom.getLoadedPacks()) {
            //check for atlas first
            if (pack.getAtlasImage(fileName) != null) {
                return pack.getAtlasImage(fileName);
            }
            //fileName could be in the form of pack:element:variation because of variations
            String id = StringUtils.countMatches(fileName, ":") == 2 ? fileName.split(":")[2] : fileName;
            if (pack.getName().equals("Alchemy") && this.pack.getName().equals("Alchemy")) {
                //if the element is of the default pack and we are in the default pack right now, load default location
                String defaultPath = "resources/elements/alchemy/" + this.group.getID() + "/" + id + ".png";
                PImage image = new File(defaultPath).exists() ? main.loadImage(defaultPath) : error;
                image.resize(SIZE, SIZE);
                return image;
            } else {
                String packPath = pack.getPath() + "/elements/" + this.group.getPack().getNamespace() + "/" + this.group.getID() + "/" + id + ".png";
                if (new File(packPath).exists()) {
                    PImage image = main.loadImage(packPath);
                    image.resize(SIZE, SIZE);
                    return image;
                }
            }
        }

        return null;
    }

    @Override
    void setImage(PImage image) {
        //so there are 2 resizing going on here
        super.setImage(image);
        this.getImage().resize(SIZE, SIZE);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public ArrayList<String> getTags() {
        return this.tags;
    }

    public void setVariation(Variation variation) {
        this.variation = variation;
    }

    public Variation getVariation() {
        return this.variation;
    }

    public static void loadImage(ArrayList<Element> elements) {
        Thread thread = new Thread(() -> {
            for (Element element : elements) {
                //load original image
                if (element.getImage() == null) {
                    element.setImage(element.getImage(element.getID()));
                }

                if (element.variation != null) {
                    element.variation.loadImages();
                }

                main.loading.updateProgress();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    boolean isInPack(Pack pack) {
        String prefix = pack.getNamespace() + ":";
        return this.name.length() >= prefix.length() && this.name.substring(0, prefix.length()).equals(prefix);
    }

    @Nullable
    public static Element getElement(String name) {
        if (!main.elements.containsKey(name)) {
            return null;
        }
        if (!main.groups.containsKey(main.elements.get(name))) {
            System.err.println(main.elements.get(name).getName() + " group not found!");
            return null;
        }
        for (Element element : main.groups.get(main.elements.get(name))) {
            if (element.name.equals(name)) {
                return element;
            }
        }
        return null;
    }

    public static void drawElements() {
        //determine how many elements to draw horizontally
        int elementCountX = Math.floorDiv(main.screenWidth - (Group.groupSelectedX + Group.SIZE + Group.GAP + 20 + Arrow.SIZE), SIZE + GAP);
        //determine how many elements to draw vertically
        int elementCountY = Math.floorDiv(Group.groupSelectedBY - Group.groupSelectedAY, HEIGHT + 16);
        maxElements = elementCountX * elementCountY;

        touching = null;

        int x = Group.groupSelectedX + Group.SIZE + Group.GAP;
        int y = Group.groupSelectedAY;
        ArrayList<Element> elements;
        if (Group.groupSelectedA != null) {
            totalPagesA = (int) Math.ceil((float) main.game.getDiscovered().get(Group.groupSelectedA).size() / maxElements);
            elements = getElementsA();
            for (int i = 0; i < elements.size(); i++) {
                elements.get(i).updateAlpha();
                elements.get(i).draw(x, y);
                x += SIZE + GAP;
                if ((i + 1) % elementCountX == 0) {
                    x = Group.groupSelectedX + Group.SIZE + Group.GAP;
                    y += HEIGHT + 16;
                }
            }
        }

        x = Group.groupSelectedX + Group.SIZE + Group.GAP;
        y = Group.groupSelectedBY;
        if (Group.groupSelectedB != null) {
            totalPagesB = (int) Math.ceil((float) main.game.getDiscovered().get(Group.groupSelectedB).size() / maxElements);
            elements = getElementsB();
            for (int i = 0; i < elements.size(); i++) {
                elements.get(i).updateAlpha();
                elements.get(i).draw(x, y);
                x += SIZE + GAP;
                if ((i + 1) % elementCountX == 0) {
                    x = Group.groupSelectedX + Group.SIZE + Group.GAP;
                    y += HEIGHT + 16;
                }
            }
        }

        //reset transparency
        main.tint(255, 255);

        //draw tooltip, it's done here so it gets drawn on top of all the elements
        drawTooltip();

        //draw multi select
        x = Group.groupSelectedX;
        y = main.screenHeight - Group.GAP - HEIGHT;
        for (Element element : elementsSelected) {
            element.draw(x, y);
            x += SIZE + GAP;
        }
    }

    public static void drawTooltip() {
        if (touching != null) {
            //draw tool tip
            main.textSize(20);
            final int padding = 2; //some horizontal padding
            float width = main.textWidth(touching.getDisplayName()) + padding * 2;
            float height = main.textAscent() + main.textDescent();
            final float offset = 13; //13 pixel offset so it doesn't cover the cursor
            float x = main.mouseX + offset;

            if (x + width >= main.screenWidth) {
                x = main.screenWidth - width - 1; //subtracting 1 here so that the border is shown
            }

            main.stroke(255);
            main.fill(0);
            main.rect(x, main.mouseY, width, height);

            main.textAlign(PConstants.LEFT, PConstants.TOP);
            main.fill(main.getSettings().getBoolean("group colour") ? touching.group.getColour() : 255, touching.alpha);
            main.text(touching.getDisplayName(), x + padding, main.mouseY);
        }
    }

    public static void drawCreatedElements() {
        touching = null;
        int length = (SIZE + GAP) * elementsCreated.size() - GAP;
        int x = main.screenWidth / 2 - length / 2;
        for (Element element : elementsCreated) {
            element.draw(x, main.screenHeight / 2F - SIZE / 2F);
            x += SIZE + GAP;
        }
        drawTooltip();
    }

    public static void drawHintElement(Element element) {
        touching = null;
        element.draw(main.screenWidth / 2F - Element.SIZE / 2F, main.screenHeight / 2F - Element.SIZE / 2F);
        drawTooltip();
    }

    public String getName() {
        return this.name;
    }

    private String getNamespace() {
        return this.name.split(":")[0];
    }

    private String getID() {
        return this.name.split(":")[1];
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    private String getDisplayName() {
        if (this.variation != null) {
            String variationName;
            if (this.variation instanceof RandomVariation) {
                variationName = Language.getLanguageSelected().getElementLocalizedString(this.getNamespace(), ((RandomVariation) this.variation).getName(this.getImage()));
            } else {
                variationName = Language.getLanguageSelected().getElementLocalizedString(this.getNamespace(), this.variation.getName());
            }
            if (variationName != null) {
                return variationName;
            }
        }
        String displayName = Language.getLanguageSelected().getElementLocalizedString(this.getNamespace(), this.getID());
        //TODO
        if (displayName == null) {
            this.pack.generateEnglish(this.getID());
        }
        return displayName == null ? this.name : displayName;
    }

    private String getShortenedDisplayName() {
        String displayName = this.getDisplayName();
        while (main.textWidth(displayName + "...") >= SIZE) {
            displayName = displayName.substring(0, displayName.length() - 1);
        }
        return displayName + "...";
    }

    @Override
    protected void drawButton() {
        main.image(this.variation instanceof AnimationVariation ? this.variation.getImage() : this.getImage(), this.getX(), this.getY());
        main.fill(main.getSettings().getBoolean("group colour") ? this.group.getColour() : 255, this.alpha);
        main.textAlign(PConstants.CENTER);

        boolean drawTooltip = main.setFontSize(this.getDisplayName(), 20, SIZE);
        if (touching == null && this.inBounds() && drawTooltip) {
            touching = this;
        }
        main.text(drawTooltip ? this.getShortenedDisplayName() : this.getDisplayName(), this.getX() + SIZE / 2F, this.getY() + SIZE + 22);
        main.fill(255);

        if (main.getRoom() instanceof Game && (elementSelectedA == this || elementSelectedB == this)) {
            if (failed()) {
                main.stroke(255, 0, 0);
            } else {
                main.stroke(255);
            }
            main.noFill();
            main.rect(this.getX(), this.getY(), SIZE, HEIGHT);
        }

        //if failed timer passed failed time limit and it didn't get reset yet
        if (main.millis() - time > FAILED_TIME && time != -1) {
            time = -1; //reset timer
            elementSelectedA = null;
            elementSelectedB = null;
        }

        main.noStroke();
    }

    private static boolean failed() {
        return time != -1 && main.millis() - time <= FAILED_TIME;
    }

    public ArrayList<ImmutablePair<PImage, String>> getImages() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        if (this.getImage() != null && this.getImage() != error) {
            list.add(new ImmutablePair<>(this.getImage(), this.getName()));
        }
        if (this.variation != null) {
            list.addAll(this.variation.getImages());
        }
        return list;
    }

    @Override
    public void clicked() {
        if (main.mouseButton == PConstants.LEFT) {
            if (main.getRoom() instanceof Game) {
                if (!failed()) {
                    if (main.keyPressed && main.keyCode == PConstants.SHIFT) {
                        //these conditions must be separate, or else when it reaches max, it does normal select
                        int max = Math.floorDiv(main.screenWidth - Group.groupSelectedX, Element.SIZE + Element.GAP); //determine the maximum amount of elements for multi select (based on screen size for now)
                        if (elementsSelected.size() < max) {
                            //clear normal select
                            elementSelectedA = null;
                            elementSelectedB = null;

                            elementsSelected.add(this.deepCopy(this.group, this.alpha, 0));
                            if (main.game.mode.equals("puzzle")) {
                                //can't select the same element multiple times, or else it will be duping
                                this.setDisabled(true);
                            }
                        }
                    } else {
                        //deselect
                        if (elementSelectedA == this) {
                            elementSelectedA = null;
                        } else if (elementSelectedB == this) {
                            elementSelectedB = null;
                        }
                        //select
                        else if (elementSelectedA == null) {
                            elementSelectedA = this;
                        } else if (elementSelectedB == null) {
                            elementSelectedB = this;
                        }
                    }
                }
            } else if (main.getRoom() instanceof ElementRoom) {
                main.elementRoom.setElement(this);
                main.switchRoom(main.elementRoom);
            }
        } else {
            main.elementRoom.setElement(this);
            main.switchRoom(main.elementRoom);
        }
    }

    public static void checkForMultiCombos() {
        //reset disabled
        if (Group.groupSelectedA != null) {
            for (Element element : elementsA) {
                element.setDisabled(false);
            }
        }
        if (Group.groupSelectedB != null) {
            for (Element element : elementsB) {
                element.setDisabled(false);
            }
        }

        ArrayList<String> elementsSelectedString = new ArrayList<>();
        for (Element element : elementsSelected) {
            elementsSelectedString.add(element.name);
        }

        elementsCreated.clear();
        for (Combo combo : main.comboList) {
            if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (CollectionUtils.isEqualCollection(multiCombo.getIngredients(), elementsSelectedString)) {
                    Element element = Element.getElement(multiCombo.getElement());
                    if (Objects.requireNonNull(element).variation instanceof ComboVariation) {
                        ((ComboVariation) element.variation).setCurrentImage(multiCombo);
                    }
                    for (int i = 0; i < multiCombo.getAmount(); i++) {
                        elementsCreated.add(element.deepCopy());
                    }
                    main.game.getHistory().add(multiCombo);
                }
            }
        }

        for (RandomCombo randomCombo : main.randomCombos) {
            MultiCombo multiCombo = randomCombo.canCreate(elementsSelectedString);
            if (multiCombo != null) {
                ArrayList<Element> randomElements = randomCombo.getElements();
                elementsCreated.addAll(randomElements);
                for (Element element : randomElements) {
                    if (element.variation instanceof ComboVariation) {
                        ((ComboVariation) element.variation).setCurrentImage(multiCombo);
                    }
                    main.game.getHistory().add(new MultiCombo(element.name, multiCombo.getIngredients()));
                }
            }
        }

        if (elementsCreated.size() > 0) {
            for (Element element : elementsCreated) {
                main.game.addElement(element);
            }

            if (main.game.mode.equals("puzzle")) {
                for (Element element : elementsSelected) {
                    if (!element.persistent) {
                        main.game.removeElement(element.name);
                    }
                }
            }

            //need to update because affected groups might be already selected
            updateA();
            updateB();

            main.game.success();
            elementsSelected.clear();
        } else {
            time = main.millis();
        }
    }

    public static void checkForCombos() {
        if (elementSelectedA != null && elementSelectedB != null) {
            //check for combos
            elementsCreated.clear();
            for (Combo combo : main.comboList) {
                if (combo instanceof NormalCombo) {
                    NormalCombo normalCombo = (NormalCombo) combo;
                    if ((normalCombo.getA().equals(elementSelectedA.name) && normalCombo.getB().equals(elementSelectedB.name)) || (normalCombo.getA().equals(elementSelectedB.name) && normalCombo.getB().equals(elementSelectedA.name))) {
                        Element element = Element.getElement(normalCombo.getElement());
                        if (Objects.requireNonNull(element).variation instanceof ComboVariation) {
                            ((ComboVariation) element.variation).setCurrentImage(normalCombo);
                        }
                        for (int i = 0; i < combo.getAmount(); i++) {
                            elementsCreated.add(element.deepCopy());
                        }
                        main.game.getHistory().add(normalCombo);
                    }
                }
            }

            for (RandomCombo randomCombo : main.randomCombos) {
                NormalCombo normalCombo = randomCombo.canCreate(elementSelectedA, elementSelectedB);
                if (normalCombo != null) {
                    ArrayList<Element> randomElements = randomCombo.getElements();
                    elementsCreated.addAll(randomElements);
                    for (Element element : randomElements) {
                        if (element.variation instanceof ComboVariation) {
                            ((ComboVariation) element.variation).setCurrentImage(normalCombo);
                        }
                        main.game.getHistory().add(new NormalCombo(element.name, normalCombo.getA(), normalCombo.getB()));
                    }
                }
            }

            if (elementsCreated.size() > 0) {

                for (Element element : elementsCreated) {
                    //element adding conditions has been refactored into the method
                    main.game.addElement(element);
                }

                if (main.game.mode.equals("puzzle")) {
                    if (!elementSelectedA.persistent) {
                        main.game.removeElement(elementSelectedA.name);
                    }
                    if (!elementSelectedB.persistent) {
                        main.game.removeElement(elementSelectedB.name);
                    }
                }

                //need to update because affected groups might be already selected
                updateA();
                updateB();

                main.game.success();
                elementSelectedA = null;
                elementSelectedB = null;
            } else {
                time = main.millis();
            }
        }
    }

    public Group getGroup() {
        return this.group;
    }

    public String getDescription() {
        return this.description;
    }

    public static ArrayList<Element> getElementsSelected() {
        return elementsSelected;
    }

    public Pack getPack() {
        return this.pack;
    }

    private void updateAlpha() {
        if (this.alphaChange > 0) { //fade in
            if (this.alpha < 255) {
                this.alpha += this.alphaChange;
            }
            if (this.alpha > 255) {
                this.alpha = 255;
            }
        } else if (this.alphaChange < 0) { //fade out
            if (this.alpha > 0) {
                this.alpha += this.alphaChange;
            }
            if (this.alpha < 0) { //completely invisible
                this.alpha = 0;
            }
        }
        main.tint(255, this.alpha);
    }

    public static void updateA() {
        elementsA.clear();
        //group might be gone if we are in puzzle mode
        if (Group.groupSelectedA != null && Group.groupSelectedA.exists()) {
            for (Element element : main.game.getDiscovered().get(Group.groupSelectedA)) {
                elementsA.add(element.deepCopy(Group.groupSelectedA, 255, 0));
            }
        } else {
            Group.groupSelectedA = null;
        }
    }

    static void resetA() {
        pageNumberA = 0;
        elementsA.clear();
        for (int i = 0; i < main.game.getDiscovered().get(Group.groupSelectedA).size(); i++) {
            Element element = main.game.getDiscovered().get(Group.groupSelectedA).get(i);
            if (i < maxElements) {
                elementsA.add(element.deepCopy(Group.groupSelectedA, 0, ALPHA_CHANGE));
            } else {
                //elements that are not on the first page don't need to fade in
                elementsA.add(element.deepCopy(Group.groupSelectedA, 255, 0));
            }
        }
        totalPagesA = (int) Math.ceil((float) main.game.getDiscovered().get(Group.groupSelectedA).size() / maxElements);
    }

    public static void updateB() {
        elementsB.clear();
        //group might be gone if we are in puzzle mode
        if (Group.groupSelectedB != null && Group.groupSelectedB.exists()) {
            for (Element element : main.game.getDiscovered().get(Group.groupSelectedB)) {
                elementsB.add(element.deepCopy(Group.groupSelectedB, 255, 0));
            }
        } else {
            Group.groupSelectedB = null;
        }
    }

    static void resetB() {
        pageNumberB = 0;
        elementsB.clear();
        for (int i = 0; i < main.game.getDiscovered().get(Group.groupSelectedB).size(); i++) {
            Element element = main.game.getDiscovered().get(Group.groupSelectedB).get(i);
            if (i < maxElements) {
                elementsB.add(element.deepCopy(Group.groupSelectedB, 0, ALPHA_CHANGE));
            } else {
                //elements that are not on the first page don't need to fade in
                elementsB.add(element.deepCopy(Group.groupSelectedB, 255, 0));
            }
        }
        totalPagesB = (int) Math.ceil((float) main.game.getDiscovered().get(Group.groupSelectedB).size() / maxElements);
    }

    static void hidePagesA() {
        //this is necessary because the first element clicked can be from group B
        if (elementSelectedA != null && elementSelectedA.group == Group.groupSelectedA) {
            elementSelectedA = null;
        } else if (elementSelectedB != null && elementSelectedB.group == Group.groupSelectedA) {
            elementSelectedB = null;
        }
        for (Element element : elementsA) {
            element.alphaChange = -ALPHA_CHANGE;
        }
    }

    static void hidePagesB() {
        if (elementSelectedA != null && elementSelectedA.group == Group.groupSelectedB) {
            elementSelectedA = null;
        } else if (elementSelectedB != null && elementSelectedB.group == Group.groupSelectedB) {
            elementSelectedB = null;
        }
        for (Element element : elementsB) {
            element.alphaChange = -ALPHA_CHANGE;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private Element deepCopy(Group group, int alpha, int alphaChange) {
        Element element = this.deepCopy();
        element.group = group;
        element.alpha = alpha;
        element.alphaChange = alphaChange;
        return element;
    }

    public Element deepCopy() {
        Element element = new Element(this);
        element.setX(this.getX());
        element.setY(this.getY());
        if (this.variation != null) {
            PImage image = this.variation.getImage(); //need to only call this once because Random Variation will give different images
            if (image != null) {
                element.setImage(image);
            } else {
                element.setImage(this.getImage());
            }
        } else {
            element.setImage(this.getImage());
        }
        return element;
    }

    //this only returns visible elements (i.e. those on the current page)
    public static ArrayList<Element> getElementsA() {
        //after screen resizing, it's possible that page number changes
        if (pageNumberA >= totalPagesA) {
            pageNumberA = totalPagesA - 1;
        }
        ArrayList<Element> elements = new ArrayList<>();
        for (int i = pageNumberA * maxElements; i < (pageNumberA + 1) * maxElements; i++) {
            if (i < elementsA.size()) {
                elements.add(elementsA.get(i));
            }
        }
        return elements;
    }

    public static ArrayList<Element> getElementsB() {
        //after screen resizing, it's possible that page number changes
        if (pageNumberB >= totalPagesB) {
            pageNumberB = totalPagesB - 1;
        }
        ArrayList<Element> elements = new ArrayList<>();
        for (int i = pageNumberB * maxElements; i < (pageNumberB + 1) * maxElements; i++) {
            if (i < elementsB.size()) {
                elements.add(elementsB.get(i));
            }
        }
        return elements;
    }
}
