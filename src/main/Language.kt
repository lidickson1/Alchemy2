package main

import main.Element.Companion.getElement
import processing.data.JSONObject
import java.lang.NullPointerException
import java.lang.RuntimeException
import processing.core.PApplet
import java.io.File
import java.util.ArrayList

class Language private constructor(private val id: String, val json: JSONObject) {
    fun getLocalizedString(`object`: String, key: String): String {
        return try {
            json.getJSONObject(`object`).getString(key)
        } catch (ignored: NullPointerException) {
            "$`object`.$key"
        }
    }

    fun getElementLocalizedString(namespace: String?, id: String?): String? {
        try {
            return json.getJSONObject("elements").getJSONObject(namespace).getString(id)
        } catch (ignored: RuntimeException) {
        }
        return null
    }

    companion object {
        @JvmStatic
        lateinit var languageSelected: Language
        private val languages = ArrayList<Language>()

        @JvmStatic
        fun getLanguage(id: String): Language? = languages.find { it.id == id }

        @JvmStatic
        fun loadLanguages(path: String) {
            File(path).listFiles()?.let {
                for (file in it) {
                    if (file.isFile && file.extension == "json") {
                        val `object` = PApplet.loadJSONObject(file)
                        val id = file.nameWithoutExtension
                        if (getLanguage(id) == null) { //language doesn't exist
                            languages.add(Language(id, PApplet.loadJSONObject(file)))
                        } else {
                            val language = getLanguage(id)!!
                            //we recursively traverse the json, and merge changes in place
                            traverseJSON(language.json, `object`)
                        }
                    }
                }
            }
        }

        private fun traverseJSON(original: JSONObject, `object`: JSONObject) {
            for (k in original.keys()) {
                val key = k as String?
                if (`object`.hasKey(key)) {
                    if (original[key] is JSONObject) {
                        traverseJSON(original[key] as JSONObject, `object`[key] as JSONObject)
                    } else {
                        original.put(key, `object`[key])
                    }
                }
            }
            for (k in `object`.keys()) {
                val key = k as String?
                if (original.isNull(key)) {
                    original.put(key, `object`[key])
                }
            }
        }

        //finds and removes unused element names, default english only
        fun validateEnglish() {
            val language = getLanguage("english")!!
            val `object` = language.json.getJSONObject("elements").getJSONObject("alchemy")
            `object`.keys().removeIf { e: Any -> getElement("alchemy:$e") == null }
            Main.saveJSONObject(language.json, "resources/languages/english.json", "indent=4")
        }
    }
}