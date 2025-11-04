/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GameGFX;
import mariotest.*;
import java.awt.Dimension;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author LENOVO
 */
public class Ventana {
    private JFrame frame;
    private Dimension size; // continen un ancho y una altura
    
    public Ventana(int width , int height , String titulo , Mariotest game){
        size = new Dimension (width,height);
        frame = new JFrame(titulo);
        try { 
            String iconPath = "/Imagenes/icono.png";

            URL iconURL = getClass().getResource(iconPath);

            if (iconURL != null) {
                ImageIcon logo = new ImageIcon(iconURL);
                frame.setIconImage(logo.getImage());
            } 
        } catch (Exception e) {
            System.err.println("Error al cargar el icono: " + e.getMessage());
        }
        
        frame.setPreferredSize(size);
        frame.setMaximumSize(size);
        frame.setMinimumSize(size);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.add(game);
        frame.setVisible(true);
    }
}
