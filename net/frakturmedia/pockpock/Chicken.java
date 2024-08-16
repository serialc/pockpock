// Chicken.java
// depicts a chicken sprite

package net.frakturmedia.pockpock;

import java.awt.*;
import javax.swing.*;

public class Chicken extends Sprite implements Comparable {
    
    //chicken attributes
    private final int speed = 3;
    private final int VISION_DISTANCE = 150;
    private boolean frozen = false;
    private boolean chicken_steps = true;
    private boolean chicken_eats = true;
    private int PERIOD_TIME = 20;
    private int hunger = PERIOD_TIME;
    private int nest_count = PERIOD_TIME;
    private int nest_egg_count = PERIOD_TIME;
    private int chick_count = PERIOD_TIME * 2;
    private int grass_eating_id;
    private int pecking_order;

    Chicken(JFrame a, Graphics2D g, int screenW, int screenH, int peck_order) {
        //pass the call to the superclass Sprite
        //call to super() must be first in constructor
        super(a, g, screenW, screenH);
        pecking_order = peck_order;
        
        if(image == null) {
            load();
            //System.out.println("Loading Sprite images.");
        }

        setSpriteType("chicken");
    }

    public int getPeckOrder() { return pecking_order; }
    public int getEatingGrassId() { return grass_eating_id; }
    public void setEatingGrassId( int grass_id ) { grass_eating_id = grass_id; }
    public void eatGrass() {
        //grass is existing to eat
        hunger -= 1;

        if ( hunger > 0 ) {
           
            //change animation frame
            if ( chicken_eats ) {
                setSpriteFrame(2);
                chicken_eats = false;
            } else {
                setSpriteFrame(0);
                chicken_eats = true;
            }

        } else {
            //no longer hungry, time to lay an egg
            incState();
            //reset hunger counter
            hunger = PERIOD_TIME;
            //set sprite frame
            setSpriteFrame(3);
        }
    }

    public boolean nestEgg() {

        nest_count -= 1;

        //check if nesting is finished
        if ( nest_count <= 0 ) {
            //finished nesting go back to food hunting
            setState(1);
            setSpriteFrame(1);
            nest_count = PERIOD_TIME;
            return true;
        }

        return false;
        
    }

    public boolean emptyNest() {
        //set sprite frame (repeats but whatever)
        setSpriteFrame(4);

        nest_egg_count -= 1;

        if ( nest_egg_count <= 0 ) {
            nest_egg_count = PERIOD_TIME;
            incState();
            setSpriteFrame(5);
            return true;
        }

        return false;
    }

    public void beChick() {

        //check if it is being dragged around
        if ( !frozen ) {
            //if not increment chick lifespan
            chick_count -= 1;
        }

        if ( chick_count <= 0 ) {
            //chick has matured to chicken
            chick_count = PERIOD_TIME * 2;
            incState();//will loop back to state 1 (chicken)
            setSpriteFrame(1);
        }
    }

    public void setFreeze() {
        frozen = true;
    }
    public void unsetFreeze() {
        frozen = false;
    }
    public boolean seeGrass(double dist) {
        //check if closest grass is further than limit
        if ( dist > VISION_DISTANCE ) {
            //incCoord(rand.nextInt(4) - 2, rand.nextInt(4) - 2); 
            setSpriteFrame(7);
            return false;
        }

        return true;
    }
    
    public double[] getDesiredLocation(double gx, double gy, int grass_id, double dist) {

        double[] target;

        //check if the grass is reachable
        //so distance is < speed
        if ( dist < speed ) {
            //enter eating state
            incState();
            setEatingGrassId(grass_id);

            //set coordinate to that of the grass
            //setCoord(gx, gy);
            target = new double[]{gx, gy};
        } else {
            //determine location to move to
            //move towards grass
            double cx = getX();
            double cy = getY();
            double ratio = speed / (double)dist;

            //change animation frame
            if ( chicken_steps ) {
                setSpriteFrame(1);
                chicken_steps = false;
            } else {
                setSpriteFrame(0);
                chicken_steps = true;
            }

            //incCoord((ratio * (gx - cx)), (ratio * (gy - cy)));
            target = new double[]{(ratio * (gx - cx)) + getX(), (ratio * (gy - cy)) + getY()};
        }
        return target;
    }

    public int compareTo(Object anotherChicken) throws ClassCastException {
        if (!(anotherChicken instanceof Chicken)) {
            throw new ClassCastException("A Chicken object expected.");
        }

        int anotherChickenX = (int) ((Chicken)anotherChicken).getY();  
        return (int)getY() - anotherChickenX;
    }
}
