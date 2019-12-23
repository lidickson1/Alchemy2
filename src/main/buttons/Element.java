package main.buttons;

import com.sun.istack.internal.Nullable;
import main.*;
import main.rooms.ElementRoom;
import main.rooms.Game;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import processing.core.PConstants;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;

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

    private int alpha = 255;
    private int alphaChange;

    private Element(String name, Group group, Pack pack) {
        super(SIZE, HEIGHT);

        this.name = name;
        this.group = group;
        this.pack = pack;
        this.tintOverlay = false;
    }

    //copy constructor
    private Element(String name, Group group, Pack pack, ArrayList<String> tags, String description, int alpha, int alphaChange) {
        super(SIZE, HEIGHT);

        this.name = name;
        this.group = group;
        this.pack = pack;
        this.tags = tags;
        this.description = description;
        this.alpha = alpha;
        this.alphaChange = alphaChange;
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

    public static void loadElements(JSONArray array, Pack pack) {
        ArrayList<ImmutableTriple<String, Permutation, JSONObject>> permutations = new ArrayList<>();
        ArrayList<ImmutableTriple<RandomCombo, Permutation, JSONObject>> randomPermutations = new ArrayList<>();
        ArrayList<ImmutableTriple<String, Permutation, JSONObject>> removes = new ArrayList<>();
        @SuppressWarnings("SpellCheckingInspection") ArrayList<ImmutablePair<String, JSONArray>> removeMultis = new ArrayList<>(); //remove combos with multiple ingredients

        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            String element = pack.getNamespacedName(object.getString("name"));

            if (object.hasKey("remove")) {
                String remove = object.getString("remove");
                //noinspection IfCanBeSwitch
                if (remove.equals("all")) {
                    main.comboList.clear();
                    main.randomCombos.clear();
                    main.loading.removeAllElements(); //this needs to be called first or else we can't determine how much progress to remove
                    for (HashSet<Element> list : main.groups.values()) {
                        list.clear();
                    }
                    main.elements.clear();
                } else if (remove.equals("element")) {
                    //remove all combos of an element
                    String elementName = pack.getNamespacedName(object.getString("element"));
                    main.comboList.removeIf(e -> e.getElement().equals(elementName));
                    for (RandomCombo randomCombo : main.randomCombos) {
                        randomCombo.removeElement(elementName);
                    }
                    Element element1 = Element.getElement(elementName);
                    if (element1 != null) {
                        main.groups.get(element1.group).remove(element1);
                        main.elements.remove(elementName);
                        main.loading.removeElement();
                    } else {
                        System.err.println(elementName + " could not be removed!");
                    }
                } else if (remove.equals("combo")) {
                    JSONArray combos = object.getJSONArray("combos");
                    for (int j = 0; j < combos.size(); j++) {
                        JSONObject combo = combos.getJSONObject(j);
                        if (combo.hasKey("first element")) {
                            removeCombo(element, pack.getNamespacedName(combo.getString("first element")), pack.getNamespacedName(combo.getString("second element")));
                        } else if (combo.hasKey("elements")) {
                            if (combo.hasKey("paired") && combo.getBoolean("paired")) {
                                removes.add(new ImmutableTriple<>(element, Permutation.UNRESTRICTED, combo));
                            } else {
                                removeMultis.add(new ImmutablePair<>(element, combo.getJSONArray("elements")));
                            }
                        } else if (combo.hasKey("first elements")) {
                            removes.add(new ImmutableTriple<>(element, Permutation.RESTRICTED, combo));
                        } else {
                            removes.add(new ImmutableTriple<>(element, Permutation.SETS, combo));
                        }
                    }
                    main.loading.removeCombo();
                }
                //TODO: remove random
            } else if (object.hasKey("combo") && object.hasKey("result")) {
                //random
                RandomCombo randomCombo = new RandomCombo(object.getJSONArray("result"));
                JSONObject combo = object.getJSONObject("combo");
                if (combo.hasKey("first element")) {
                    NormalCombo combo1 = new NormalCombo(element, pack.getNamespacedName(combo.getString("first element")), pack.getNamespacedName(combo.getString("second element")));
                    if (combo.hasKey("amount")) {
                        combo1.setAmount(combo.getInt("amount"));
                    }
                    randomCombo.addCombo(combo1);
                } else if (combo.hasKey("elements")) {
                    if (combo.hasKey("paired") && combo.getBoolean("paired")) {
                        randomPermutations.add(new ImmutableTriple<>(randomCombo, Permutation.UNRESTRICTED, combo));
                    } else {
                        JSONArray elements = combo.getJSONArray("elements");
                        ArrayList<String> list = processTags(elements, pack);
                        MultiCombo multiCombo = new MultiCombo(element, list);
                        int count = combo.hasKey("amount") ? combo.getInt("amount") : 1;
                        multiCombo.setAmount(count);
                        randomCombo.addCombo(multiCombo);
                    }
                } else if (combo.hasKey("first elements")) {
                    randomPermutations.add(new ImmutableTriple<>(randomCombo, Permutation.RESTRICTED, combo));
                } else {
                    randomPermutations.add(new ImmutableTriple<>(randomCombo, Permutation.SETS, combo));
                }
                main.randomCombos.add(randomCombo);
                main.loading.randomCombo();
            } else {
                Group group = Group.getGroup(pack.getNamespacedName(object.getString("group")));
                Element e = new Element(element, group, pack);
                if (group == null) {
                    System.err.println("Error: Group " + pack.getNamespacedName(object.getString("group")) + " not found!");
                    main.loading.elementFailed();
                    continue;
                }

                //element might exist because we allow existing elements to be modified
                if (main.elements.containsKey(element)) {
                    e = Element.getElement(element);
                    assert e != null;
                    main.loading.modifyElement();
                } else {
                    main.groups.get(group).add(e);
                    main.elements.put(element, group);
                }

                if (object.hasKey("description")) {
                    e.description = object.getString("description");
                }

                if (object.hasKey("tags")) {
                    JSONArray tags = object.getJSONArray("tags");
                    for (int j = 0; j < tags.size(); j++) {
                        //tags are namespaced too!
                        e.tags.add(pack.getNamespacedName(tags.getString(j)));
                    }
                }

                if (object.hasKey("combos")) {
                    JSONArray combos = object.getJSONArray("combos");
                    for (int j = 0; j < combos.size(); j++) {
                        JSONObject combo = combos.getJSONObject(j);
                        if (combo.hasKey("first element")) {
                            NormalCombo combo1 = new NormalCombo(element, pack.getNamespacedName(combo.getString("first element")), pack.getNamespacedName(combo.getString("second element")));
                            if (combo.hasKey("amount")) {
                                combo1.setAmount(combo.getInt("amount"));
                            }
                            main.comboList.add(combo1);
                        } else if (combo.hasKey("elements")) {
                            if (combo.hasKey("paired") && combo.getBoolean("paired")) {
                                permutations.add(new ImmutableTriple<>(element, Permutation.UNRESTRICTED, combo));
                            } else {
                                JSONArray elements = combo.getJSONArray("elements");
                                ArrayList<String> list = processTags(elements, pack);
                                MultiCombo multiCombo = new MultiCombo(element, list);
                                int count = combo.hasKey("amount") ? combo.getInt("amount") : 1;
                                multiCombo.setAmount(count);
                                main.comboList.add(multiCombo);
                            }
                        } else if (combo.hasKey("first elements")) {
                            permutations.add(new ImmutableTriple<>(element, Permutation.RESTRICTED, combo));
                        } else {
                            permutations.add(new ImmutableTriple<>(element, Permutation.SETS, combo));
                        }
                    }
                }
            }

            main.loading.updateProgress();
        }

        //this is done separately because all elements must be loaded first for tag: and group: to work
        for (ImmutableTriple<String, Permutation, JSONObject> triple : permutations) {
            JSONObject combo = triple.right;
            ArrayList<ImmutablePair<String, String>> combos = processPermutations(triple.middle, combo, pack);
            for (ImmutablePair<String, String> pair : combos) {
                NormalCombo combo1 = new NormalCombo(triple.left, pair.left, pair.right);
                if (combo.hasKey("amount")) {
                    combo1.setAmount(combo.getInt("amount"));
                }
                main.comboList.add(combo1);
            }
        }

        for (ImmutableTriple<RandomCombo, Permutation, JSONObject> triple : randomPermutations) {
            JSONObject combo = triple.right;
            ArrayList<ImmutablePair<String, String>> combos = processPermutations(triple.middle, combo, pack);
            for (ImmutablePair<String, String> pair : combos) {
                NormalCombo combo1 = new NormalCombo(null, pair.left, pair.right);
                if (combo.hasKey("amount")) {
                    combo1.setAmount(combo.getInt("amount"));
                }
                triple.left.addCombo(combo1);
            }
        }

        for (ImmutableTriple<String, Permutation, JSONObject> triple : removes) {
            String element = triple.left;
            JSONObject combo = triple.right;
            ArrayList<ImmutablePair<String, String>> combos = processPermutations(triple.middle, combo, pack);
            for (ImmutablePair<String, String> pair : combos) {
                removeCombo(element, pair.left, pair.right);
            }
        }

        for (ImmutablePair<String, JSONArray> pair : removeMultis) {
            ArrayList<String> list = processTags(pair.right, pack);
            list.sort(String::compareTo);
            main.comboList.removeIf(e -> (e instanceof MultiCombo) && CollectionUtils.isEqualCollection(list, e.getIngredients()));
        }

        validateCombos();

        //output combos to file
//        JSONArray comboArray = new JSONArray();
//        for (ImmutableTriple<String, String, String> triple : main.comboList) {
//            JSONObject object = new JSONObject();
//            object.setString("a", triple.left);
//            object.setString("b", triple.middle);
//            object.setString("c", triple.right);
//            comboArray.append(object);
//        }
//        main.saveJSONArray(comboArray, "combos.json");
    }

    //TODO: validate MultiCombo
    private static void validateCombos() {
        for (Combo combo : main.comboList) {
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                if (!main.elements.containsKey(normalCombo.getA())) {
                    System.err.println("Error with combo: " + normalCombo.getA() + " doesn't exist!");
                }
                if (!main.elements.containsKey(normalCombo.getB())) {
                    System.err.println("Error with combo: " + normalCombo.getB() + " doesn't exist!");
                }
            }
        }
    }

    private static void removeCombo(String element, String a, String b) {
        main.comboList.removeIf((e) -> {
            if (!(e instanceof NormalCombo)) {
                return false;
            }

            NormalCombo normalCombo = (NormalCombo) e;
            return (normalCombo.getA().equals(a) && normalCombo.getB().equals(b)) || (normalCombo.getA().equals(b) && normalCombo.getB().equals(a)) && normalCombo.getElement().equals(element);
        });
    }

    private enum Permutation {
        UNRESTRICTED,
        RESTRICTED,
        SETS
    }

    private static ArrayList<ImmutablePair<String, String>> processPermutations(Permutation permutation, JSONObject combo, Pack pack) {
        ArrayList<ImmutablePair<String, String>> combos = new ArrayList<>();
        if (permutation == Permutation.UNRESTRICTED) {
            JSONArray elementsArray = combo.getJSONArray("elements");
            ArrayList<String> elements = processTags(elementsArray, pack);
            for (String a : elements) {
                for (String b : elements) {
                    if (!containsPair(a, b, combos) && !containsPair(b, a, combos) && !a.equals(b)) {
                        combos.add(new ImmutablePair<>(a, b));
                    }
                }
            }
        } else if (permutation == Permutation.RESTRICTED) {
            JSONArray firstArray = combo.getJSONArray("first elements");
            JSONArray secondArray = combo.getJSONArray("second elements");
            ArrayList<String> firstElements = processTags(firstArray, pack);
            ArrayList<String> secondElements = processTags(secondArray, pack);
            for (String a : firstElements) {
                for (String b : secondElements) {
                    if (!containsPair(a, b, combos)) {
                        combos.add(new ImmutablePair<>(a, b));
                    }
                }
            }
        } else if (permutation == Permutation.SETS) {
            JSONArray firstArray = combo.getJSONArray("first set");
            JSONArray secondArray = combo.getJSONArray("second set");
            ArrayList<String> firstElements = processTags(firstArray, pack);
            ArrayList<String> secondElements = processTags(secondArray, pack);
            for (int i = 0; i < firstElements.size(); i++) {
                String a = firstElements.get(i);
                String b = secondElements.get(i);
                if (!containsPair(a, b, combos)) {
                    combos.add(new ImmutablePair<>(a, b));
                }
            }
        }
        return combos;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean containsPair(String a, String b, ArrayList<ImmutablePair<String, String>> list) {
        for (ImmutablePair p : list) {
            if (p.left.equals(a) && p.right.equals(b)) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<String> processTags(JSONArray array, Pack pack) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(pack.getNamespacedName(array.getString(i)));
        }
        ListIterator<String> iterator = list.listIterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            if (string.contains("tag:")) {
                iterator.remove();
                String tag = string.replace("tag:", "");
                for (Group group : main.groups.keySet()) {
                    for (Element element : main.groups.get(group)) {
                        if (element.tags.contains(tag)) {
                            iterator.add(element.name);
                        }
                    }
                }
            } else if (string.contains("group:")) {
                iterator.remove();
                String group = string.replace("group:", "");
                for (Element element : main.groups.get(Group.getGroup(group))) {
                    iterator.add(element.name);
                }
            }
        }
        return list;
    }

    private void loadImage(String path) {
        this.setImage(main.loadImage(path));
        this.getImage().resize(SIZE, SIZE);
    }

    void loadImage(PImage image) {
        this.setImage(image);
        this.getImage().resize(SIZE, SIZE);
    }

    //making it public
    @Override
    public PImage getImage() {
        return super.getImage();
    }

    //TODO: maybe we can just use Element.group?
    public static void loadImage(ArrayList<ImmutablePair<Element, Group>> elements) {
        Thread thread = new Thread(() -> {
            for (ImmutablePair<Element, Group> pair : elements) {
                Element element = pair.left;
                Group group = pair.right;
                //check if a pack has the image, from top to bottom
                for (Pack pack : main.packsRoom.getLoadedPacks()) {
                    if (pack.getName().equals("Alchemy") && element.pack.getName().equals("Alchemy")) {
                        //if the element is of the default pack and we are in the default pack right now, load default location
                        element.loadImage("resources/elements/alchemy/" + group.getID() + "/" + element.getID() + ".png");
                        break;
                    } else {
                        String packPath = pack.getPath() + "/elements/" + group.getPack().getNamespace() + "/" + group.getID() + "/" + element.getID() + ".png";
                        if (new File(packPath).exists()) {
                            element.loadImage(packPath);
                            break;
                        }
                    }
                }

                if (element.getImage() == null) {
                    element.setImage(null);
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

    private String getDisplayName() {
        String displayName = Language.getLanguageSelected().getElementLocalizedString(this.getNamespace(), this.getID());
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
        main.image(this.getImage(), this.getX(), this.getY());
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
        ArrayList<String> elementsSelectedString = new ArrayList<>();
        for (Element element : elementsSelected) {
            elementsSelectedString.add(element.name);
        }

        elementsCreated.clear();
        for (Combo combo : main.comboList) {
            if (combo instanceof MultiCombo) {
                MultiCombo multiCombo = (MultiCombo) combo;
                if (CollectionUtils.isEqualCollection(multiCombo.getIngredients(), elementsSelectedString)) {
                    for (int i = 0; i < multiCombo.getAmount(); i++) {
                        elementsCreated.add(Element.getElement(multiCombo.getElement()));
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
                    main.game.removeElement(element.name);
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
                        for (int i = 0; i < combo.getAmount(); i++) {
                            elementsCreated.add(Element.getElement(combo.getElement()));
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
                    main.game.removeElement(elementSelectedA.name);
                    main.game.removeElement(elementSelectedB.name);
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
        Element element = new Element(this.name, group, this.pack, this.tags, this.description, alpha, alphaChange);
        element.setX(this.getX());
        element.setY(this.getY());
        element.setImage(this.getImage());
        return element;
    }

    public Element deepCopy() {
        Element element = new Element(this.name, this.group, this.pack, this.tags, this.description, this.alpha, this.alphaChange);
        element.setX(this.getX());
        element.setY(this.getY());
        element.setImage(this.getImage());
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
