package main;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import g4p_controls.G4P;
import main.buttons.Button;
import main.buttons.Element;
import main.buttons.Group;
import main.rooms.Menu;
import main.rooms.*;
import processing.awt.PSurfaceAWT;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main extends PApplet {

    public int screenWidth = 1600;
    public int screenHeight = 900;

    private JFrame jFrame;

    private Minim minim;
    public AudioPlayer backgroundMusic;
    public Thread backgroundMusicThread;

    public DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PFont font;
    public Random random = new Random();
    private PImage icon;

    private Room room;
    public Loading loading;
    public Menu menu;
    public Game game;
    public SaveRoom saveRoom;
    public LoadGame loadGame;
    public SettingsRoom settingsRoom;
    public HistoryRoom historyRoom;
    public ElementRoom elementRoom;
    public PacksRoom packsRoom;
    public Hint hintRoom;

    private JSONObject settings;

    private Language languageSelected;
    private ArrayList<Language> languages = new ArrayList<>();

    public HashMap<Group, HashSet<Element>> groups = new HashMap<>();
    public HashMap<String, Group> elements = new HashMap<>();
    public HashSet<Combo> comboList = new HashSet<>();
    public HashMap<String, ArrayList<ArrayList<String>>> multiComboList = new HashMap<>();

    public static void main(String[] args) {
        PApplet.main("main.Main");
    }

    public void settings() {
        if (new File("resources/settings.json").exists()) {
            this.settings = this.loadJSONObject("resources/settings.json");
        } else {
            this.settings = new JSONObject();
        }
        this.defaultSettings();

        this.size(this.screenWidth, this.screenHeight);
        this.smooth(8);

        Entity.init(this);

        G4P.messagesEnabled(false);
    }

    private void defaultSettings() {
        if (this.settings.isNull("volume")) {
            this.settings.put("volume", 0.5);
        }
        if (this.settings.isNull("music")) {
            this.settings.put("music", true);
        }
        if (this.settings.isNull("sound")) {
            this.settings.put("sound", true);
        }
        if (this.settings.isNull("fullscreen")) {
            this.settings.put("fullscreen", false);
        }
        if (this.settings.isNull("loaded packs")) {
            this.settings.put("loaded packs", new JSONArray().append("Alchemy"));
        }
        if (this.settings.isNull("group colour")) {
            this.settings.put("group colour", false);
        }
    }

    public void setup() {
        this.icon = this.loadImage("resources/images/icon.png");

        this.surface.setTitle("Alchemy");
        this.surface.setIcon(this.icon);
        this.surface.setResizable(true);

        //set minimum size
        PSurfaceAWT.SmoothCanvas sc = (PSurfaceAWT.SmoothCanvas) this.getSurface().getNative();
        this.jFrame = (JFrame) sc.getFrame();
        this.jFrame.setMinimumSize(new Dimension(1280, 720));
        if (this.settings.getBoolean("fullscreen")) {
            //technically it's not maximized because the window icon is still 1 rectangle
            this.jFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        //so the window doesn't appear to be a bit off-screen
        this.jFrame.setLocationRelativeTo(null);

        this.minim = new Minim(this);
        Button.click = this.minim.loadFile("resources/audio/click.mp3");
        Button.click.setGain(this.toGain(this.settings.getFloat("volume")));

        this.font = this.createFont("resources/fonts/Franklin Gothic Book Regular.ttf", 20);
        Font errorFont = null;
        try {
            errorFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/Franklin Gothic Book Regular.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        assert errorFont != null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(errorFont);

        //setting up colors for error dialog
        UIManager.put("OptionPane.background", Color.BLACK);
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("OptionPane.messageFont", new Font(errorFont.getFontName(), Font.PLAIN, 20));
        UIManager.put("OptionPane.buttonFont", new Font(errorFont.getFontName(), Font.PLAIN, 14));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.background", Color.BLACK);
        UIManager.put("Button.select", Color.BLACK); //the background color when you hold and click on the button
        UIManager.put("Button.focus", Color.BLACK); //the border color that shows up when the button is focused
        UIManager.put("Button.border", new BorderUIResource(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10))));

        Language.loadLanguages(this.languages);
        this.languageSelected = Language.getLanguage("english");

        //TODO: make this dynamic?
        ArrayList<String> musicNames = new ArrayList<>(Arrays.asList("Angel Share", "Dreamer", "Easy Lemon", "Frozen Star", "Handbook - Spirits", "Immersed"));

        this.backgroundMusicThread = new Thread(() -> {
            while (true) {
                Collections.shuffle(musicNames);
                for (String name : musicNames) {
                    //to hide the mp3 tag error
                    PrintStream originalStream = System.out;
                    PrintStream dummyStream = new PrintStream(new OutputStream() {
                        public void write(int b) {
                        }
                    });
                    System.setOut(dummyStream);
                    this.backgroundMusic = this.minim.loadFile("resources/audio/" + name + ".mp3");
                    System.setOut(originalStream);

                    //this is normally controlled by SettingsRoom, we need it here so that it can still mute when we first load the game since we are not in SettingsRoom
                    if (!this.settings.getBoolean("music")) {
                        this.backgroundMusic.mute();
                    }

                    this.backgroundMusic.setGain(this.toGain(this.settings.getFloat("volume")));

                    this.backgroundMusic.play();
                    try {
                        Thread.sleep(this.backgroundMusic.length());
                    } catch (InterruptedException ignored) {
                        this.backgroundMusic.close();
                        this.backgroundMusic.unmute();
                    }
                }
            }
        });

        this.backgroundMusicThread.setDaemon(true);
        this.backgroundMusicThread.start();

        this.loading = new Loading();
        this.menu = new Menu();
        this.game = new Game();
        this.saveRoom = new SaveRoom();
        this.loadGame = new LoadGame();
        this.settingsRoom = new SettingsRoom();
        this.historyRoom = new HistoryRoom();
        this.elementRoom = new ElementRoom();
        this.packsRoom = new PacksRoom();
        this.hintRoom = new Hint();
        this.switchRoom(this.loading);
    }

    public float toGain(float value) {
        final int min = -40;
        final int max = 6;
        if (value <= 0.02) {
            return Integer.MIN_VALUE;
        } else {
            return min + value * (max - min);
        }
    }

    ArrayList<Language> getLanguages() {
        return this.languages;
    }

    public Language getLanguageSelected() {
        return this.languageSelected;
    }

    public PImage getIcon() {
        return this.icon;
    }

    public void draw() {
        this.room.draw();

        //updating screen size
        this.screenWidth = this.width;
        this.screenHeight = this.height;
        this.settings.put("fullscreen", this.jFrame.getExtendedState() == Frame.MAXIMIZED_BOTH);
    }

    public void exit() {
        this.saveSettings();
        super.exit();
    }

    public void saveSettings() {
        this.saveJSONObject(this.settings, "resources/settings.json", "indent=4");
    }

    public void switchRoom(Room room) {
        if (this.room != null) {
            this.room.end();
        }
        this.room = room;
        this.room.setup();
    }

    public Room getRoom() {
        return this.room;
    }

    public JSONObject getSettings() {
        return this.settings;
    }

    public void mousePressed() {
        this.room.mousePressed();
    }

    public void keyPressed() {
        //prevents application from closing when esc key is pressed
        if (this.key == 27) {
            this.key = 0;
        }
        this.room.keyPressed();
    }

    public boolean setFontSize(String name, int start, int max) {
        final int threshold = 12; //when font size is <= this we need to display a tooltip
        int i;
        for (i = start; i > 12; i--) {
            this.textFont(this.font, i);
            if (this.textWidth(name) <= max) {
                break;
            }
        }
        return i <= threshold;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(null, message, this.getLanguageSelected().getLocalizedString("misc", "error"), JOptionPane.ERROR_MESSAGE);
    }

}
