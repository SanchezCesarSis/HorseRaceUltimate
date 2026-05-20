/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package horseraceultimate;

/**
 *
 * @author Sanchez Herrera Cesar Antonio
 */

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.jogamp.opengl.GLProfile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RaceRenderer extends GLJPanel implements GLEventListener {

    // Constantes
    static final int NUM_HORSES = 20;
    static final float RACE_DISTANCE = 80f;
    static final float LANE_WIDTH = 2.5f;
    static final float TRACK_LENGTH = 100f;
    static final int FPS = 60;

    // Colores
    private static final float[][] horseColors = {
        {0.8f,0.5f,0.2f}, {0,0,0}, {0.9f,0.9f,0.9f}, {0.6f,0.3f,0.1f}, {0.4f,0.2f,0},
        {0.2f,0.2f,0.2f}, {0.9f,0.8f,0.6f}, {0.7f,0.7f,0.7f}, {0.8f,0.4f,0.4f}, {0.5f,0.3f,0.2f}
    };

    // Estado de carrera
    private Horse[] horses = new Horse[NUM_HORSES];
    private boolean preRaceDelay = false;
    private long preRaceStartTime = 0;
    boolean raceStarted = false;
    boolean raceFinished = false;
    int winnerIdx = -1;
    int[] selectedHorseIdx;
    private RaceListener listener;  

    // OpenGL
    private GLU glu;
    private GLUT glut;
    private Texture grassTexture, fenceTexture, skyTexture;
    private float[] lightPos = {0,15,40,1};
    private float[] lightAmb = {0.4f,0.4f,0.4f,1};
    private float[] lightDif = {0.9f,0.9f,0.8f,1};
    private float[] lightSpec = {1,1,1,1};

    public interface RaceListener {
        void onRaceStart(); 
        void onRaceOver(int horseNumber);
    }
    

    public RaceRenderer(RaceListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(800, 600));
        addGLEventListener(this);
        for (int i = 0; i < NUM_HORSES; i++) {
            horses[i] = new Horse(i + 1, horseColors[i % horseColors.length]);
        }
    }

    public void startRace(int[] selected) {
        selectedHorseIdx = selected;
        for (Horse h : horses) {
            h.reset();
            h.raceStarted = false;
            h.raceFinished = false;
        }
        preRaceDelay = true;
        preRaceStartTime = System.currentTimeMillis();
        raceStarted = false;
        raceFinished = false;
        winnerIdx = -1;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();
        gl.glClearColor(0,0,0,1);
        gl.glClearDepth(1);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDif, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpec, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);

        grassTexture = loadTexture("textures/grass.jpg");
        fenceTexture = loadTexture("textures/fence.jpg");
        skyTexture   = loadTexture("textures/sky.jpg");
    }

    private Texture loadTexture(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            return AWTTextureIO.newTexture(GLProfile.getDefault(), img, false);
        } catch (IOException e) {
            System.err.println("Falta textura: " + path);
            return null;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        if (h == 0) h = 1;
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, (float)w/h, 1.0, 200.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        drawSky(gl);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0,20,-20, 0,0,40, 0,1,0);

        drawGround(gl);
        drawFences(gl);

        if (preRaceDelay && !raceFinished) {
            if (System.currentTimeMillis() - preRaceStartTime >= 16000) {
                raceStarted = true;
                preRaceDelay = false;
                for (Horse h : horses) h.raceStarted = true;
                SwingUtilities.invokeLater(() -> listener.onRaceStart());
            }
        }

        // Mover y dibujar
        for (int i = 0; i < NUM_HORSES; i++) {
            horses[i].advance();
            float x = -((NUM_HORSES-1) * LANE_WIDTH) / 2f + i * LANE_WIDTH;
            drawHorse(gl, horses[i].getColor(), x, 0.1f, horses[i].getPosZ());
        }

        // Ganador
        if (!raceFinished && raceStarted) {
            for (int i = 0; i < NUM_HORSES; i++) {   // ← NUM_HORSES con guión
                if (horses[i].getPosZ() >= RACE_DISTANCE) {
                    raceFinished = true;
                    winnerIdx = i;
                    raceStarted = false;
                    for (Horse h : horses) h.raceFinished = true;
                    final int winnerNumber = horses[i].getNumber();
                    SwingUtilities.invokeLater(() -> listener.onRaceOver(winnerNumber));
                    break;
                }
            }
        }

        drawLaneNumbers(gl);
    }

    @Override public void dispose(GLAutoDrawable d) {}

    // Métodos de dibujo 
    private void drawSky(GL2 gl) {
        gl.glPushAttrib(GL2.GL_TEXTURE_BIT);
        gl.glPushMatrix();
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(-1,1,-1,1,-1,1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        if (skyTexture != null) {
            skyTexture.bind(gl);
            skyTexture.enable(gl);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            gl.glColor3f(1,1,1);
        } else {
            gl.glColor3f(0.53f,0.81f,0.98f);
        }

        float repX = 2, repY = 2;
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, repY);   gl.glVertex3f(-1,-1,0);
        gl.glTexCoord2f(repX, repY); gl.glVertex3f( 1,-1,0);
        gl.glTexCoord2f(repX, 0);    gl.glVertex3f( 1, 1,0);
        gl.glTexCoord2f(0, 0);       gl.glVertex3f(-1, 1,0);
        gl.glEnd();

        if (skyTexture != null) skyTexture.disable(gl);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glDepthMask(true);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopAttrib();
    }

    private void drawGround(GL2 gl) {
        gl.glDisable(GL2.GL_LIGHTING);
        if (grassTexture != null) {
            grassTexture.bind(gl);
            grassTexture.enable(gl);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
            gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            gl.glColor3f(1,1,1);
        } else {
            gl.glColor3f(0.3f,0.8f,0.3f);
        }
        float ancho = NUM_HORSES * LANE_WIDTH + 10f;
        float largo = TRACK_LENGTH;
        float repX = ancho / 5f;
        float repZ = largo / 5f;
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0,0);          gl.glVertex3f(-ancho/2,0,0);
        gl.glTexCoord2f(repX,0);       gl.glVertex3f( ancho/2,0,0);
        gl.glTexCoord2f(repX,repZ);    gl.glVertex3f( ancho/2,0,largo);
        gl.glTexCoord2f(0,repZ);       gl.glVertex3f(-ancho/2,0,largo);
        gl.glEnd();
        if (grassTexture != null) grassTexture.disable(gl);
        gl.glEnable(GL2.GL_LIGHTING);
    }

    private void drawFences(GL2 gl) {
        if (fenceTexture != null) {
            fenceTexture.bind(gl);
            fenceTexture.enable(gl);
        }
        gl.glColor3f(0.6f, 0.4f, 0.2f);
        float ancho = NUM_HORSES * LANE_WIDTH + 4f;
        for (int lado = -1; lado <= 1; lado += 2) {
            float x = lado * ancho / 2f;
            gl.glBegin(GL2.GL_QUADS);
            for (int i = 0; i < TRACK_LENGTH; i += 2) {
                float z = i;
                gl.glTexCoord2f(0, 0); gl.glVertex3f(x-0.1f, 0, z);
                gl.glTexCoord2f(1, 0); gl.glVertex3f(x+0.1f, 0, z);
                gl.glTexCoord2f(1, 1); gl.glVertex3f(x+0.1f, 1.5f, z);
                gl.glTexCoord2f(0, 1); gl.glVertex3f(x-0.1f, 1.5f, z);
            }
            gl.glEnd();
        }
        if (fenceTexture != null) fenceTexture.disable(gl);
    }

    private void drawHorse(GL2 gl, float[] color, float x, float y, float z) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);

        // Parámetros de animación
        float ciclo = z * 3.0f;
        float reboteCuerpo = (float) Math.sin(ciclo) * 0.12f;
        float balanceoCabeza = (float) Math.sin(ciclo) * 0.08f;
        float fasePatas = (float) Math.sin(ciclo);

        gl.glScalef(0.7f, 0.7f, 1.0f);
        setMaterial(gl, color);

        // CUERPO 
        gl.glPushMatrix();
        gl.glTranslatef(0, 0.9f + reboteCuerpo, 0);
        gl.glScalef(0.5f, 0.35f, 0.9f);  
        glut.glutSolidCube(1.0f);
        gl.glPopMatrix();

        // PECHO 
        gl.glPushMatrix();
        gl.glTranslatef(0, 0.75f + reboteCuerpo, 0.55f);
        gl.glScalef(0.45f, 0.45f, 0.3f);
        glut.glutSolidCube(1.0f);
        gl.glPopMatrix();

        // CUELLO 
        gl.glPushMatrix();
        gl.glTranslatef(0, 1.15f + reboteCuerpo, 0.45f);
        gl.glRotatef(-20 + balanceoCabeza * 8, 1, 0, 0);
        gl.glScalef(0.2f, 0.4f, 0.2f);
        glut.glutSolidCube(1.0f);
        gl.glPopMatrix();

        // CABEZA 
        gl.glPushMatrix();
        gl.glTranslatef(0, 1.45f + reboteCuerpo + balanceoCabeza, 0.7f);
        gl.glRotatef(balanceoCabeza * 5, 1, 0, 0);
        gl.glScalef(0.3f, 0.25f, 0.35f);
        glut.glutSolidCube(1.0f);
        gl.glPopMatrix();

        // HOCICO 
        gl.glPushMatrix();
        gl.glTranslatef(0, 1.38f + reboteCuerpo + balanceoCabeza, 0.9f);
        gl.glScalef(0.2f, 0.18f, 0.2f);
        glut.glutSolidCube(1.0f);
        gl.glPopMatrix();

        // OREJAS 
        float[] earX = {-0.1f, 0.1f};
        for (float ex : earX) {
            gl.glPushMatrix();
            gl.glTranslatef(ex, 1.6f + reboteCuerpo + balanceoCabeza, 0.65f);
            gl.glScalef(0.08f, 0.2f, 0.08f);
            glut.glutSolidCube(1.0f);
            gl.glPopMatrix();
        }

        // PATAS 
        float[] pX = {-0.18f, 0.18f};
        float[] pZ = {-0.3f, 0.35f};

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                float fase = (float) ((j == 0) ? 
                        ((i == 0) ? ciclo : ciclo + Math.PI) :
                        ((i == 0) ? ciclo + Math.PI : ciclo));
                float despPata = (float) Math.sin(fase) * 0.2f;

                gl.glPushMatrix();
                gl.glTranslatef(pX[i], 0.25f, pZ[j] + despPata);
                gl.glRotatef(despPata * 20, 1, 0, 0);
                gl.glScalef(0.12f, 0.45f, 0.12f);
                glut.glutSolidCube(1.0f);
                gl.glPopMatrix();
            }
        }

        // COLA 
        gl.glPushMatrix();
        gl.glTranslatef(0, 0.85f + reboteCuerpo, -0.55f);
        gl.glRotatef(-30 + (float)Math.sin(ciclo * 0.7f) * 10, 1, 0, 0);
        gl.glScalef(0.1f, 0.35f, 0.1f);
        glut.glutSolidCube(1.0f);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }
    
    private void setMaterial(GL2 gl, float[] color) {
        float[] amb = {color[0]*0.3f, color[1]*0.3f, color[2]*0.3f, 1f};
        float[] dif = {color[0], color[1], color[2], 1f};
        float[] spec = {0.5f, 0.5f, 0.5f, 1f};
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, amb, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, dif, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, spec, 0);
        gl.glMateriali(GL.GL_FRONT, GL2.GL_SHININESS, 32);
    }

    private void drawLaneNumbers(GL2 gl) {
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(1f, 1f, 0f);
        for (int i = 0; i < NUM_HORSES; i++) {
            float x = -((NUM_HORSES - 1) * LANE_WIDTH) / 2f + i * LANE_WIDTH;
            gl.glRasterPos3f(x, 2.5f, horses[i].posZ);
            glut.glutBitmapString(GLUT.BITMAP_HELVETICA_12, String.valueOf(i + 1));
        }
        gl.glEnable(GL2.GL_LIGHTING);
    }
}
