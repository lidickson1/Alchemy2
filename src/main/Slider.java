package main;

import g4p_controls.G4P;
import g4p_controls.GCustomSlider;

public class Slider extends GCustomSlider {

    public Slider(Main main) {
        super(main, 0, 0, 600, 50, null);

        this.setLocalColor(2, main.color(255));
        this.setLocalColor(12, main.color(255));
        this.setLocalColor(7, main.color(0));
        this.setLocalColor(14, main.color(0));

        this.setShowDecor(false, true, false, true);
        this.setNumberFormat(G4P.DECIMAL, 2);
        this.setLimits(0f, 1.0f);
        this.setShowValue(false);
    }

}
