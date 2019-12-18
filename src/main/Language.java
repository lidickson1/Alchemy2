package main;

import org.apache.commons.io.FilenameUtils;
import processing.data.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Language extends Entity {

    private String id;
    private JSONObject json;

    private Language(String id, JSONObject json) {
        this.json = json;
        this.id = id;
    }

    static Language getLanguage(String id) {
        for (Language language : main.getLanguages()) {
            if (language.id.equals(id)) {
                return language;
            }
        }
        return null;
    }

    public String getLocalizedString(String object, String key) {
        try {
            return this.json.getJSONObject(object).getString(key);
        } catch (NullPointerException ignored) {
            return object + "." + key;
        }
    }

    public String getElementLocalizedString(String namespace, String id) {
        try {
            return this.json.getJSONObject("elements").getJSONObject(namespace).getString(id);
        } catch (NullPointerException ignored) {
        }
        return null;
    }

    static void loadLanguages(ArrayList<Language> list) {
        File[] files = new File("resources/languages").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("json")) {
                    list.add(new Language(FilenameUtils.removeExtension(file.getName()), Main.loadJSONObject(file)));
                }
            }
        }
    }

}
