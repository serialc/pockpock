// Sprite.java
// A basic game entity

package net.frakturmedia.pockpock;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.awt.Rectangle;
import java.awt.geom.*; //for AffineTransform
import java.util.Random;

public class Sprite extends Object {

    //CHANGES TO DO
    //-I should just load 1 sprite with all the graphics
    // and display the appropriate ones based on state -> row/col
    protected Graphics2D g2d;
    protected JFrame frame;
    protected static Image image;
    protected int state;
    private AffineTransform identity = new AffineTransform();
    protected int screenW, screenH;

    protected Random rand = new Random();

    private boolean mirrored;
    private int sprite_type;
    private int sprite_frame;
    private double x, y;
    private boolean alive;
    private int tile_size = 50;

    //CONSTRUCTOR
    Sprite(JFrame a, Graphics2D g, int sw, int sh) {
        g2d = g;
        frame = a;
        screenW = sw;
        screenH = sh;
        alive = false;
        mirrored = false;
    }

    public void setCoord(double x, double y) { 
        if ( this.x > x ) {
            mirrored = true;
        } else {
            mirrored = false;
        }
        this.x = x;
        this.y = y;
    }
    public void setCoord(int x, int y) {
        if ( this.x > x ) {
            mirrored = true;
        } else {
            mirrored = false;
        }
        this.x = x;
        this.y = y;
    }
    public void incCoord(double x, double y) { 
        if ( x < 0 ) {
            mirrored = true;
        } else {
            mirrored = false;
        }
        this.x += x;
        this.y += y;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setAlive() { alive = true; }
    public void setDead() { alive = false; }
    public void setLife( boolean life ) { alive = life; }
    public boolean isAlive() { return alive; }
    public void setState(int s) { state = s; }
    public int getState() { return state; }
    public void incState() {
        //STATES
        //0 - not alive
        //1 - travel to food
        //2 - eating
        //3 - nesting
        //4 - egg in nest
        //5 - chick wandering around
        state += 1;
        if ( state > 5 ) {
            state = 1;
        }
    }
    public void decState() {
        state -= 1;
        if ( state <= 0 ) {
            setDead();
            System.out.println("Chicken died!");
        }
    }
    public void setSpriteFrame(int sf) { sprite_frame = sf; }
    public int getSpriteFrame() { return sprite_frame; }
    public void setSpriteType(String type) {
        if ( type == "grass" ) {
            sprite_type = 1;
            sprite_frame = 6;
        }
        if ( type == "chicken" ) {
            incState();
            sprite_type = 2;
            sprite_frame = 0;
        }
    }
    public int getSpriteType() {
        return sprite_type;
    }

    private double getImageXCoord() { return x - (tile_size/2); }
    private double getImageYCoord() { return y - (tile_size/2); } 

    public Rectangle getBounds() {
        Rectangle r;
        r = new Rectangle((int)getX() - 25, (int)getY() - 25, 50, 50);
        return r;
    }

    private URL getURL(String filename) {
        URL url = null;
        try {
            url = this.getClass().getResource(filename);
        }
        catch (Exception e) {}
        return url;
    }

    public void load() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        image = tk.getImage(getURL("resources/pockpock_sprites.png"));
    }
    
    public void load(String image_path) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        image = tk.getImage(getURL("image_path"));
    }

    public void draw() {
        int imgx = (int)getImageXCoord();
        int imgy = (int)getImageYCoord();
        //g2d.setTransform(identity);
        //g2d.scale(0.1,1);
        if (mirrored) {
            //flipped x coords to flip image
            g2d.drawImage(image, imgx + tile_size, imgy, imgx, imgy + tile_size, sprite_frame * 100, 0, (sprite_frame + 1) * 100, 100, frame);
        } else {
            g2d.drawImage(image, imgx, imgy, imgx + tile_size, imgy + tile_size, sprite_frame * 100, 0, (sprite_frame + 1) * 100, 100, frame);
        }
        
        Rectangle r = getBounds();
        //g2d.drawRect(r.x, r.y, r.width, r.height);
    }
}
