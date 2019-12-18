package main.rooms;

import main.TextField;
import main.buttons.iconbuttons.Exit;
import main.buttons.iconbuttons.Save;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class SaveRoom extends Room {

    private TextField textField;

    private Save save;
    private Exit exit;

    private String text;

    public SaveRoom() {
        this.save = new Save();
        this.exit = new Exit();
    }

    @Override
    public void setup() {
        //for hiding the G4P announcement
        PrintStream originalStream = System.out;
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        });
        System.setOut(dummyStream);
        this.textField = new TextField(main, 400, 30);
        System.setOut(originalStream);
    }

    @Override
    public void draw() {
        this.save.setDisabled(!this.updateText());

        this.drawTitle("save", "save game");

        main.textSize(20);
        main.text(main.getLanguageSelected().getLocalizedString("save", "enter name"), main.screenWidth / 2, main.screenHeight / 2 - 60);

        this.textField.moveTo(main.screenWidth / 2F - this.textField.getWidth() / 2, main.screenHeight / 2F - this.textField.getHeight() / 2);
        this.textField.draw();

        if (!this.text.equals("")) {
            main.textSize(20);
            main.text(main.getLanguageSelected().getLocalizedString("save", this.text), main.screenWidth / 2, main.screenHeight / 2 + 60);
        }

        main.stroke(255);
        main.noFill();
        main.rect(this.textField.getX(), this.textField.getY(), this.textField.getWidth(), this.textField.getHeight());

        this.save.draw(main.screenWidth / 2F - 15 - this.save.getWidth(), main.screenHeight - 30 - this.save.getHeight());
        this.exit.draw(main.screenWidth / 2F + 15, main.screenHeight - 30 - this.exit.getHeight());
    }

    @Override
    public void end() {
        this.textField.dispose();
    }

    @Override
    public void mousePressed() {
        this.save.mousePressed();
        this.exit.mousePressed();
    }

    private boolean updateText() {
        String input = this.textField.getText().trim();

        if (input.equals("")) {
            this.text = "name empty";
            return false;
        }

        File[] files = new File("resources/saves/").listFiles();
        if (files != null) {
            ArrayList<String> names = new ArrayList<>();
            for (File file : files) {
                if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("json")) {
                    names.add(FilenameUtils.removeExtension(file.getName()));
                }
            }
            if (names.contains(input)) {
                this.text = "already exists";
                return false;
            }
        }

        this.text = "";
        return true;
    }

    String getSaveName() {
        return this.textField.getText().trim();
    }

}
