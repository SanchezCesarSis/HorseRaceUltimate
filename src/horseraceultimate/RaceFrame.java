/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package horseraceultimate;

/**
 *
 * @author Sanchez Herrera Cesar Antonio
 */

import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class RaceFrame extends JFrame implements RaceRenderer.RaceListener {
    private RaceRenderer renderer;
    private JPanel bottomPanel;
    private JPanel selectionPanel, resultPanel;
    private JComboBox<String>[] combos;
    private JLabel resultLabel;
    private CardLayout cardLayout;
    private FPSAnimator animator;
    private SoundManager sound;

    public RaceFrame(int players) {
        setTitle("Hipódromo - Horse Race Ultimate");
        setExtendedState(MAXIMIZED_BOTH);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        try {
            BufferedImage icon = ImageIO.read(new File("textures/horse.jpg"));
            setIconImage(icon);
        } catch (IOException e) {
            System.err.println("No se pudo cargar el ícono.");
        }

        sound = new SoundManager();

        renderer = new RaceRenderer(this);
        add(renderer, BorderLayout.CENTER);

        cardLayout = new CardLayout();
        bottomPanel = new JPanel(cardLayout);
        bottomPanel.setBackground(Color.DARK_GRAY);
        add(bottomPanel, BorderLayout.SOUTH);

        selectionPanel = new JPanel(new FlowLayout());
        selectionPanel.setBackground(Color.WHITE);
        combos = new JComboBox[players];
        for (int i = 0; i < players; i++) {
            selectionPanel.add(new JLabel("Jugador " + (i+1) + ":"));
            combos[i] = new JComboBox<>();
            for (int j = 1; j <= RaceRenderer.NUM_HORSES; j++) combos[i].addItem("Caballo " + j);
            selectionPanel.add(combos[i]);
        }
        JButton startBtn = new JButton("INICIAR CARRERA");
        startBtn.addActionListener(e -> {
            int[] sel = new int[players];
            for (int i = 0; i < players; i++) sel[i] = combos[i].getSelectedIndex();
            renderer.startRace(sel);
            cardLayout.show(bottomPanel, "result");
            resultLabel.setText("¡La carrera está por comenzar...!");
            sound.startRace();
        });
        selectionPanel.add(startBtn);

        resultPanel = new JPanel();
        resultPanel.setBackground(Color.DARK_GRAY);
        resultLabel = new JLabel("Selecciona tus caballos y presiona INICIAR");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 20));
        resultLabel.setForeground(Color.WHITE);
        resultPanel.add(resultLabel);

        JButton restartBtn = new JButton("REINICIAR");
        restartBtn.addActionListener(e -> {
            cardLayout.show(bottomPanel, "selection");
            sound.stopRace();
        });
        JButton menuBtn = new JButton("MENÚ PRINCIPAL");
        menuBtn.addActionListener(e -> {
            animator.stop();
            sound.stopRace();
            dispose();
            new MenuFrame().setVisible(true);
        });
        resultPanel.add(restartBtn);
        resultPanel.add(menuBtn);

        bottomPanel.add(selectionPanel, "selection");
        bottomPanel.add(resultPanel, "result");
        cardLayout.show(bottomPanel, "selection");

        animator = new FPSAnimator(renderer, RaceRenderer.FPS, true);
        animator.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                animator.stop();
                sound.stopRace();
                dispose();
            }
        });
        setVisible(true);
    }
    
    @Override
    public void onRaceStart() {
        resultLabel.setText("¡La carrera ha iniciado!");
    }

    @Override
    public void onRaceOver(int horseNumber) {
        sound.stopRace();
        sound.playWin();

        String msg = "¡El caballo " + horseNumber + " ha ganado!";
        boolean ganaste = false;
        for (int sel : renderer.selectedHorseIdx) {
            if (sel == horseNumber - 1) { ganaste = true; break; }
        }
        msg += ganaste ? " ¡Felicidades, ganaste!" : " Has perdido la carrera";
        resultLabel.setText(msg);
    }
}