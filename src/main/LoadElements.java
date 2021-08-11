package main;

import main.buttons.Group;
import main.buttons.Pack;
import main.combos.Combo;
import main.combos.MultiCombo;
import main.combos.NormalCombo;
import main.combos.RandomCombo;
import main.rooms.Loading;
import main.variations.Variation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.*;

public class LoadElements extends Entity {

    private static final ArrayList<Normal> normals = new ArrayList<>();
    private static final ArrayList<Permutation> permutations = new ArrayList<>();
    private static final ArrayList<MultiPermutation> multiPermutations = new ArrayList<>();
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
            String elementName = pack.getNamespacedName(object.getString("name"));

            if (object.hasKey("remove")) {
                String remove = object.getString("remove");
                //noinspection IfCanBeSwitch
                if (remove.equals("all")) {
                    ArrayList<Element> exceptElements = new ArrayList<>();
                    ArrayList<Combo> exceptCombos = new ArrayList<>();
                    ArrayList<RandomCombo> exceptRandom = new ArrayList<>();
                    if (object.hasKey("except")) {
                        JSONArray array1 = object.getJSONArray("except");
                        for (int j = 0;j < array1.size();j++) {
                            Element element1 = Element.Companion.getElement(array1.getString(j));
                            if (element1 != null) {
                                exceptElements.add(element1);
                                for (Combo combo : main.comboList) {
                                    if (combo.contains(element1.getId())) {
                                        exceptCombos.add(combo);
                                    }
                                }
                                for (RandomCombo randomCombo : main.randomCombos) {
                                    if (randomCombo.contains(element1.getId())) {
                                        exceptRandom.add(randomCombo);
                                    }
                                }
                            }
                        }
                    }
                    main.comboList.clear();
                    main.comboList.addAll(exceptCombos);
                    main.randomCombos.clear();
                    main.randomCombos.addAll(exceptRandom);
                    Loading.INSTANCE.removeAllElements(exceptElements.size()); //this needs to be called first or else we can't determine how much progress to remove
                    for (HashSet<Element> list : main.groups.values()) {
                        list.removeIf(e -> !exceptElements.contains(e));
                    }
                    main.elements.clear();
                    for (Element element1 : exceptElements) {
                        main.elements.put(element1.getId(), element1.getGroup());
                    }
                } else if (remove.equals("element")) {
                    //remove all combos of an element
                    String e = pack.getNamespacedName(object.getString("element"));
                    main.comboList.removeIf(combo -> combo.getElement().equals(e));
                    for (RandomCombo randomCombo : main.randomCombos) {
                        randomCombo.removeElement(e);
                    }
                    Element element1 = Element.Companion.getElement(e);
                    if (element1 != null) {
                        main.groups.get(element1.getGroup()).remove(element1);
                        main.elements.remove(e);
                        Loading.INSTANCE.removeElement();
                    } else {
                        System.err.println(e + " could not be removed!");
                    }
                } else if (remove.equals("combo")) {
                    JSONArray combos = object.getJSONArray("combos");
                    for (int j = 0; j < combos.size(); j++) {
                        processCombo(combos.getJSONObject(j), elementName, true, null);
                    }
                    Loading.INSTANCE.removeCombo();
                } else if (remove.equals("random")) {
                    JSONObject combo = object.getJSONObject("combo");
                    processCombo(combo, null, true, null);
                    Loading.INSTANCE.removeCombo();
                }
            } else if (object.hasKey("combo") && object.hasKey("result")) {
                //random
                RandomCombo randomCombo = new RandomCombo(object.getJSONArray("result"));
                JSONObject combo = object.getJSONObject("combo");
                processCombo(combo, null, false, randomCombo);
                main.randomCombos.add(randomCombo);
                Loading.INSTANCE.randomCombo();
            } else {
                Element element;
                //element might exist because we allow existing elements to be modified
                if (main.elements.containsKey(elementName)) {
                    element = Element.Companion.getElement(elementName);
                    assert element != null;
                    Loading.INSTANCE.modifyElement();
                } else {
                    Group group = Group.getGroup(pack.getNamespacedName(object.getString("group")));
                    if (group == null) {
                        System.err.println("Error: Group " + pack.getNamespacedName(object.getString("group")) + " not found!");
                        Loading.INSTANCE.elementFailed();
                        continue;
                    }
                    element = new Element(elementName, group, pack);
                    main.groups.get(group).add(element);
                    main.elements.put(elementName, group);
                }

                if (object.hasKey("persistent")) {
                    element.setPersistent(object.getBoolean("persistent"));
                }

                if (object.hasKey("description")) {
                    element.setDescription(object.getString("description"));
                }

                if (object.hasKey("tags")) {
                    JSONArray tags = object.getJSONArray("tags");
                    for (int j = 0; j < tags.size(); j++) {
                        //tags are namespaced too!
                        element.addTag(pack.getNamespacedName(tags.getString(j)));
                    }
                }

                if (object.hasKey("variation")) {
                    element.setVariation(Variation.getVariation(object.getJSONObject("variation"), element, pack));
                }

                if (object.hasKey("combos")) {
                    JSONArray combos = object.getJSONArray("combos");
                    for (int j = 0; j < combos.size(); j++) {
                        processCombo(combos.getJSONObject(j), elementName, false, null);
                    }
                }
            }

