/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package horseraceultimate;

/**
 *
 * @author Sanchez Herrerra Cesar Antonio
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MenuFrame extends JFrame {
    public MenuFrame() {
        setTitle("Horse Race Ultimate - Menú");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(75, 0, 130));
        setLayout(new BorderLayout());

        JPanel titlePnl = new JPanel();
        titlePnl.setBackground(new Color(75, 0, 130));
        titlePnl.setBorder(new EmptyBorder(30,10,10,10));
        JLabel titleLbl = new JLabel("HORSE RACE ULTIMATE", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Serif", Font.BOLD, 36));
        titleLbl.setForeground(Color.WHITE);
        titlePnl.add(titleLbl);
        add(titlePnl, BorderLayout.NORTH);
        
        try {
            BufferedImage icon = ImageIO.read(new File("textures/horse.jpg"));
            setIconImage(icon);
        } catch (IOException e) {
            System.err.println("No se pudo cargar el ícono.");
        }

        JLabel imgLbl = new JLabel("", SwingConstants.CENTER);
        try {
            BufferedImage img = ImageIO.read(new File("textures/horse.jpg"));
            if (img != null) imgLbl.setIcon(new ImageIcon(img));
            else imgLbl.setText("🐴");
        } catch (IOException e) {
            imgLbl.setText("🐴");
        }
        imgLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
        add(imgLbl, BorderLayout.CENTER);

        // botones
        JPanel btnPnl = new JPanel(new GridLayout(4,1,10,10));
        btnPnl.setBackground(new Color(75, 0, 130));
        btnPnl.setBorder(new EmptyBorder(20,80,30,80));
        btnPnl.add(createBtn("UN JUGADOR", () -> { dispose(); new RaceFrame(1); }));
        btnPnl.add(createBtn("DOS JUGADORES", () -> { dispose(); new RaceFrame(2); }));
        btnPnl.add(createBtn("TRES JUGADORES", () -> { dispose(); new RaceFrame(3); }));
        btnPnl.add(createBtn("SALIR", () -> System.exit(0)));
        add(btnPnl, BorderLayout.SOUTH);
    }

    private JButton createBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(255,215,0));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(Color.ORANGE); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(new Color(255,215,0)); }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }
}