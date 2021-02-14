package main;

import main.buttons.ElementButton;
import main.buttons.Pack;
import main.combos.Combo;
import main.combos.MultiCombo;
import main.combos.NormalCombo;
import main.rooms.PacksRoom;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Generation extends Entity {

    private static final boolean PRINT_COMBOS = true;

    public static void generate(Set<String> elements, HashSet<Combo> combos) {
        HashSet<String> discovered = new HashSet<>();

        ArrayList<ElementButton> list = new ArrayList<>();
        for (Pack pack : PacksRoom.INSTANCE.getLoadedPacks()) {
            pack.getStartingElements(list);
        }
        for (ElementButton element : list) {
            discovered.add(element.getName());
        }

        try {
            PrintWriter printWriter = new PrintWriter("generation.txt");
            int generation = 1;
            while (discovered.size() < elements.size()) {
                printWriter.println("Generation: " + generation);
                HashSet<String> created = new HashSet<>(); //elements just created in this generation
                HashSet<Combo> generationCombos = new HashSet<>(); //combos that can be made in this generation
                for (Combo combo : combos) {
                    //cannot use canCreate here because we are not using main.game.discovered
                    if (!discovered.contains(combo.getElement())) {
                        //checking if all ingredients are discovered
                        if (combo instanceof NormalCombo) {
                            NormalCombo normalCombo = (NormalCombo) combo;
                            if (discovered.contains(normalCombo.getA()) && discovered.contains(normalCombo.getB())) {
                                created.add(normalCombo.getElement());
                                generationCombos.add(normalCombo);
                            }
                        } else if (combo instanceof MultiCombo) {
                            MultiCombo multiCombo = (MultiCombo) combo;
                            //here I cannot use ingredientsDiscovered because that uses main.game.discovered which is not what we want
                            boolean flag = true;
                            for (String ingredient : multiCombo.getIngredients()) {
                                if (!discovered.contains(ingredient)) {
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                created.add(multiCombo.getElement());
                                generationCombos.add(multiCombo);
                            }
                        }
                    }
                }

                if (PRINT_COMBOS) {
                    for (Combo combo : generationCombos) {
                        printWriter.println((String.join(", ", combo.getIngredients()) + ", " + combo.getElement()));
                    }
                } else {
                    printWriter.println(String.join(", ", created));
                }

                discovered.addAll(created);
                generation++;

                //if nothing was created in this generation but we are not done yet, that means there's an error
                if (created.size() == 0 && discovered.size() < elements.size()) {
                    HashSet<String> missing = new HashSet<>();
                    for (String element : elements) {
                        if (!discovered.contains(element)) {
                            missing.add(element);
                        }
                    }
                    System.err.println("Error: The following elements can't be reached: " + String.join(", ", missing));
                    break;
                }
            }
            printWriter.close();
        } catch (FileNotFoundException ignored) {

        }
    }

}
