//Sounds.java
//handles sounds for game

package net.frakturmedia.pockpock;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class Sounds {
    //audio data source stream
    private AudioInputStream peep;
    private AudioInputStream gack;

    //the clips
    private Clip sound_buffer1, sound_buffer2;

    //constructor
    public Sounds() {
        try {
            //create a sound buffer
            sound_buffer1 = AudioSystem.getClip();
            sound_buffer2 = AudioSystem.getClip();
        } catch (LineUnavailableException e) {}
        
        try {
            //set teh audio stream source
            peep = AudioSystem.getAudioInputStream(getURL("resources/peep_peep.wav"));
            gack = AudioSystem.getAudioInputStream(getURL("resources/gack_gack.wav"));

            //load audio files
            sound_buffer1.open(peep);
            sound_buffer2.open(gack);

            //catch potential errors
        } catch (IOException e) {
        } catch (UnsupportedAudioFileException e) {
        } catch (LineUnavailableException e) {}
    }

    public void Peep() {
        if ( (boolean)(peep!= null) ) {
            sound_buffer1.setFramePosition(0);
            sound_buffer1.start();
        }
    }
    public void Gack() {
        if ( (boolean)(gack != null) ) {
            sound_buffer2.setFramePosition(0);
            sound_buffer2.start();
        }
    }

    private URL getURL(String filename) {
        URL url = null;
        try {
            url = this.getClass().getResource(filename);
        } catch (Exception e) {}
        return url;
    }
}
