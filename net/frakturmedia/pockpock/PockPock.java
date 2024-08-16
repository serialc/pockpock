// PockPock.java
// main class

package net.frakturmedia.pockpock;

import java.awt.*;
import javax.swing.*;
import java.util.*; //Random, etc.
import java.awt.image.*;
import java.awt.event.*;
import java.net.*;

public class PockPock extends JFrame implements Runnable, KeyListener, MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 19991231235959L;

    static int ScreenWidth = 840;
    static int ScreenHeight = 525;

    Thread gameloop;
    Random rand = new Random();

    //double buffer objects
    BufferedImage backbuffer;
    Graphics2D g2d;

    //background images
    private Image title_image;
    private Image game_background;
    private Image game_instructions;
    
    //game speed
    private int FPS = 10;
    private int PAUSE = 1000/FPS;
    //Grass objects
    private int GRASS_COUNT = 150;
    //private int CHICKEN_COUNT = 100;
    private int CHICKEN_COUNT = 100;
    private Grass[] grasses = new Grass[GRASS_COUNT];
    private Chicken[] chickens = new Chicken[CHICKEN_COUNT];
    private int chicken_counter;

    //collision constant
    private int COLLISION_THRESHHOLD = 50;

    //drag+drop
    private Point old_drag_coord;
    private Chicken dragged_chick;

    //Game sound
    private Sounds sounds;

    // Game networking for highscores
    private scoreConnection hscon = new scoreConnection();
    private String[][] hsdata;
    private String hsname = "chicken";
    private int hssubmit = 0;
    
    //game logic elements
    private int game_state;
    private int game_time;
    private float high_score;
    private float player_score;

    public static void main(String[] args) {
        new PockPock();
    }

    public PockPock() {
        //INITIALIZATION
        //set title bar of game
        super("Pock Pock Chicken Game");

        game_state = 0;
        game_time = 0;
        high_score = -1;

        //generic init
        setSize(ScreenWidth, ScreenHeight);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //create back buffer for smooth frame transition
        backbuffer = new BufferedImage(ScreenWidth, ScreenHeight, BufferedImage.TYPE_INT_RGB);
        g2d = backbuffer.createGraphics();

        //loading background images
        title_image = load("resources/pockpock_title.png");
        game_background = load("resources/background.png");
        game_instructions = load("resources/instructions.png");

        //prep sounds
        sounds = new Sounds();

        //add mouse listener
        addMouseListener(this);
        addMouseMotionListener(this);
 
        //add keyboard listener
        addKeyListener(this);
        //game loop
        gameloop = new Thread(this);
        gameloop.start();
    }

    public void run() {
        Thread t = Thread.currentThread();

        while (t == gameloop) {
            try {
                Thread.sleep(PAUSE);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            switch (game_state) {
                case 0:
                    showTitleScreen();
                    break;
                case 1:
                    showInstructions();
                    break;
                case 2:
                    initializeGame();
                    break;
                case 3:
                    gameLoop();
                    break;
                case 99:
                    gameOver();
                    break;
                case 100:
                    retrieveHighScore();
                    break;
                case 101:
                    //we are not repainting the canvas
                    break;
            }
        }
    }

    private void showTitleScreen() {
        g2d.drawImage(title_image, 0, 0, ScreenWidth -1 , ScreenHeight - 1, this); 
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Verdana", Font.BOLD, 25));
        g2d.drawString("Click to begin game", 250, 300);
        repaint();
    }

    private void showInstructions() {
        g2d.drawImage(game_instructions, 0, 0, ScreenWidth - 1, ScreenHeight - 1, this);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Verdana", Font.BOLD, 20));
        g2d.drawString("Game by Cyrille Medard de Chardon", 190, 420);
        g2d.drawString("Sound effects by:", 190, 440);
        g2d.drawString("Gaelle & Noellie Medard de Chardon", 190, 460);
        g2d.setFont(new Font("Verdana", Font.BOLD, 28));
        g2d.drawString("Credits", 190, 380);
        g2d.drawString("Click to start", 40, 180);
        g2d.drawString("the game", 40, 210);
        repaint();
    }

    private void initializeGame() {
        //CREATE GRASS AND CHICKENS

        //initialize game time to 0
        game_time = 0;
        hssubmit = 0;

        //create grass sprites 
        for (int i = 0; i < GRASS_COUNT; i += 1) {
            grasses[i] = new Grass(this, g2d, ScreenWidth, ScreenHeight);
            //all tiles will be 50x50
            grasses[i].setCoord(rand.nextInt(ScreenWidth - 50) + 25, rand.nextInt(ScreenHeight - 70) + 45);
            grasses[i].setAlive();
        }

        //create chicken sprites
        for (int i = 0; i < CHICKEN_COUNT; i += 1) {
            chickens[i] = new Chicken(this, g2d, ScreenWidth, ScreenHeight, i);
            //all chickens are initialized as being in the centre of the screen
            chickens[i].setCoord(ScreenWidth / 2, ScreenHeight / 2);
        }
        
        //create first chicken and increment chicken counter
        chicken_counter = 0;
        chickens[chicken_counter].setAlive();
        chicken_counter += 1;

        //run the gameLoop during next cycle
        game_state = 3;
    }

    // game state 99
    private void gameOver() {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(ScreenWidth / 2 - 125, ScreenHeight / 2 - 40 , 250, 130);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Verdana", Font.PLAIN, 25));
        g2d.drawString("Game over!", ScreenWidth / 2 - 80, ScreenHeight / 2);
        
        // show player his score
        player_score = game_time / (float)FPS;
        g2d.drawString("Score: " + player_score, ScreenWidth / 2 - 80, ScreenHeight / 2 + 30);

        //get high scores data
        hsdata = hscon.getSetHighScores("");

        //check if a new high score
        if ( high_score < 0 || (high_score > 0 && player_score < high_score)) {
            //make exciting text about high score
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Verdana", Font.PLAIN, 14));
            g2d.drawString("New session best score!", ScreenWidth / 2 - 90, ScreenHeight / 2 + 45);

            //set new high score
            high_score = player_score;
        } 

        //Play again? Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Verdana", Font.PLAIN, 14));
        g2d.drawString("(Click screen to play again)", ScreenWidth / 2 - 100, ScreenHeight / 2 + 60);
        repaint();

        game_state = 100;

        //whipe any keys that have been pressed up until now
        hsname = "";
    }

    // game state 100
    private void retrieveHighScore() {

        //black area for text
        g2d.setFont(new Font("Verdana", Font.PLAIN, 14));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(ScreenWidth - 200, ScreenHeight / 4, 175, 250);
        g2d.setColor(Color.WHITE);

        // do networking of high score, hsdata holds data
        if(hsdata.length < 2) {
            g2d.drawString("Unable to connect to high score server - sorry!", ScreenWidth - 200, ScreenHeight / 4);
        } else {
            // Connection successfull

            // Show all high scores
            g2d.drawString("Global high scores", ScreenWidth - 190, ScreenHeight / 4 + 20);
            for( int i = hsdata.length - 1; i >= 0; i -= 1 ) {
                g2d.drawString(hsdata[i][0], ScreenWidth - 180, 10 + ScreenHeight / 4 + ( 2 + i ) * 20);
                g2d.drawString(hsdata[i][1], ScreenWidth - 90, 10 + ScreenHeight / 4 + ( 2 + i ) * 20);
            }

            //array holds lower scores to higher scores, indexed 0-9
            // see if player's score is sufficient to go on high score board
            if(Float.parseFloat(hsdata[hsdata.length - 1][1]) > player_score) {

                if( hssubmit == 0 ) {
                    hssubmit = 1;
                }

                //score needs to be submitted, ask for name
                g2d.setColor(Color.BLACK);
                g2d.fillRect(185, 70, 420, 80);
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Verdana", Font.PLAIN, 18));
                g2d.drawString("Global high score! Please enter your name:", 200, 100);
                g2d.setColor(Color.BLUE);
                g2d.drawString(hsname, 200, 130);
            }
        }

        //upload high score
        if( hssubmit == 2) {
            String high_score_string = String.format("%.1f", player_score);
            hsdata = hscon.getSetHighScores(hsname + "-" + high_score_string);
            hssubmit = 3;
        }

        repaint();
    }

    public void gameLoop() {

        //GAME PHYSICS/LOGIC

        //increment game time
        game_time += 1;

        boolean game_over = true;
        //if there is no more grass, the game is over
        for (int j = 0; j < GRASS_COUNT; j += 1) {
            if (grasses[j].isAlive()) {
                game_over = false;
                break;
            }
        }
        
        //check how many chickens have been made
        if (chicken_counter == CHICKEN_COUNT) {
            game_over = true;
        }

        //if either time is up or there remains no grass, the game is over
        if(game_over) {
            game_state = 99;
            //quit game loop
            //System.out.println("Game over!");
            return;
        }

        //chicken looks for closest grass
        //overly large number
        int min_dist = ScreenWidth + ScreenHeight;
        int target_grass_id;

        for (int i = 0; i < CHICKEN_COUNT; i += 1) {
            if (chickens[i].isAlive()) {
                int ch_state = chickens[i].getState();

                switch ( ch_state ) {
                    case 0:
                        //not alive, should not even reach here
                        System.out.println("ERROR: A dead chicken is being processed.");
                        break;
                    case 1:
                        //looking for closest grass/seeds and set as target
                        //chicken should move towards closest grass
                        //System.out.println("Searching.");
                        double[] results = findClosestGrass(i);
                        if ( chickens[i].seeGrass(results[1]) ) {
                            results = chickens[i].getDesiredLocation(grasses[(int)results[0]].getX(), grasses[(int)results[0]].getY(), (int)results[0], results[1]);

                            boolean collision = false;

                            //check for collision (check distance) with other chickens
                            for (int c = 0; c < CHICKEN_COUNT; c += 1) {
                                if (chickens[c].isAlive()) {
                                    if ( c != i ) {
                                        
                                        //calculate current distance
                                        double cur_dist, future_dist;
                                        double cx, cy, ix, iy;
                                        cx = chickens[c].getX();
                                        cy = chickens[c].getY();
                                        ix = chickens[i].getX();
                                        iy = chickens[i].getY();

                                        cur_dist = Math.sqrt(Math.pow(cx - ix, 2) + Math.pow(cy - iy, 2));

                                        future_dist = Math.sqrt(Math.pow(cx - results[0], 2) + Math.pow(cy - results[1], 2));

                                        //if they are moving closer to anothr chicken and it will be within collision distance, register collision
                                        if ( future_dist < cur_dist && future_dist < COLLISION_THRESHHOLD && chickens[c].getPeckOrder() < chickens[i].getPeckOrder()) {
                                            collision = true;
                                        }
                                    }
                                }
                            }

                            //move chicken or not depending on whether it will cause a collision
                            if (collision) {
                                    //you should stay and wait, sit down!
                                    chickens[i].setSpriteFrame(7);
                            } else {
                                chickens[i].setCoord(results[0], results[1]);
                            }
                        }
                        break;
                    case 2:
                        //System.out.println("Feeding.");
                        Grass grass_being_eaten = grasses[chickens[i].getEatingGrassId()];

                        if ( grass_being_eaten.isAlive() ) {
                            //animate chicken eating and check if the chicken is still hungry
                            //if it is no longer hungry, transition to nesting
                            chickens[i].eatGrass();

                            //decrease life of grass by 1
                            grass_being_eaten.eatGrass();
                        } else {
                            //food dissapeared before nesting, search for more food
                            chickens[i].decState();
                        }

                        break;
                    case 3:
                        //System.out.println("Nesting.");
                        if (chickens[i].nestEgg()) {
                            //finished nesting
                            //create new sprite at chicken state
                            chickens[chicken_counter].setAlive();
                            //initialize new chicken(chick)
                            chickens[chicken_counter].setState(4);

                            chickens[chicken_counter].setCoord(chickens[i].getX(), chickens[i].getY());

                            //Make gack gack sound
                            sounds.Gack();

                            //increment chicken counter
                            chicken_counter += 1;

                            //check if there are too many chickens
                            if(chicken_counter >= CHICKEN_COUNT) {
                                game_state = 99;
                                return;
                            }

                        }
                        break;
                    case 4:
                        //System.out.println("Empty nest with egg");
                        if (chickens[i].emptyNest()) {
                            sounds.Peep();
                        }
                        break;
                    case 5:
                        //System.out.println("Chick.");
                        chickens[i].beChick();
                        break;
                }
            }
        }

        //GAME GRAPHICS
        
        //background
        g2d.setColor(Color.BLACK);
        //g2d.fill( new Rectangle(0, 0, ScreenWidth, ScreenHeight) );
        g2d.drawImage(game_background, 0, 0, ScreenWidth -1 , ScreenHeight - 1, this); 
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Verdana", Font.BOLD, 15));
        g2d.drawString("Time: " + (game_time / FPS ), 10, 40);

        //show highscore
        if ( high_score > 0 ) {
            String high_score_string = String.format("%.1f", high_score);
            g2d.drawString("Best score: " + high_score_string, ScreenWidth - 150, 40);
        }


        //draw grass
        for (int i = 0; i < GRASS_COUNT; i += 1) {
            if (grasses[i].isAlive()) {
                grasses[i].draw();
            }
        }

        Arrays.sort(chickens);

        //draw chicken(s)
        //NEED TO SORT BY Y POSTION (largest to smallest)
        //**********************************************
        for (int i = 0; i < CHICKEN_COUNT; i += 1) {
            if (chickens[i].isAlive()) {
                chickens[i].draw();
            }
        }

        repaint();
    }

    public double[] findClosestGrass(int chicken_id) {

        double gx, gy, cx, cy;
        int target_grass_id = 0;
        double min_dist = ScreenWidth + ScreenHeight;

        for (int j = 0; j < GRASS_COUNT; j += 1) {
            if (grasses[j].isAlive()) {
                //get location of grass and chicken
                gx = grasses[j].getX();
                gy = grasses[j].getY();

                cx = chickens[chicken_id].getX();
                cy = chickens[chicken_id].getY();

                double dist = Math.sqrt(Math.pow(gx - cx, 2) + Math.pow(gy - cy, 2));

                if (dist < min_dist) {
                    min_dist = dist;
                    target_grass_id = j;
                }
            }
        }

        double[] results = {target_grass_id, min_dist};
        return results;
    }

    public void paint(Graphics g) {
        //draw the backbuffer to the screen
        g.drawImage(backbuffer, 0, 0, this);
    }

    public void mousePressed(MouseEvent e) {
        switch (game_state) {
            case 0:
                game_state = 1;
                break;

            case 1:
                game_state = 2;
                break;

            case 3:
                //check each chicken
                for ( int c = 0; c < CHICKEN_COUNT; c += 1 ) {
                    //check if chick
                    if ( chickens[c].getState() == 5 ) {
                        //chick if click is inside
                        if ( chickens[c].getBounds().contains( e.getX(), e.getY() ) ) {
                            //System.out.println(e.getX() + " " + e.getY());
                            dragged_chick = chickens[c];
                            //freeze growth
                            dragged_chick.setFreeze();
                            old_drag_coord = new Point( e.getX(), e.getY() );
                        }
                    }
                }
                break;

            case 100:
                game_state = 1;
                break;
        }
    }

    //handles typing
    public void keyReleased(KeyEvent k) {}
    public void keyTyped(KeyEvent k) {}
    public void keyPressed(KeyEvent k) {
        int keyVal = k.getKeyCode();
        char keyChar = k.getKeyChar();

        // Allow the submission only if this is a high score
        if(hssubmit == 1) {

            //only capture a-zA-Z
            if(keyVal >= 65 && keyVal <= 90) {
                hsname += keyChar;
            }
            
            //backspace
            if(keyVal == 8) {
                hsname = hsname.substring(0, hsname.length()-1);
            }

            //return
            if(keyVal == 10) {
                hssubmit = 2;
            }
            //System.out.println(hsname + "-" + keyChar + "-" + keyVal);
        }
    }

    //handles mouse interaction
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {
        if ( dragged_chick != null ) {
            dragged_chick.unsetFreeze();
            dragged_chick = null;
        }
    }
    public void mouseDragged(MouseEvent e) {
        //determine difference in location since initial click
        if ( dragged_chick != null ) {
            dragged_chick.incCoord(e.getX() - old_drag_coord.x, e.getY() - old_drag_coord.y);
            old_drag_coord = new Point( e.getX(), e.getY() );
        }
    }
    public void mouseMoved(MouseEvent e) {}

    private URL getURL(String filename) {
        URL url = null;
        try {
            url = this.getClass().getResource(filename);
        }
        catch (Exception e) {}
        return url;
    }

    public Image load(String image_path) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.getImage(getURL(image_path));
    }
} 
