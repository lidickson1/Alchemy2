package main.rooms;

import main.buttons.Group;
import main.buttons.LongButton;
import main.buttons.Pack;
import main.buttons.iconbuttons.Exit;
import main.buttons.iconbuttons.IconButton;
import processing.core.PConstants;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class PacksRoom extends Room {

    private static final int GAP = 100;

    private Exit exit;
    private IconButton done;

    private ArrayList<Pack> unloadedPacks = new ArrayList<>();
    private ArrayList<Pack> loadedPacks = new ArrayList<>();
    private Pack movePack; //pack to be moved

    public PacksRoom() {
        this.exit = new Exit();
        this.done = new IconButton("resources/images/done_button.png") {
            @Override
            public void clicked() {
                main.packsRoom.save();
                main.switchRoom(main.loading);
            }
        };
    }

    public void setMovePack(Pack movePack) {
        this.movePack = movePack;
    }

    @Override
    public void setup() {
        this.unloadedPacks.clear();
        this.loadedPacks.clear();
        this.movePack = null;

        File[] directory = new File("resources/packs/").listFiles();
        if (directory != null) {
            for (File folder : directory) {
                File jsonFile = new File(folder.getAbsolutePath() + "/pack.json");
                File icon = new File(folder.getAbsolutePath() + "/icon.png");
                if (jsonFile.exists() && icon.exists()) {
                    JSONObject object = main.loadJSONObject(jsonFile.getAbsolutePath());
                    if (object.getString("name").equals("Alchemy")) {
                        System.err.println("Error: pack name cannot be name \"Alchemy\"");
                        continue;
                    }
                    this.unloadedPacks.add(new Pack(folder.getAbsolutePath(), object));
                }
            }
        }
        this.unloadedPacks.add(new Pack()); //add default pack

        main.packsRoom.unloadedPacks.sort(Pack::compareTo);

        //loaded packs are read from settings file
        JSONArray array = main.getSettings().getJSONArray("loaded packs");
        for (int i = 0; i < array.size(); i++) {
            String string = array.getString(i);
            Pack pack;
            if (string.equals("Alchemy")) {
                pack = getPack("Alchemy", this.unloadedPacks);
            } else {
                pack = getPack(string, this.unloadedPacks);
                if (pack == null) {
                    System.err.println("Error: can't load pack with name: " + string);
                    continue;
                }
            }
            this.unloadedPacks.remove(pack);
            this.loadedPacks.add(pack);
        }
    }

    public ArrayList<Pack> getLoadedPacks() {
        return this.loadedPacks;
    }

    @Override
    public void draw() {
        this.drawTitle("packs", "packs");

        int x = main.screenWidth / 2 - GAP / 2 - LongButton.WIDTH;
        int y = 120;

        main.textSize(24);
        main.textAlign(PConstants.CENTER, PConstants.CENTER);
        main.fill(255);
        main.text(main.getLanguageSelected().getLocalizedString("packs", "unloaded packs"), x + LongButton.WIDTH / 2, y);

        y += 40;

        for (Pack pack : this.unloadedPacks) {
            pack.draw(x, y);
            y += pack.getHeight();
        }

        x = main.screenWidth / 2 + GAP / 2;
        y = 120;

        main.textSize(24);
        main.textAlign(PConstants.CENTER, PConstants.CENTER);
        main.fill(255);
        main.text(main.getLanguageSelected().getLocalizedString("packs", "loaded packs"), x + LongButton.WIDTH / 2, y);

        y += 40;

        for (Pack pack : this.loadedPacks) {
            pack.draw(x, y);
            y += pack.getHeight();
        }

        this.done.setDisabled(this.loadedPacks.size() == 0);
        this.done.draw(main.screenWidth - Group.GAP - IconButton.SIZE - (IconButton.SIZE + Group.GAP), main.screenHeight - Group.GAP - IconButton.SIZE);
        this.exit.draw();
    }

    @Override
    public void mousePressed() {
        for (Pack pack : this.unloadedPacks) {
            pack.mousePressed();
        }

        for (Pack pack : this.loadedPacks) {
            pack.mousePressed();
        }

        if (this.movePack != null) {
            if (main.packsRoom.unloadedPacks.contains(this.movePack)) {
                main.packsRoom.unloadedPacks.remove(this.movePack);
                main.packsRoom.loadedPacks.add(0, this.movePack);
            } else {
                main.packsRoom.loadedPacks.remove(this.movePack);
                main.packsRoom.unloadedPacks.add(this.movePack);
            }
            main.packsRoom.unloadedPacks.sort(Pack::compareTo);
        }
        this.movePack = null;

        this.done.mousePressed();
        this.exit.mousePressed();
    }

    public void save() {
        JSONArray array = new JSONArray();
        for (Pack pack : this.loadedPacks) {
            array.append(pack.getName());
        }
        main.getSettings().put("loaded packs", array);
        main.saveSettings();
    }

    private static Pack getPack(String name, ArrayList<Pack> list) {
        for (Pack pack : list) {
            if (pack.getName().equals(name)) {
                return pack;
            }
        }
        return null;
    }


}
