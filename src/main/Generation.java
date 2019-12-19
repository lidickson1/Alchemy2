package main;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Generation {

    public static void generate(HashSet<String> elements, HashSet<Combo> combos, HashMap<String, ArrayList<ArrayList<String>>> multicombos) {
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
                    if (!discovered.contains(combo.getElement()) && discovered.contains(combo.getA()) && discovered.contains(combo.getB())) {
                        created.add(combo.getElement());
                    }
                }
                for (String key : multicombos.keySet()) {
                    if (!discovered.contains(key)) {
                        for (ArrayList<String> list : multicombos.get(key)) {
                            boolean flag = true;
                            for (String element : list) {
                                if (!discovered.contains(element)) {
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag) {
                                created.add(key);
                                break;
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
