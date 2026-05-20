/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package horseraceultimate;

/**
 *
 * @author Sanchez Herrera Cesar Antonio
 */

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    private Clip raceClip;
    private Clip winClip;

    public SoundManager() {
        try {
            raceClip = loadClip("sounds/race.wav");
            winClip  = loadClip("sounds/winner.wav");
        } catch (Exception e) {
            System.err.println("Error cargando sonidos: " + e.getMessage());
        }
    }

    private Clip loadClip(String path) throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path));
        Clip clip = AudioSystem.getClip();
        clip.open(stream);
        return clip;
    }

    public void startRace() {
        if (raceClip != null) {
            raceClip.setFramePosition(0);
            raceClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopRace() {
        if (raceClip != null && raceClip.isRunning()) {
            raceClip.stop();
        }
    }

    public void playWin() {
        if (winClip != null) {
            winClip.setFramePosition(0);
            winClip.start();
        }
    }
}