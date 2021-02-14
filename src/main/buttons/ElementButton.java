package main.buttons;

import com.sun.istack.internal.Nullable;
import main.Language;
import main.combos.Combo;
import main.combos.MultiCombo;
import main.combos.NormalCombo;
import main.combos.RandomCombo;
import main.rooms.*;
import main.variations.ComboVariation;
import main.variations.RandomVariation;
import main.variations.Variation;
import main.variations.appearances.Appearance;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ElementButton extends Button implements Comparable<ElementButton> {

    public static final int SIZE = 64;
    public static final int HEIGHT = SIZE + 30;
    private static final int GAP = 30;
    private static final int ALPHA_CHANGE = 10;
    private static final int FAILED_TIME = 500;

    private static int maxElements;

    private static final ArrayList<ElementButton> elementsA = new ArrayList<>();
    private static final ArrayList<ElementButton> elementsB = new ArrayList<>();

    private static ElementButton elementSelectedA;
    private static ElementButton elementSelectedB;
    private static final ArrayList<ElementButton> elementsSelected = new ArrayList<>(); //for selecting more than 2 elements

    public static int pageNumberA;
    public static int totalPagesA;
    public static int pageNumberB;
    public static int totalPagesB;

    private static final ArrayList<ElementButton> elementsCreated = new ArrayList<>();

    public static ElementButton touching;

    private static long time = -1; //timer when combination is wrong

    private final String name;
    private Group group;
    private ArrayList<String> tags = new ArrayList<>();
    private String description;
    private final Pack pack;
    private boolean persistent;
    private Variation variation;
    private Appearance randomAppearance;

    private int alpha = 255;
    private int alphaChange;

    public ElementButton(String name, Group group, Pack pack) {
        super(SIZE, HEIGHT);

        this.name = name;
        this.group = group;
        this.pack = pack;
        this.tintOverlay = false;
    }

    //copy constructor
    public ElementButton(ElementButton other) {
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
        this.randomAppearance = other.randomAppearance;
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
    public int compareTo(ElementButton o) {
        return this.name.compareTo(o.name);
    }

    //file name without extension
    public PImage getImage(String fileName) {
        //check if a pack has the image, from top to bottom
        for (Pack pack : PacksRoom.INSTANCE.getLoadedPacks()) {
            //check for atlas first
            if (pack.getAtlasImage(fileName) != null) {
                return pack.getAtlasImage(fileName);
            }
            //fileName could be in the form of pack:element:variation because of variations
            String id = StringUtils.countMatches(fileName, ":") == 2 ? fileName.split(":")[2] : fileName;
            if (pack.getName().equals("Alchemy") && this.pack.getName().equals("Alchemy")) {
                //if the element is of the default pack and we are in the default pack right now, load default location
                String defaultPath = "resources/elements/alchemy/" + this.group.getID() + "/" + id;
                PImage image = this.getImageFromPath(defaultPath);
                if (image == null) {
                    image = error;
                } else {
                    image.resize(SIZE, 0);
                }
                return image;
            } else {
                String packPath = pack.getPath() + "/elements/" + this.group.getPack().getNamespace() + "/" + this.group.getID() + "/" + id;
                PImage image = this.getImageFromPath(packPath);
                if (image != null) {
                    image.resize(SIZE, 0);
                    return image;
                }
            }
        }
        return null;
    }

    private PImage getImageFromPath(String path) {
        String png = path + ".png";
        if (new File(png).exists()) {
            return main.loadImage(png);
        }
        //https://stackoverflow.com/questions/54443002/java-splitting-gif-image-in-bufferedimages-gives-malformed-images
        //TODO: gif -> animated frames -> AnimationVariation
        //problem is AnimationVariation itself also calls this method lmao, recursive animation?? lol
        String gif = path + ".gif";
        if (new File(gif).exists()) {
            return main.loadImage(gif);
        }
        return null;
    }

    public PImage getImageWithoutFallback(String fileName) {
        PImage image = this.getImage(fileName);
        if (image == Button.error) {
            return null;
        } else {
            return image;
        }
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

    public static void loadImage(ArrayList<ElementButton> elements) {
        Thread thread = new Thread(() -> {
            for (ElementButton element : elements) {
                //load original image
                if (element.getImage() == null) {
                    element.setImage(element.getImage(element.getID()));
                }

                if (element.variation != null) {
                    element.variation.loadImages();
                }

                Loading.INSTANCE.updateProgress();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    boolean isInPack(Pack pack) {
        String prefix = pack.getNamespace() + ":";
        return this.name.length() >= prefix.length() && this.name.startsWith(prefix);
    }

    @Nullable
    public static ElementButton getElement(String name) {
        if (!main.elements.containsKey(name)) {
            return null;
        }
        if (!main.groups.containsKey(main.elements.get(name))) {
            System.err.println(main.elements.get(name).getName() + " group not found!");
            return null;
        }
        for (ElementButton element : main.groups.get(main.elements.get(name))) {
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
        ArrayList<ElementButton> elements;
        if (Group.groupSelectedA != null) {
            totalPagesA = (int) Math.ceil((float) Game.INSTANCE.getDiscovered().get(Group.groupSelectedA).size() / maxElements);
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
            totalPagesB = (int) Math.ceil((float) Game.INSTANCE.getDiscovered().get(Group.groupSelectedB).size() / maxElements);
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
        for (ElementButton element : elementsSelected) {
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
        for (ElementButton element : elementsCreated) {
            element.draw(x, main.screenHeight / 2F - SIZE / 2F);
            x += SIZE + GAP;
        }
        drawTooltip();
    }

    public static void drawHintElement(ElementButton element) {
        touching = null;
        element.draw(main.screenWidth / 2F - ElementButton.SIZE / 2F, main.screenHeight / 2F - ElementButton.SIZE / 2F);
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
                variationName = Language.getLanguageSelected().getElementLocalizedString(this.getNamespace(), this.randomAppearance.getName());
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

    public PImage getDrawnImage() {
        PImage image = null;
        if (this.variation != null) {
            image = this.variation instanceof RandomVariation ? this.randomAppearance.getImage() : this.variation.getImage();
        }
        //random appearance might return null
        if (image == null) {
            image = this.getImage();
        }
        return image;
    }

    @Override
    protected void drawButton() {
        main.image(this.getDrawnImage(), this.getX(), this.getY());
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
            list.addAll(this.variation.getPairs());
        }
        return list;
    }

    private boolean isDepleted(List<ElementButton> selected) {
        int count = 0;
        for (ElementButton element : Game.INSTANCE.getDiscovered().get(this.group)) {
            if (element.getName().equals(this.getName())) {
                count++;
            }
        }
        for (ElementButton element : selected) {
            if (element != null && element.getName().equals(this.getName())) {
                count--;
            }
        }
        return count <= 0;
    }

    @Override
    public void clicked() {
        if (main.mouseButton == PConstants.LEFT) {
            if (main.getRoom() instanceof Game) {
                if (!failed()) {
                    if (main.keyPressed && main.keyCode == PConstants.SHIFT) {
                        //these conditions must be separate, or else when it reaches max, it does normal select
                        int max = Math.floorDiv(main.screenWidth - Group.groupSelectedX, ElementButton.SIZE + ElementButton.GAP); //determine the maximum amount of elements for multi select (based on screen size for now)
                        if (elementsSelected.size() < max) {
                            //clear normal select
                            elementSelectedA = null;
                            elementSelectedB = null;

                            elementsSelected.add(this.deepCopy(this.group, this.alpha, 0));
                            if (Game.mode == GameMode.PUZZLE && this.isDepleted(elementsSelected)) {
                                //can't select the same element multiple times, or else it will be duping
                                for (ElementButton element : Game.INSTANCE.getDiscovered().get(this.group)) {
                                    if (element.getName().equals(this.getName())) {
                                        this.setDisabled(true);
                                    }
                                }
                            }
                        }
                    } else {
                        //in puzzle mode, if you open the same group twice, you cannot select both of the same element twice
                        //this shouldn't be allowed because it would be duping
                        //we also need to check if we have already selected it because we still need to allow deselecting
                        if (Game.mode == GameMode.PUZZLE && elementSelectedA != this && elementSelectedB != this && this.isDepleted(Arrays.asList(elementSelectedA, elementSelectedB))) {
                            return;
                        }
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
                ElementRoom.INSTANCE.setElement(this);
                main.switchRoom(ElementRoom.INSTANCE);
            }
        } else {
            ElementRoom.INSTANCE.setElement(this);
            main.switchRoom(ElementRoom.INSTANCE);
        }
    }

    public static void checkForMultiCombos() {
        //reset disabled
        if (Group.groupSelectedA != null) {
            for (ElementButton element : elementsA) {
                element.setDisabled(false);
            }
        }
        if (Group.groupSelectedB != null) {
            for (ElementButton element : elementsB) {
                element.setDisabled(false);
            }
        }

        ArrayList<String> elementsSelectedString = new ArrayList<>();
        for (ElementButton element : elementsSelected) {
            elementsSelectedString.add(element.name);
        }

        elementsCreated.clear();
        for (Combo combo : main.comboList) {
            if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (CollectionUtils.isEqualCollection(multiCombo.getIngredients(), elementsSelectedString)) {
                    ElementButton element = ElementButton.getElement(multiCombo.getElement());
                    if (Objects.requireNonNull(element).variation instanceof ComboVariation) {
                        ((ComboVariation) element.variation).setCurrentImage(multiCombo);
                    }
                    for (int i = 0; i < multiCombo.getAmount(); i++) {
                        elementsCreated.add(element.deepCopy());
                    }
                    Game.INSTANCE.getHistory().add(multiCombo);
                }
            }
        }

        for (RandomCombo randomCombo : main.randomCombos) {
            MultiCombo multiCombo = randomCombo.canCreate(elementsSelectedString);
            if (multiCombo != null) {
                ArrayList<ElementButton> randomElements = randomCombo.getElements();
                elementsCreated.addAll(randomElements);
                for (ElementButton element : randomElements) {
                    if (element.variation instanceof ComboVariation) {
                        ((ComboVariation) element.variation).setCurrentImage(multiCombo);
                    }
                    Game.INSTANCE.getHistory().add(new MultiCombo(element.name, multiCombo.getIngredients()));
                }
            }
        }

        if (elementsCreated.size() > 0) {
            for (ElementButton element : elementsCreated) {
                Game.INSTANCE.addElement(element);
            }

            if (Game.mode == GameMode.PUZZLE) {
                for (ElementButton element : elementsSelected) {
                    if (!element.persistent) {
                        Game.INSTANCE.removeElement(element.name);
                    }
                }
            }

            //need to update because affected groups might be already selected
            updateA();
            updateB();

            Game.INSTANCE.success();
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
                        ElementButton element = ElementButton.getElement(normalCombo.getElement());
                        if (Objects.requireNonNull(element).variation instanceof ComboVariation) {
                            ((ComboVariation) element.variation).setCurrentImage(normalCombo);
                        }
                        for (int i = 0; i < combo.getAmount(); i++) {
                            elementsCreated.add(element.deepCopy());
                        }
                        Game.INSTANCE.getHistory().add(normalCombo);
                    }
                }
            }

            for (RandomCombo randomCombo : main.randomCombos) {
                NormalCombo normalCombo = randomCombo.canCreate(elementSelectedA, elementSelectedB);
                if (normalCombo != null) {
                    ArrayList<ElementButton> randomElements = randomCombo.getElements();
                    elementsCreated.addAll(randomElements);
                    for (ElementButton element : randomElements) {
                        if (element.variation instanceof ComboVariation) {
                            ((ComboVariation) element.variation).setCurrentImage(normalCombo);
                        }
                        Game.INSTANCE.getHistory().add(new NormalCombo(element.name, normalCombo.getA(), normalCombo.getB()));
                    }
                }
            }

            if (elementsCreated.size() > 0) {

                for (ElementButton element : elementsCreated) {
                    //element adding conditions has been refactored into the method
                    Game.INSTANCE.addElement(element);
                }

                if (Game.mode == GameMode.PUZZLE) {
                    if (!elementSelectedA.persistent) {
                        Game.INSTANCE.removeElement(elementSelectedA.name);
                    }
                    if (!elementSelectedB.persistent) {
                        Game.INSTANCE.removeElement(elementSelectedB.name);
                    }
                }

                //need to update because affected groups might be already selected
                updateA();
                updateB();

                Game.INSTANCE.success();
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

    public static ArrayList<ElementButton> getElementsSelected() {
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
            for (ElementButton element : Game.INSTANCE.getDiscovered().get(Group.groupSelectedA)) {
                elementsA.add(element.deepCopy(Group.groupSelectedA, 255, 0));
            }
        } else {
            Group.groupSelectedA = null;
        }
    }

    static void resetA() {
        pageNumberA = 0;
        elementsA.clear();
        for (int i = 0; i < Game.INSTANCE.getDiscovered().get(Group.groupSelectedA).size(); i++) {
            ElementButton element = Game.INSTANCE.getDiscovered().get(Group.groupSelectedA).get(i);
            if (i < maxElements) {
                elementsA.add(element.deepCopy(Group.groupSelectedA, 0, ALPHA_CHANGE));
            } else {
                //elements that are not on the first page don't need to fade in
                elementsA.add(element.deepCopy(Group.groupSelectedA, 255, 0));
            }
        }
        totalPagesA = (int) Math.ceil((float) Game.INSTANCE.getDiscovered().get(Group.groupSelectedA).size() / maxElements);
    }

    public static void updateB() {
        elementsB.clear();
        //group might be gone if we are in puzzle mode
        if (Group.groupSelectedB != null && Group.groupSelectedB.exists()) {
            for (ElementButton element : Game.INSTANCE.getDiscovered().get(Group.groupSelectedB)) {
                elementsB.add(element.deepCopy(Group.groupSelectedB, 255, 0));
            }
        } else {
            Group.groupSelectedB = null;
        }
    }

    static void resetB() {
        pageNumberB = 0;
        elementsB.clear();
        for (int i = 0; i < Game.INSTANCE.getDiscovered().get(Group.groupSelectedB).size(); i++) {
            ElementButton element = Game.INSTANCE.getDiscovered().get(Group.groupSelectedB).get(i);
            if (i < maxElements) {
                elementsB.add(element.deepCopy(Group.groupSelectedB, 0, ALPHA_CHANGE));
            } else {
                //elements that are not on the first page don't need to fade in
                elementsB.add(element.deepCopy(Group.groupSelectedB, 255, 0));
            }
        }
        totalPagesB = (int) Math.ceil((float) Game.INSTANCE.getDiscovered().get(Group.groupSelectedB).size() / maxElements);
    }

    static void hidePagesA() {
        //this is necessary because the first element clicked can be from group B
        if (elementSelectedA != null && elementSelectedA.group == Group.groupSelectedA) {
            elementSelectedA = null;
        } else if (elementSelectedB != null && elementSelectedB.group == Group.groupSelectedA) {
            elementSelectedB = null;
        }
        for (ElementButton element : elementsA) {
            element.alphaChange = -ALPHA_CHANGE;
        }
    }

    static void hidePagesB() {
        if (elementSelectedA != null && elementSelectedA.group == Group.groupSelectedB) {
            elementSelectedA = null;
        } else if (elementSelectedB != null && elementSelectedB.group == Group.groupSelectedB) {
            elementSelectedB = null;
        }
        for (ElementButton element : elementsB) {
            element.alphaChange = -ALPHA_CHANGE;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private ElementButton deepCopy(Group group, int alpha, int alphaChange) {
        ElementButton element = this.deepCopy();
        element.group = group;
        element.alpha = alpha;
        element.alphaChange = alphaChange;
        return element;
    }

    public ElementButton deepCopy() {
        ElementButton element = new ElementButton(this);
        element.setX(this.getX());
        element.setY(this.getY());
        if (this.variation instanceof RandomVariation) {
            element.randomAppearance = this.variation.getAppearance();
        }
        element.setImage(this.getImage());
        return element;
    }

    //this only returns visible elements (i.e. those on the current page)
    public static ArrayList<ElementButton> getElementsA() {
        //after screen resizing, it's possible that page number changes
        if (pageNumberA >= totalPagesA) {
            pageNumberA = totalPagesA - 1;
        }
        ArrayList<ElementButton> elements = new ArrayList<>();
        for (int i = pageNumberA * maxElements; i < (pageNumberA + 1) * maxElements; i++) {
            if (i < elementsA.size()) {
                elements.add(elementsA.get(i));
            }
        }
        return elements;
    }

    public static ArrayList<ElementButton> getElementsB() {
        //after screen resizing, it's possible that page number changes
        if (pageNumberB >= totalPagesB) {
            pageNumberB = totalPagesB - 1;
        }
        ArrayList<ElementButton> elements = new ArrayList<>();
        for (int i = pageNumberB * maxElements; i < (pageNumberB + 1) * maxElements; i++) {
            if (i < elementsB.size()) {
                elements.add(elementsB.get(i));
            }
        }
        return elements;
    }
}
