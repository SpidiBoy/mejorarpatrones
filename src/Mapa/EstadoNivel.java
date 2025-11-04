package Mapa;
import Objetos.*;
import java.awt.Graphics;
import Objetos.Utilidad.*;
import mariotest.Mariotest;

/**
 * Patrón STATE para manejar estados del nivel
 * 
 */
public abstract class EstadoNivel {
    
    protected Mariotest juego;
    protected GestorNiveles gestorNiveles;
    
    public EstadoNivel(Mariotest juego, GestorNiveles gestorNiveles) {
        this.juego = juego;
        this.gestorNiveles = gestorNiveles;
    }
    
    public abstract void entrar();
    public abstract void tick();
    public abstract void render(Graphics g);
    public abstract void salir();
    
    public abstract boolean permitirMovimientoJugador();
    public abstract boolean permitirSpawnEnemigos();
    
    // ==================== ESTADO: JUGANDO ====================
    
    public static class Jugando extends EstadoNivel {
        
        public Jugando(Mariotest juego, GestorNiveles gestorNiveles) {
            super(juego, gestorNiveles);
        }
        
        @Override
        public void entrar() {
            System.out.println("[ESTADO NIVEL] → JUGANDO");
        }
        
        @Override
        public void tick() {
            if (gestorNiveles.verificarVictoria()) {
                gestorNiveles.cambiarEstado(
                    new Victoria(juego, gestorNiveles)
                );
            }
        }
        
        @Override
        public void render(Graphics g) {
            // Renderizado normal
        }
        
        @Override
        public void salir() {
            System.out.println("[ESTADO NIVEL] JUGANDO → saliendo");
        }
        
        @Override
        public boolean permitirMovimientoJugador() {
            return true;
        }
        
        @Override
        public boolean permitirSpawnEnemigos() {
            return true;
        }
    }
    
    // ==================== ESTADO: VICTORIA ====================

    public static class Victoria extends EstadoNivel {
        
        private int ticksAnimacion;
        private static final int DURACION_ANIMACION = 240; // 4 segundos
        
        // Control de secuencia de animación
        private int faseActual;
        private static final int FASE_CORAZON = 0;
        private static final int FASE_DK_AGARRA = 1;
        private static final int FASE_MOVIMIENTO = 2;
        private static final int FASE_CORAZON_ROTO = 3;
        private static final int FASE_TRANSICION = 4;
        
        // 🔥 FIX: Referencia directa a DK para actualizarlo manualmente
        private DiegoKong diegoKong;
        private Princesa princesa;
        
        public Victoria(Mariotest juego, GestorNiveles gestorNiveles) {
            super(juego, gestorNiveles);
            this.ticksAnimacion = 0;
            this.faseActual = FASE_CORAZON;
        }
        
        @Override
        public void entrar() {
            System.out.println("[ESTADO NIVEL] → VICTORIA");
            
            // Detener spawners
            gestorNiveles.detenerSpawners();
            
            // Detener movimiento del jugador
            if (juego.getHandler().getPlayer() != null) {
                juego.getHandler().getPlayer().detenerMovimiento();
            }
            
            // 🔥 FIX: Obtener referencias a DK y Princesa
            obtenerReferenciasEntidades();
            
            // Iniciar animación de victoria
            gestorNiveles.iniciarAnimacionVictoria();
        }
        
        /**
         * 🔥 NUEVO: Obtiene referencias directas a DK y Princesa
         */
        private void obtenerReferenciasEntidades() {
            Handler handler = juego.getHandler();
            
            for (GameObjetos obj : handler.getGameObjs()) {
                if (obj.getId() == ObjetosID.DiegoKong) {
                    diegoKong = (DiegoKong) obj;
                } else if (obj.getId() == ObjetosID.Princesa) {
                    princesa = (Princesa) obj;
                }
            }
            
            if (diegoKong == null) {
                System.err.println("[VICTORIA] ❌ ERROR: No se encontró DiegoKong");
            }
            if (princesa == null) {
                System.err.println("[VICTORIA] ❌ ERROR: No se encontró Princesa");
            }
        }
        
