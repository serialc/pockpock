// Grass.java
// depicts a grass sprite

package net.frakturmedia.pockpock;

import java.awt.*;
import javax.swing.*;

public class Grass extends Sprite {

    private int grass_life = 8;

    Grass(JFrame a, Graphics2D g, int screenW, int screenH) {
        //pass the call to the superclass Sprite
        //call to super() must be first in constructor
        super(a, g, screenW, screenH);

        //only load image once
        if(image == null) {
            load();
            //System.out.println("Loading Sprite images.");
        }

        setSpriteType("grass");

    }


    public void eatGrass() {
        grass_life -= 1;

        if ( grass_life <= 0 ) {
            setDead();
        }
    }
}
