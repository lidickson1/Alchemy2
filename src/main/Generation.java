package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;

public class Generation {

    public static void generate(HashSet<String> elements, HashSet<Combo> combos) {
        HashSet<String> discovered = new HashSet<>();

        discovered.add("alchemy:air");
        discovered.add("alchemy:earth");
        discovered.add("alchemy:fire");
        discovered.add("alchemy:water");

        try {
            PrintWriter printWriter = new PrintWriter("generation.txt");
            int generation = 1;
            while (discovered.size() < elements.size()) {
                printWriter.println("Generation: " + generation);
                HashSet<String> created = new HashSet<>(); //elements just created in this generation
                for (Combo combo : combos) {
                    //cannot use canCreate here because we are not using main.game.discovered
                    if (!discovered.contains(combo.getElement())) {
                        if (combo instanceof NormalCombo) {
                            NormalCombo normalCombo = (NormalCombo) combo;
                            if (!discovered.contains(normalCombo.getElement()) && discovered.contains(normalCombo.getA()) && discovered.contains(normalCombo.getB())) {
                                created.add(normalCombo.getElement());
                            }
                        } else if (combo instanceof MultiCombo) {
                            MultiCombo multiCombo = (MultiCombo) combo;
                            boolean flag = true;
                            for (String ingredient : multiCombo.getIngredients()) {
                                if (!discovered.contains(ingredient)) {
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                discovered.add(multiCombo.getElement());
                            }
                        }
                    }
                }

                printWriter.println(String.join(", ", created));
                discovered.addAll(created);
                generation++;

                //nothing was created in this generation, but we are not done yet, this means there's an error
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