        @Override
        public void tick() {
            ticksAnimacion++;
            
            // 🔥 FIX CRÍTICO: Actualizar DK y Princesa manualmente durante victoria
            if (diegoKong != null) {
                diegoKong.tick(); // ✅ Esto hace que la animación se ejecute
            }
            
            if (princesa != null && faseActual >= FASE_MOVIMIENTO) {
                princesa.tick(); // ✅ Actualiza movimiento de princesa
            }
            
            // ==================== SECUENCIA DE ANIMACIÓN ====================
            
            // FASE 0: Mostrar corazón (0.5 seg)
            if (ticksAnimacion == 30) {
                faseActual = FASE_CORAZON;
                gestorNiveles.mostrarCorazon();
            } 
            // FASE 1: DK comienza a agarrar (1.5 seg)
            else if (ticksAnimacion == 90) {
                faseActual = FASE_DK_AGARRA;
                gestorNiveles.animarDKAgarraPrincesa();
                
                // 🔥 DEBUG: Verificar estado de DK
                if (diegoKong != null) {
                    System.out.println("[VICTORIA] Diego Kong Estado: " + diegoKong.getEstado());
                } else {
                    System.err.println("[VICTORIA] ❌ diegoKong es NULL en tick 90");
                }
            } 
            // FASE 2: Movimiento visible de princesa hacia DK (2.5 seg)
            else if (ticksAnimacion == 120) {
                faseActual = FASE_MOVIMIENTO;
                System.out.println("[VICTORIA] Iniciando FASE_MOVIMIENTO");
            }
            // FASE 3: Corazón roto (3.5 seg)
            else if (ticksAnimacion == 180) {
                faseActual = FASE_CORAZON_ROTO;
                gestorNiveles.mostrarCorazonRoto();
            }
            
            // Durante la FASE_MOVIMIENTO, mover a DK y Princesa
            if (faseActual == FASE_MOVIMIENTO) {
                gestorNiveles.moverDKYPrincesaHaciaArriba();
            }
            
            // Finalizar animación (4 seg)
            if (ticksAnimacion >= DURACION_ANIMACION) {
                gestorNiveles.cambiarEstado(
                    new Transicion(juego, gestorNiveles)
                );
            }
        }
        
        @Override
        public void render(Graphics g) {
            // Renderizar overlay de victoria
            gestorNiveles.renderOverlayVictoria(g, faseActual, ticksAnimacion);
        }
        
        @Override
        public void salir() {
            System.out.println("[ESTADO NIVEL] VICTORIA → saliendo");
        }
        
        @Override
        public boolean permitirMovimientoJugador() {
            return false;
        }
        
        @Override
        public boolean permitirSpawnEnemigos() {
            return false;
        }
    }
    
    // ==================== ESTADO: TRANSICION ====================
    
    public static class Transicion extends EstadoNivel {
        
        private int ticksTransicion;
        private static final int DURACION_FADE = 60;
        private float alphaFade;
        
        public Transicion(Mariotest juego, GestorNiveles gestorNiveles) {
            super(juego, gestorNiveles);
            this.ticksTransicion = 0;
            this.alphaFade = 0f;
        }
        
        @Override
        public void entrar() {
            System.out.println("[ESTADO NIVEL] → TRANSICION");
        }
        
        @Override
        public void tick() {
            ticksTransicion++;
            alphaFade = Math.min(1f, (float)ticksTransicion / DURACION_FADE);
            
            if (ticksTransicion >= DURACION_FADE) {
                gestorNiveles.cambiarEstado(
                    new CargandoNivel(juego, gestorNiveles)
                );
            }
        }
        
        @Override
        public void render(Graphics g) {
            java.awt.Color colorFade = new java.awt.Color(
                0, 0, 0, (int)(alphaFade * 255)
            );
            g.setColor(colorFade);
            g.fillRect(0, 0, 
                Mariotest.getVentanaWidth(), 
                Mariotest.getVentanaHeight()
            );
        }
        
        @Override
        public void salir() {
            System.out.println("[ESTADO NIVEL] TRANSICION → saliendo");
        }
        
        @Override
        public boolean permitirMovimientoJugador() {
            return false;
        }
        
        @Override
        public boolean permitirSpawnEnemigos() {
            return false;
        }
    }
    
    // ==================== ESTADO: CARGANDO NIVEL ====================
    
    public static class CargandoNivel extends EstadoNivel {
        
        public CargandoNivel(Mariotest juego, GestorNiveles gestorNiveles) {
            super(juego, gestorNiveles);
        }
        
        @Override
        public void entrar() {
            System.out.println("[ESTADO NIVEL] → CARGANDO_NIVEL");
            gestorNiveles.cargarSiguienteNivel();
            gestorNiveles.cambiarEstado(
                new Jugando(juego, gestorNiveles)
            );
        }
        
        @Override
        public void tick() {
            // Se ejecuta solo una vez
        }
        
        @Override
        public void render(Graphics g) {
            g.setColor(java.awt.Color.BLACK);
            g.fillRect(0, 0, 
                Mariotest.getVentanaWidth(), 
                Mariotest.getVentanaHeight()
            );
            
            g.setColor(java.awt.Color.WHITE);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
            String texto = "CARGANDO NIVEL...";
            int x = Mariotest.getVentanaWidth() / 2 - 100;
            int y = Mariotest.getVentanaHeight() / 2;
            g.drawString(texto, x, y);
        }
        
        @Override
        public void salir() {
            System.out.println("[ESTADO NIVEL] CARGANDO_NIVEL → saliendo");
        }
        
        @Override
        public boolean permitirMovimientoJugador() {
            return false;
        }
        
        @Override
        public boolean permitirSpawnEnemigos() {
            return false;
        }
    }
}