package main.buttons;

import kotlin.Pair;
import main.Element;
import main.Language;
import main.Main;
import main.rooms.Loading;
import main.rooms.PacksRoom;
import org.apache.commons.lang3.StringUtils;
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
    private final JSONObject json;
    private final PImage icon;
    private JSONObject englishJson;
    private final HashMap<String, PImage> atlasMap = new HashMap<>();

    public Pack(String path, JSONObject json) {
        super();

        this.path = path;
        this.json = json;
        this.icon = Main.INSTANCE.loadImage(path + "/icon.png");
        this.icon.resize(HEIGHT - 1, HEIGHT - 1);

        if (this.json.getBoolean("auto english", false)) {
            this.englishJson = Main.INSTANCE.loadJSONObject(path + "/languages/english.json");
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

        this.icon = Main.INSTANCE.loadImage("resources/images/icon.png");
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
            PImage atlas = Main.INSTANCE.loadImage(this.getAtlasImagePath());
            Scanner scanner = new Scanner(new File(this.getAtlasTextPath()));
            int x = 0;
            int y = 0;
            this.atlasMap.clear();
            while (scanner.hasNextLine()) {
                String name = scanner.nextLine();
                int count = StringUtils.countMatches(name, ":");
                if (count == 1) {
                    Element element = Element.Companion.getElement(name);
                    assert element != null;
                    element.setImage(atlas.get(x, y, ElementButton.SIZE, ElementButton.SIZE));
                } else {
                    this.atlasMap.put(name, atlas.get(x, y, ElementButton.SIZE, ElementButton.SIZE));
                }
                x += ElementButton.SIZE;
                if (x >= atlas.width) {
                    x = 0;
                    y += ElementButton.SIZE;
                }
            }
        } catch (FileNotFoundException ignored) {
        }
        Loading.INSTANCE.updateProgress();
    }

    public void loadLanguages() {
        Language.loadLanguages(this.getName().equals("Alchemy") ? "resources/languages" : this.path + "/languages");
    }

    private ArrayList<Pair<PImage, String>> getPairs() {
        ArrayList<Pair<PImage, String>> list = new ArrayList<>();
        for (HashSet<Element> elements : Main.INSTANCE.groups.values()) {
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
                ArrayList<Pair<PImage, String>> pairs = this.getPairs();
                int width = (int) Math.round(Math.sqrt(pairs.size()));
                int height = (int) Math.ceil((double) pairs.size() / width);
                if (width == 0 || height == 0) {
                    return; //this could happen if a pack removes elements!
                }
                PGraphics graphics = Main.INSTANCE.createGraphics(width * ElementButton.SIZE, height * ElementButton.SIZE);
                PrintWriter printWriter = new PrintWriter(this.getAtlasTextPath());
                graphics.beginDraw();
                int index = 0;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        Pair<PImage, String> pair = pairs.get(index);
                        printWriter.println(pair.getSecond());
                        graphics.image(pair.getFirst(), j * ElementButton.SIZE, i * ElementButton.SIZE);
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

    public String getNamespace() {
        return this.json.getString("namespace");
    }

    @Override
    protected void drawButton() {
        Main.INSTANCE.image(this.icon, this.getX() + 1, this.getY() + 1);

        super.drawButton(); //drawing this later because I want the outline to be drawn on top of the icon

        Main.INSTANCE.textSize(20);
        Main.INSTANCE.textAlign(PConstants.LEFT, PConstants.CENTER);
        Main.INSTANCE.fill(255);
        Main.INSTANCE.text(this.getName(), this.getX() + HEIGHT + 10, this.getY() + 16);

        Main.INSTANCE.textAlign(PConstants.LEFT, PConstants.CENTER);
        Main.INSTANCE.fill(120);
        Main.INSTANCE.text(Language.getLanguageSelected().getLocalizedString("packs", "by") + ": " + this.json.getString("author"), this.getX() + HEIGHT + 10, this.getY() + HEIGHT - 26);
    }

    @Override
    public void clicked() {
        PacksRoom.INSTANCE.setMovePack(this);
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
            if (name.length() >= 5 && name.startsWith("tag:")) { //5 because at least a character should be behind "tag:"
                return this.json.getString("namespace") + ":" + name;
            } else if (name.length() >= 7 && name.startsWith("group:")) {
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

    public void getStartingElements(ArrayList<Element> elements) {
        if (this.json.isNull("starting elements")) {
            return;
        }
        JSONArray array = this.json.getJSONArray("starting elements");
        for (int i = 0; i < array.size(); i++) {
            String name;
            boolean remove = false;
            if (array.get(i) instanceof String) {
                name = this.getNamespacedName(array.getString(i));
            } else {
                name = this.getNamespacedName(array.getJSONObject(i).getString("name"));
                remove = array.getJSONObject(i).getBoolean("remove", false);
            }
            Element element = Element.Companion.getElement(name);
            if (element == null) {
                //that means the starting element got removed
                //This is allowed if you want to control the starting elements by doing so
            } else {
                if (remove) {
                    elements.remove(element);
                } else {
                    elements.add(element);
                }
            }
        }
    }

    public void generateEnglish(String element) {
        if (this.englishJson != null) {
            String name = WordUtils.capitalize(element.replace("_"," "));
            this.englishJson.getJSONObject("elements").getJSONObject(this.getNamespace()).put(element, name);
            Objects.requireNonNull(Language.getLanguage("english")).getJson().getJSONObject("elements").getJSONObject(this.getNamespace()).put(element, name);
            Main.INSTANCE.saveJSONObject(this.englishJson, this.path + "/languages/english.json", "indent=4");
        }
    }

}
