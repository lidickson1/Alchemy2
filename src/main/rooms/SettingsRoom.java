package main.rooms;

import main.Slider;
import main.buttons.iconbuttons.Exit;
import main.buttons.iconbuttons.ToggleButton;

import java.io.OutputStream;
import java.io.PrintStream;

public class SettingsRoom extends Room {

    private ToggleButton musicButton;
    private ToggleButton soundButton;
    private ToggleButton groupColour;
    private Exit exit;

    private Slider slider;

    public SettingsRoom() {
        this.musicButton = new ToggleButton("resources/images/music_button.png") {
            @Override
            public void clicked() {
                super.clicked();
                //noinspection StatementWithEmptyBody
                if (this.isToggled()) {
                    main.backgroundMusicThread.interrupt();
                } else {
                    //it will be muted automatically, see SettingsRoom.draw
                }
            }
        };
        this.soundButton = new ToggleButton("resources/images/sound_button.png");
        this.groupColour = new ToggleButton("resources/images/group_colour_button.png");
        this.exit = new Exit();
    }

    @Override
    public void setup() {
        this.musicButton.setToggled(main.getSettings().getBoolean("music"));
        this.soundButton.setToggled(main.getSettings().getBoolean("sound"));
        this.groupColour.setToggled(main.getSettings().getBoolean("group colour"));

        //for hiding the G4P announcement
        PrintStream originalStream = System.out;
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        });
        System.setOut(dummyStream);
        this.slider = new Slider(main);
        this.slider.setValue(main.getSettings().getFloat("volume"));
        System.setOut(originalStream);
    }

    @Override
    public void draw() {
        this.drawTitle("settings", "settings");

        main.textSize(20);
        main.text(main.getLanguageSelected().getLocalizedString("settings", "volume"), main.screenWidth / 2, 200);

        this.slider.moveTo(main.screenWidth / 2F - this.slider.getWidth() / 2, 220);
        this.slider.draw();

        int length = (ToggleButton.SIZE + ToggleButton.GAP) * 3 - ToggleButton.GAP;
        int x = main.screenWidth / 2 - length / 2;
        this.musicButton.draw(x, 400);
        x += ToggleButton.SIZE + ToggleButton.GAP;
        this.soundButton.draw(x, 400);
        x += ToggleButton.SIZE + ToggleButton.GAP;
        this.groupColour.draw(x, 400);

        this.exit.draw();

        //this needs to be updated immediately because background music constantly plays in the background
        main.backgroundMusic.setGain(main.toGain(this.slider.getValueF()));
        if (this.musicButton.isToggled()) {
            main.backgroundMusic.unmute();
        } else {
            main.backgroundMusic.mute();
        }

        //update settings because they should be in effect immediately
        main.getSettings().put("music", this.musicButton.isToggled());
        main.getSettings().put("sound", this.soundButton.isToggled()); //if this wasn't updated here, the button clicks wouldn't respond to this setting
        main.getSettings().put("volume", this.slider.getValueF());
        main.getSettings().put("group colour", this.groupColour.isToggled());
    }

    @Override
    public void end() {
        this.slider.dispose();
    }

    @Override
    public void mousePressed() {
        this.musicButton.mousePressed();
        this.soundButton.mousePressed();
        this.groupColour.mousePressed();
        this.exit.mousePressed();
    }

}
