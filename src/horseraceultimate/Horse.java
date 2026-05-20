/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package horseraceultimate;

/**
 *
 * @author Sanchez Herrera Cesar Antonio
 */

import java.util.Random;

public class Horse {
    int number;
    float posZ;
    float speed;
    final float[] color;
    boolean raceStarted;
    boolean raceFinished;
    Random rand = new Random();

    public Horse(int number, float[] color) {
        this.number = number;
        this.color = color;
        reset();
    }

    public void reset() {
        posZ = 0f;
        speed = 0.05f + rand.nextFloat() * 0.15f;
    }

    public void advance() {
        if (raceStarted && !raceFinished) {
            posZ += speed;
            speed += (rand.nextFloat() - 0.55f) * 0.01f;
            speed = Math.max(0.02f, Math.min(speed, 0.3f));
        }
    }

    public float getPosZ() { return posZ; }
    public int getNumber() { return number; }
    public float[] getColor() { return color; }
}