package main;

import org.apache.commons.io.FilenameUtils;
import processing.data.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Language extends Entity {

    private static Language languageSelected;
    private static final ArrayList<Language> languages = new ArrayList<>();

    private final String id;
    private final JSONObject json;

    private Language(String id, JSONObject json) {
        this.json = json;
        this.id = id;
    }

    public static Language getLanguage(String id) {
        for (Language language : languages) {
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
        } catch (RuntimeException ignored) {
        }
        return null;
    }

    public static void loadLanguages(String path) {
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("json")) {
                    JSONObject object = Main.loadJSONObject(file);
                    String id = FilenameUtils.removeExtension(file.getName());
                    if (getLanguage(id) == null) { //language doesn't exist
                        languages.add(new Language(id, Main.loadJSONObject(file)));
                    } else {
                        Language language = getLanguage(id);
                        assert language != null;
                        traverseJSON(language.json, object);
                    }
                }
            }
        }
    }

    private static void traverseJSON(JSONObject original, JSONObject object) {
        for (Object k : original.keys()) {
            String key = (String) k;
            if (object.hasKey(key)) {
                if (original.get(key) instanceof JSONObject) {
                    traverseJSON((JSONObject) original.get(key), (JSONObject) object.get(key));
                } else {
                    original.put(key, object.get(key));
                }
            }
        }
        for (Object k : object.keys()) {
            String key = (String) k;
            if (original.isNull(key)) {
                original.put(key, object.get(key));
            }
        }
    }

    public JSONObject getJson() {
        return this.json;
    }

    //finds and removes unused element names, default english only
    public static void validateEnglish() {
        Language language = getLanguage("english");
        assert language != null;
        JSONObject object = language.json.getJSONObject("elements").getJSONObject("alchemy");
        //noinspection unchecked
        object.keys().removeIf(e -> Element.Companion.getElement("alchemy:" + e) == null);
        main.saveJSONObject(language.json, "resources/languages/english.json","indent=4");
    }

    public static void setLanguageSelected(String languageSelected) {
        Language.languageSelected = getLanguage(languageSelected);
    }

    public static Language getLanguageSelected() {
        return languageSelected;
    }
}
