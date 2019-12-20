package main.rooms;

import main.Language;
import main.Main;
import main.buttons.SaveFile;
import main.buttons.iconbuttons.Exit;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;

public class LoadGame extends Room {

    private Exit exit;
    private boolean failed;

    private ArrayList<SaveFile> saveFiles = new ArrayList<>();

    public LoadGame() {
        this.exit = new Exit();
    }

    @Override
    public void setup() {
        this.failed = false;
        this.saveFiles.clear();
        File[] files = new File("resources/saves/").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("json")) {
                    this.saveFiles.add(new SaveFile(FilenameUtils.removeExtension(file.getName()), Main.loadJSONObject(file)));
                }
            }
        }
        this.saveFiles.sort(SaveFile::compareTo);
    }

    @Override
    public void draw() {
        this.drawTitle("load game", "load game");

        int y = 120;
        for (SaveFile saveFile : this.saveFiles) {
            saveFile.draw(main.screenWidth / 2F - saveFile.getWidth() / 2F, y);
            y += saveFile.getHeight();
        }

        this.exit.draw();
    }

    @Override
    public void mousePressed() {
        for (SaveFile saveFile : this.saveFiles) {
            saveFile.mousePressed();
            if (this.failed) {
                break;
            }
        }

        if (this.failed) {
            main.showError(Language.getLanguageSelected().getLocalizedString("load game", "elements not loaded"));
            main.switchRoom(main.loadGame);
        }

        this.exit.mousePressed();
    }

    void failed() {
        this.failed = true;
    }

}
