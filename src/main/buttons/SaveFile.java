package main.buttons;

import main.Language;
import processing.core.PConstants;
import processing.data.JSONObject;

import java.time.LocalDateTime;

public class SaveFile extends LongButton {

    private String name;
    private JSONObject json;

    public SaveFile(String name, JSONObject json) {
        super();

        this.name = name;
        this.json = json;
    }

    public String getName() {
        return this.name;
    }

    public JSONObject getJson() {
        return this.json;
    }

    @Override
    protected void drawButton() {
        super.drawButton();

        main.textSize(20);
        main.textAlign(PConstants.LEFT, PConstants.CENTER);
        main.fill(255);
        main.text(this.name, this.getX() + 10, this.getY() + 28);

        main.textAlign(PConstants.RIGHT, PConstants.CENTER);
        main.fill(120);
        main.text(Language.getLanguageSelected().getLocalizedString("load game", "last modified") + ": " + this.json.getString("last modified"), this.getX() + WIDTH - 10, this.getY() + 28);
    }

    @Override
    public void clicked() {
        main.game.setSaveFile(this);
        main.switchRoom(main.game);
    }

    @Override
    public int compareTo(LongButton o) {
        return -LocalDateTime.parse(this.json.getString("last modified"), main.formatter).compareTo(LocalDateTime.parse(((SaveFile) o).json.getString("last modified"), main.formatter));
    }
}
