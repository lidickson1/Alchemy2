package main;

import main.buttons.Element;
import main.buttons.Group;
import main.buttons.Pack;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.*;

public class LoadElements extends Entity {

    private static ArrayList<Normal> normals = new ArrayList<>();
    private static ArrayList<Permutation> permutations = new ArrayList<>();
    private static ArrayList<MultiPermutation> multiPermutations = new ArrayList<>();
    private static Pack pack;

    static class ProcessCombo {
        String element;
        JSONObject json;
        RandomCombo randomCombo;
        boolean remove;

        ProcessCombo(String element, JSONObject json, boolean remove, RandomCombo randomCombo) {
            this.element = element;
            this.json = json;
            this.remove = remove;
            this.randomCombo = randomCombo;
        }

        boolean isRandom() {
            return this.element == null && this.randomCombo != null;
        }
    }

    static class Normal extends ProcessCombo {
        String a;
        String b;

        Normal(String element, String a, String b, JSONObject json, boolean remove, RandomCombo randomCombo) {
            super(element, json, remove, randomCombo);
            this.a = a;
            this.b = b;
        }
    }

    static class Permutation extends ProcessCombo {
        PermutationType permutationType;

        Permutation(String element, PermutationType permutationType, JSONObject json, boolean remove, RandomCombo randomCombo) {
            super(element, json, remove, randomCombo);
            this.permutationType = permutationType;
        }
    }

    static class MultiPermutation extends ProcessCombo {
        MultiPermutation(String element, JSONObject json, boolean remove, RandomCombo randomCombo) {
            super(element, json, remove, randomCombo);
        }
    }

