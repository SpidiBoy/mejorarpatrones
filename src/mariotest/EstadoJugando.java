package mariotest;

import Objetos.Utilidad.EstadoJuego;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

/**
 * Estado JUGANDO - El juego está activo
 * 
 * @author LENOVO
 */
public class EstadoJugando extends EstadoJuegoBase {
    
    public EstadoJugando(GestorEstados gestorEstados, Mariotest juego) {
        super(gestorEstados, juego);
    }
    
    @Override
    public void entrar() {
        System.out.println("[ESTADO] Entrando a JUGANDO");
        
        // Reiniciar el juego desde el nivel 1
        if (juego.getGestorNiveles() != null) {
            // Reiniciar estado del juego (puntos, vidas)
            EstadoJuego.getInstance().reiniciar();
            // Cargar nivel 1
            juego.getGestorNiveles().inicializarNivel(1);
        }
    }
    
    @Override
    public void tick() {
        // Verificar si el jugador perdió todas las vidas
        if (EstadoJuego.getInstance().getVidas() <= 0) {
            System.out.println("[ESTADO] ¡GAME OVER! Sin vidas restantes");
            gestorEstados.cambiarEstado(EstadoJuegoEnum.GAME_OVER);
        }
    }
    
    @Override
    public void render(Graphics g) {
        // El renderizado lo maneja Mariotest.render()
        // Este método está vacío porque el juego se dibuja en el loop principal
    }
    
    @Override
    public void salir() {
        System.out.println("[ESTADO] Saliendo de JUGANDO");
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        // Las teclas del juego las maneja Teclas.java
        
        // Solo capturar ESC para pausar/volver al menú
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.out.println("[ESTADO] Volviendo al menú principal...");
            gestorEstados.cambiarEstado(EstadoJuegoEnum.MENU_PRINCIPAL);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Manejado por Teclas.java
    }
}