            Loading.INSTANCE.updateProgress();
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
                int count = normal.json.getInt("amount", 1);
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
                    int count = permutation.json.getInt("amount", 1);
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
                int count = multiPermutation.json.getInt("amount", 1);
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

    //use this method to parse combos if it is used after all combos are loaded
    public static ArrayList<Combo> getCombo(JSONObject combo, Element element) {
        ArrayList<Combo> list = new ArrayList<>();
        Permutation permutation = null;
        MultiPermutation multiPermutation = null;
        if (combo.hasKey("first element")) {
            list.add(new NormalCombo(element.getId(), element.getPack().getNamespacedName(combo.getString("first element")), element.getPack().getNamespacedName(combo.getString("second element"))));
        } else if (combo.hasKey("elements")) {
            if (combo.hasKey("paired") && combo.getBoolean("paired")) {
                permutation = new Permutation(element.getId(), PermutationType.UNRESTRICTED, combo, false, null);
            } else {
                multiPermutation = new MultiPermutation(element.getId(), combo, false, null);
            }
        } else if (combo.hasKey("first elements")) {
            permutation = new Permutation(element.getId(), PermutationType.RESTRICTED, combo, false, null);
        } else {
            permutation = new Permutation(element.getId(), PermutationType.SETS, combo, false, null);
        }
        if (permutation != null) {
            ArrayList<ImmutablePair<String, String>> combos = processPermutations(permutation.permutationType, permutation.json, element.getPack());
            for (ImmutablePair<String, String> pair : combos) {
                NormalCombo normalCombo = new NormalCombo(permutation.element, pair.left, pair.right);
                int count = permutation.json.getInt("amount", 1);
                normalCombo.setAmount(count);
                list.add(normalCombo);
            }
        }
        if (multiPermutation != null) {
            JSONArray elements = multiPermutation.json.getJSONArray("elements");
            ArrayList<String> ingredients = processTags(elements, element.getPack());
            MultiCombo multiCombo = new MultiCombo(multiPermutation.element, ingredients);
            int count = multiPermutation.json.getInt("amount", 1);
            multiCombo.setAmount(count);
            list.add(multiCombo);
        }
        return list;
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
        for (ImmutablePair<String, String> p : list) {
            if ((p.left.equals(a) && p.right.equals(b)) || (p.left.equals(b) && p.right.equals(a))) {
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
                            iterator.add(element.getId());
                        }
                    }
                }
            } else if (string.contains("group:")) {
                iterator.remove();
                String group = string.replace("group:", "");
                for (Element element : main.groups.get(Group.getGroup(group))) {
                    iterator.add(element.getId());
                }
            }
        }
        return list;
    }

}
