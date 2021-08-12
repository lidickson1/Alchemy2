package main;

import g4p_controls.GTextField;

import java.awt.*;

public class TextField extends GTextField {

    public TextField(Main main, float width, float height) {
        super(main, 0, 0, width, height);

        this.setLocalColor(2, main.color(255));
        this.setLocalColor(12, main.color(255));
        this.setLocalColor(7, main.color(0));
        this.setLocalColor(14, main.color(0));
        this.setFont((Font) main.getFont().getNative());
    }

}
