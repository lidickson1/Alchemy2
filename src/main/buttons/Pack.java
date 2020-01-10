package main.buttons;

import main.Language;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.text.WordUtils;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Pack extends LongButton {

    private String path;
    private JSONObject json;
    private PImage icon;
    private JSONObject englishJson;
    private HashMap<String, PImage> atlasMap = new HashMap<>();

    public Pack(String path, JSONObject json) {
        super();

        this.path = path;
        this.json = json;
        this.icon = main.loadImage(path + "/icon.png");
        this.icon.resize(HEIGHT - 1, HEIGHT - 1);

        if (this.json.hasKey("auto english") && this.json.getBoolean("auto english")) {
            this.englishJson = main.loadJSONObject(path + "/languages/english.json");
        }
    }

    public Pack() {
        super();

        this.json = new JSONObject();
        this.json.setString("name", "Alchemy");
        this.json.setString("namespace", "alchemy");
        this.json.setString("author", "God");

        JSONArray array = new JSONArray();
        array.append("alchemy:air");
        array.append("alchemy:earth");
        array.append("alchemy:fire");
        array.append("alchemy:water");
        this.json.setJSONArray("starting elements", array);

        this.icon = main.loadImage("resources/images/icon.png");
        this.icon.resize(HEIGHT, HEIGHT);
    }

    public String getPath() {
        return this.path;
    }

    private String getAtlasImagePath() {
        if (this.getName().equals("Alchemy")) {
            return "resources/elements/atlas.png";
        } else {
            return this.getPath() + "/elements/atlas.png";
        }
    }

    private String getAtlasTextPath() {
        if (this.getName().equals("Alchemy")) {
            return "resources/elements/atlas.txt";
        } else {
            return this.getPath() + "/elements/atlas.txt";
        }
    }

    public boolean hasAtlas() {
        return new File(this.getAtlasImagePath()).exists() && new File(this.getAtlasTextPath()).exists();
    }

    public PImage getAtlasImage(String path) {
        return this.atlasMap.getOrDefault(path, null);
    }

    public void loadAtlas() {
        try {
            PImage atlas = main.loadImage(this.getAtlasImagePath());
            Scanner scanner = new Scanner(new File(this.getAtlasTextPath()));
            int x = 0;
            int y = 0;
            this.atlasMap.clear();
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine();
                int count = StringUtils.countMatches(name, ":");
                if (count == 1) {
                    Element element = Element.getElement(name);
                    assert element != null;
                    element.setImage(atlas.get(x, y, Element.SIZE, Element.SIZE));
                } else {
                    this.atlasMap.put(name, atlas.get(x, y, Element.SIZE, Element.SIZE));
                }
                x += Element.SIZE;
                if (x >= atlas.width) {
                    x = 0;
                    y += Element.SIZE;
                }
            }
        } catch (FileNotFoundException ignored) {
        }
        main.loading.updateProgress();
    }

    public void loadLanguages() {
        Language.loadLanguages(this.getName().equals("Alchemy") ? "resources/languages" : this.path + "/languages");
    }

    private ArrayList<ImmutablePair<PImage, String>> getPairs() {
        ArrayList<ImmutablePair<PImage, String>> list = new ArrayList<>();
        for (HashSet<Element> elements : main.groups.values()) {
            for (Element element : elements) {
                if (element.isInPack(this)) {
                    list.addAll(element.getImages());
                }
            }
        }
        return list;
    }

    public void generateAtlas() {
        if (!this.hasAtlas()) {
            try {
                ArrayList<ImmutablePair<PImage, String>> pairs = this.getPairs();
                int width = (int) Math.round(Math.sqrt(pairs.size()));
                int height = (int) Math.ceil((double) pairs.size() / width);
                if (width == 0 || height == 0) {
                    return; //this could happen if a pack removes elements!
                }
                PGraphics graphics = main.createGraphics(width * Element.SIZE, height * Element.SIZE);
                PrintWriter printWriter = new PrintWriter(this.getAtlasTextPath());
                graphics.beginDraw();
                int index = 0;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        ImmutablePair<PImage, String> pair = pairs.get(index);
                        printWriter.println(pair.right);
                        graphics.image(pair.left, j * Element.SIZE, i * Element.SIZE);
                        index++;
                        if (index >= pairs.size()) {
                            break;
                        }
                    }
                    if (index >= pairs.size()) {
                        break;
                    }
                }
                graphics.endDraw();
                graphics.save(this.getAtlasImagePath());
                printWriter.close();
            } catch (FileNotFoundException ignored) {

            }
        }
    }

    public String getName() {
        return this.json.getString("name");
    }

    String getNamespace() {
        return this.json.getString("namespace");
    }

    @Override
    protected void drawButton() {
        main.image(this.icon, this.getX() + 1, this.getY() + 1);

        super.drawButton(); //drawing this later because I want the outline to be drawn on top of the icon

        main.textSize(20);
        main.textAlign(PConstants.LEFT, PConstants.CENTER);
        main.fill(255);
        main.text(this.getName(), this.getX() + HEIGHT + 10, this.getY() + 16);

        main.textAlign(PConstants.LEFT, PConstants.CENTER);
        main.fill(120);
        main.text(Language.getLanguageSelected().getLocalizedString("packs", "by") + ": " + this.json.getString("author"), this.getX() + HEIGHT + 10, this.getY() + HEIGHT - 26);
    }

    @Override
    public void clicked() {
        main.packsRoom.setMovePack(this);
    }

    @Override
    public int compareTo(LongButton o) {
        return this.getName().compareTo(((Pack) o).getName());
    }

    //process namespace and illegal names
    //returns null if it is an invalid name
    public String getNamespacedName(String name) {
        //TODO: illegal names or if there is a ":" but it's not in the format or a:b or a:tag/group:b
        int count = StringUtils.countMatches(name, ":");
        if (count == 0) {
            return this.json.getString("namespace") + ":" + name;
        } else if (count == 1) {
            //must be in the form of: tag:a, group:a, a:b
            if (name.length() >= 5 && name.substring(0, 4).equals("tag:")) { //5 because at least a character should be behind "tag:"
                return this.json.getString("namespace") + ":" + name;
            } else if (name.length() >= 7 && name.substring(0, 6).equals("group:")) {
                return this.json.getString("namespace") + ":" + name;
            } else {
                String[] split = name.split(":");
                if (split.length == 2) {
                    return name;
                } else {
                    //invalid -> :, a:, :b, a:b:c
                    return null;
                }
            }
        } else if (count == 2) {
            String[] split = name.split(":");
            if (split.length != 3) {
                return null;
            } else {
                if (!split[1].equals("tag") && !split[1].equals("group")) {
                    return null;
                } else {
                    //valid -> a:tag:b OR a:group:b
                    return name;
                }
            }
        }
        return null;
    }

    public ArrayList<Element> getStartingElements() {
        ArrayList<Element> list = new ArrayList<>();
        if (this.json.isNull("starting elements")) {
            return list;
        }
        JSONArray array = this.json.getJSONArray("starting elements");
        for (int i = 0; i < array.size(); i++) {
            String name = this.getNamespacedName(array.getString(i));
            Element element = Element.getElement(name);
            if (element == null) {
                System.err.println("Error when loading starting elements: " + name + " not found!");
            } else {
                list.add(element);
            }
        }
        return list;
    }

    public void generateEnglish(String element) {
        if (this.englishJson != null) {
            String name = WordUtils.capitalize(element.replace("_"," "));
            this.englishJson.getJSONObject("elements").getJSONObject(this.getNamespace()).put(element, name);
            Objects.requireNonNull(Language.getLanguage("english")).getJson().getJSONObject("elements").getJSONObject(this.getNamespace()).put(element, name);
            main.saveJSONObject(this.englishJson, this.path + "/languages/english.json", "indent=4");
        }
    }

}