    public static void loadElements(JSONArray array, Pack p) {
        permutations.clear();
        multiPermutations.clear();
        normals.clear();
        pack = p;

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
                        main.groups.get(element1.getGroup()).remove(element1);
                        main.elements.remove(elementName);
                        main.loading.removeElement();
                    } else {
                        System.err.println(elementName + " could not be removed!");
                    }
                } else if (remove.equals("combo")) {
                    JSONArray combos = object.getJSONArray("combos");
                    for (int j = 0; j < combos.size(); j++) {
                        processCombo(combos.getJSONObject(j), element, true, null);
                    }
                    main.loading.removeCombo();
                } else if (remove.equals("random")) {
                    JSONObject combo = object.getJSONObject("combo");
                    processCombo(combo, null, true, null);
                    main.loading.removeCombo();
                }
            } else if (object.hasKey("combo") && object.hasKey("result")) {
                //random
                RandomCombo randomCombo = new RandomCombo(object.getJSONArray("result"));
                JSONObject combo = object.getJSONObject("combo");
                processCombo(combo, null, false, randomCombo);
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

                if (object.hasKey("persistent")) {
                    e.setPersistent(object.getBoolean("persistent"));
                }

                if (object.hasKey("description")) {
                    e.setDescription(object.getString("description"));
                }

                if (object.hasKey("tags")) {
                    JSONArray tags = object.getJSONArray("tags");
                    for (int j = 0; j < tags.size(); j++) {
                        //tags are namespaced too!
                        e.addTag(pack.getNamespacedName(tags.getString(j)));
                    }
                }

                if (object.hasKey("combos")) {
                    JSONArray combos = object.getJSONArray("combos");
                    for (int j = 0; j < combos.size(); j++) {
                        processCombo(combos.getJSONObject(j), element, false, null);
                    }
                }
            }

            main.loading.updateProgress();
        }

        for (Normal normal : normals) {
            if (normal.remove) {
                if (normal.isRandom()) {
                    for (RandomCombo randomCombo : main.randomCombos) {
                        randomCombo.removeCombo(normal.a, normal.b);
                    }
                } else {
                    removeCombo(normal.element, new ArrayList<>(Arrays.asList(normal.a, normal.b)));
                }
            } else {
                NormalCombo normalCombo = new NormalCombo(normal.element, normal.a, normal.b);
                int count = normal.json.hasKey("amount") ? normal.json.getInt("amount") : 1;
                normalCombo.setAmount(count);
                if (normal.isRandom()) {
                    normal.randomCombo.addCombo(normalCombo);
                } else {
                    main.comboList.add(normalCombo);
                }
            }
        }

        //this is done separately because all elements must be loaded first for tag: and group: to work
        for (Permutation permutation : permutations) {
            ArrayList<ImmutablePair<String, String>> combos = processPermutations(permutation.permutationType, permutation.json, pack);
            for (ImmutablePair<String, String> pair : combos) {
                if (permutation.remove) {
                    if (permutation.isRandom()) {
                        for (RandomCombo randomCombo : main.randomCombos) {
                            randomCombo.removeCombo(pair.left, pair.right);
                        }
                    } else {
                        removeCombo(permutation.element, new ArrayList<>(Arrays.asList(pair.left, pair.right)));
                    }
                } else {
                    NormalCombo normalCombo = new NormalCombo(permutation.element, pair.left, pair.right);
                    int count = permutation.json.hasKey("amount") ? permutation.json.getInt("amount") : 1;
                    normalCombo.setAmount(count);
                    if (permutation.isRandom()) {
                        permutation.randomCombo.addCombo(normalCombo);
                    } else {
                        main.comboList.add(normalCombo);
                    }
                }
            }
        }

        for (MultiPermutation multiPermutation : multiPermutations) {
            JSONArray elements = multiPermutation.json.getJSONArray("elements");
            ArrayList<String> ingredients = processTags(elements, pack);
            if (multiPermutation.remove) {
                if (multiPermutation.isRandom()) {
                    for (RandomCombo randomCombo : main.randomCombos) {
                        randomCombo.removeCombo(ingredients);
                    }
                } else {
                    removeCombo(multiPermutation.element, ingredients);
                }
            } else {
                MultiCombo multiCombo = new MultiCombo(multiPermutation.element, ingredients);
                int count = multiPermutation.json.hasKey("amount") ? multiPermutation.json.getInt("amount") : 1;
                multiCombo.setAmount(count);
                if (multiPermutation.isRandom()) {
                    multiPermutation.randomCombo.addCombo(multiCombo);
                } else {
                    main.comboList.add(multiCombo);
                }
            }
        }

        main.randomCombos.removeIf(RandomCombo::isEmpty);

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

    private static void processCombo(JSONObject combo, String element, boolean remove, RandomCombo randomCombo) {
        if (combo.hasKey("first element")) {
            normals.add(new Normal(element, pack.getNamespacedName(combo.getString("first element")), pack.getNamespacedName(combo.getString("second element")), combo, remove, randomCombo));
        } else if (combo.hasKey("elements")) {
            if (combo.hasKey("paired") && combo.getBoolean("paired")) {
                permutations.add(new Permutation(element, PermutationType.UNRESTRICTED, combo, remove, randomCombo));
            } else {
                multiPermutations.add(new MultiPermutation(element, combo, remove, randomCombo));
            }
        } else if (combo.hasKey("first elements")) {
            permutations.add(new Permutation(element, PermutationType.RESTRICTED, combo, remove, randomCombo));
        } else {
            permutations.add(new Permutation(element, PermutationType.SETS, combo, remove, randomCombo));
        }
    }

    //TODO: validate MultiCombo
    private static void validateCombos() {
        Iterator<Combo> iterator = main.comboList.iterator();
        while (iterator.hasNext()) {
            Combo combo = iterator.next();
            if (combo instanceof NormalCombo) {
                NormalCombo normalCombo = (NormalCombo) combo;
                if (!main.elements.containsKey(normalCombo.getA()) || !main.elements.containsKey(normalCombo.getB()) || !main.elements.containsKey(normalCombo.getElement())) {
                    System.err.println("Error with combo: ");
                    if (!main.elements.containsKey(normalCombo.getA())) {
                        System.err.println(normalCombo.getA() + " doesn't exist!");
                    }
                    if (!main.elements.containsKey(normalCombo.getB())) {
                        System.err.println(normalCombo.getB() + " doesn't exist!");
                    }
                    if (!main.elements.containsKey(normalCombo.getElement())) {
                        System.err.println(normalCombo.getElement() + " doesn't exist!");
                    }
                    iterator.remove();
                }
            }
        }
    }

    private static void removeCombo(String element, ArrayList<String> ingredients) {
        main.comboList.removeIf(e -> e.getElement().equals(element) && CollectionUtils.isEqualCollection(e.getIngredients(), ingredients));
    }

    private enum PermutationType {
        UNRESTRICTED,
        RESTRICTED,
        SETS
    }

    private static ArrayList<ImmutablePair<String, String>> processPermutations(PermutationType permutationType, JSONObject combo, Pack pack) {
        ArrayList<ImmutablePair<String, String>> combos = new ArrayList<>();
        if (permutationType == PermutationType.UNRESTRICTED) {
            JSONArray elementsArray = combo.getJSONArray("elements");
            ArrayList<String> elements = processTags(elementsArray, pack);
            for (String a : elements) {
                for (String b : elements) {
                    if (!containsPair(a, b, combos) && !containsPair(b, a, combos) && !a.equals(b)) {
                        combos.add(new ImmutablePair<>(a, b));
                    }
                }
            }
        } else if (permutationType == PermutationType.RESTRICTED) {
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
        } else if (permutationType == PermutationType.SETS) {
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
                        if (element.getTags().contains(tag)) {
                            iterator.add(element.getName());
                        }
                    }
                }
            } else if (string.contains("group:")) {
                iterator.remove();
                String group = string.replace("group:", "");
                for (Element element : main.groups.get(Group.getGroup(group))) {
                    iterator.add(element.getName());
                }
            }
        }
        return list;
    }

}