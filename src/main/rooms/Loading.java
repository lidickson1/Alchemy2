package main.rooms;

import main.Generation;
import main.Language;
import main.LoadElements;
import main.buttons.Element;
import main.buttons.Group;
import main.buttons.Pack;
import processing.core.PConstants;
import processing.data.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Loading extends Room {

    private static final boolean GENERATE_ATLAS = false;
    private static final boolean PRINT_GENERATIONS = true;

    private AtomicInteger progress;
    private int total;

    private String splashText;

    @Override
    public void setup() {
        this.splashText = this.getSplashText();
        this.progress = new AtomicInteger(0);
        this.total = 0;

        main.packsRoom.setup(); //reads the pack list

        main.elements.clear();
        main.groups.clear();
        main.comboList.clear();
        main.randomCombos.clear();

        //packs are loaded from bottom to top, for loop in reverse
        Thread thread = new Thread(() -> {
            for (int i = main.packsRoom.getLoadedPacks().size() - 1; i >= 0; i--) {
                Pack pack = main.packsRoom.getLoadedPacks().get(i);
                JSONArray groupsArray = new JSONArray();
                JSONArray elementsArray = new JSONArray();

                if (pack.getName().equals("Alchemy")) {
                    groupsArray = main.loadJSONArray("resources/groups.json");
                    elementsArray = main.loadJSONArray("resources/elements.json");
                    //Compressor.toBinary(elementsArray);
                    //Compressor.fromBinary(new File("elements.bin"));
                } else {
                    if (new File(pack.getPath() + "/groups.json").exists()) {
                        groupsArray = main.loadJSONArray(pack.getPath() + "/groups.json");
                    }
                    if (new File(pack.getPath() + "/elements.json").exists()) {
                        elementsArray = main.loadJSONArray(pack.getPath() + "/elements.json");
                    }
                }

                if (pack.hasAtlas()) {
                    this.total += groupsArray.size() + elementsArray.size() + 1;
                } else {
                    this.total += groupsArray.size() + elementsArray.size() * 2;
                }

                Group.loadGroups(groupsArray, pack);
                LoadElements.loadElements(elementsArray, pack);

                if (pack.hasAtlas()) {
                    pack.loadAtlas();
                }

                pack.loadLanguages();
                //TODO: this to read from settings
                Language.setLanguageSelected("english");
            }

            if (PRINT_GENERATIONS) {
                HashSet<String> set = new HashSet<>();
                for (HashSet<Element> list : main.groups.values()) {
                    set.addAll(list.stream().map(Element::getName).collect(Collectors.toList()));
                }
                Generation.generate(set, main.comboList);
            }

            //textures are loaded here, after we have defined all of the elements
            final int size = 50; //size of each buffer
            ArrayList<Element> buffer = new ArrayList<>();
            int index = 0;
            for (Group group : main.groups.keySet()) {
                for (Element element : main.groups.get(group)) {
                    if (element.getImage() == null) { //some images might already be loaded from an atlas
                        if (index != 0 && index % size == 0) {
                            Element.loadImage(buffer);
                            buffer = new ArrayList<>();
                        }
                        buffer.add(element);
                        index++;
                    }
                }
            }
            if (!buffer.isEmpty()) {
                Element.loadImage(buffer);
            }

        });
        thread.setDaemon(true);
        thread.start();

        main.textFont(main.font, 20);
    }

    @Override
    public void draw() {
        // Loading screen
        main.fill(0);
        main.rect(0, 0, main.screenWidth, main.screenHeight);
        main.image(main.getIcon(), main.screenWidth / 2F - main.getIcon().width / 2F, main.screenHeight / 2F - main.getIcon().height / 2F - 90);

        final int width = 900;
        main.noFill();
        main.stroke(255);
        main.rect(main.screenWidth / 2F - 450, main.screenHeight / 2F + 50, width, 10);
        main.fill(255);
        main.noStroke();
        int length = Math.round(((float) this.progress.get() / this.total) * width);
        main.rect(main.screenWidth / 2F - 450, main.screenHeight / 2F + 50, length, 10);
        main.textAlign(PConstants.CENTER);

        main.text(this.splashText, main.screenWidth / 2F, main.screenHeight / 2F + 100);

        if (this.total > 0 && this.progress.get() >= this.total) {
            //generate atlases, this can only be executed when all images are loaded
            if (GENERATE_ATLAS) {
                for (Pack pack : main.packsRoom.getLoadedPacks()) {
                    pack.generateAtlas();
                }
            }
            //Language.validateEnglish();
            main.switchRoom(main.menu);
        }
    }

    private String getSplashText() {
        ArrayList<String> list = new ArrayList<>();
        File file = new File("resources/splash.txt");
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int random = main.random.nextInt(list.size());
        this.splashText = list.get(random);
        return this.splashText.replace("#", "\n");
    }

    public void updateProgress() {
        this.progress.incrementAndGet();
    }

    public void elementFailed() {
        //remove its counter from total
        this.total -= 2;
        this.checkEverythingFailed();
    }

    public void removeAllElements(int except) {
        //remove its counter from total (json loading is successful, no need to load image)
        this.total--;
        for (HashSet<Element> list : main.groups.values()) {
            //we only subtract 1 for each because the json loading is already done, only need to remove the progress for loading the images
            this.total -= list.size();
        }
        this.total += except;
        this.checkEverythingFailed();
    }

    private void checkEverythingFailed() {
        //if everything fails
        if (this.total == 0 && this.progress.get() == 0) {
            this.total = 1;
            this.progress.set(1);
        }
    }

    public void removeElement() {
        this.total -= 2; //remove both the image of the remove and the actual element
        this.checkEverythingFailed();
    }

    public void removeCombo() {
        this.total--;
        this.checkEverythingFailed();
    }

    public void removeGroup(Group group) {
        this.total -= main.groups.get(group).size();
        this.checkEverythingFailed();
    }

    public void removeAllGroups() {
        for (Group group : main.groups.keySet()) {
            this.total -= main.groups.get(group).size();
        }
        this.checkEverythingFailed();
    }

    public void randomCombo() {
        this.total--;
        this.checkEverythingFailed();
    }

    public void modifyElement() {
        this.total--;
        this.checkEverythingFailed();
    }

}
