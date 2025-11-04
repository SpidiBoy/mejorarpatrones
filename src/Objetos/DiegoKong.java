package Objetos;

import GameGFX.Animacion;
import Objetos.Utilidad.Handler;
import Objetos.Utilidad.ObjetosID;
import mariotest.Mariotest;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * 🔥 FIX: DiegoKong con animación de agarrar corregida
 */
public class DiegoKong extends GameObjetos {
    
    private static final float WIDTH = 48;
    private static final float HEIGHT = 32;
    private static final float HEIGHT_AGARRA_PRINCESA = 40; // 48x40
    
    private Handler handler;
    private BufferedImage[] dkSprites;
    
    // Animaciones
    private Animacion dkReposo;
    private Animacion dkAgarra;
    private Animacion dkLanza;
    private Animacion dkGolpeaPecho;
    private Animacion dkAgarraPrincesa;
    
    private Animacion animacionActual;
    
    // Estados
    private EstadoDK estado;
    private boolean mirandoDerecha = true;
    
    // Control de lanzamiento
    private int ticksDesdeUltimoLanzamiento = 0;
    private int ticksEntrelanzamientos = 180;
    private boolean preparandoLanzamiento = false;
    private int ticksAnimacionLanzar = 0;
    private static final int DURACION_ANIMACION_LANZAR = 30;
    
    private float offsetBarrilX = 20f;
    private float offsetBarrilY = 10f;
    
    private static final int TICKS_MIN_LANZAMIENTO = 120;
    private static final int TICKS_MAX_LANZAMIENTO = 240;
    
    public enum EstadoDK {
        REPOSO,
        AGARRANDO,
        LANZANDO,
        GOLPEANDO_PECHO,
        ENOJADO,
        AGARRANDO_PRINCESA
    }
    
    public DiegoKong(float x, float y, int scale, Handler handler) {
        super(x, y, ObjetosID.DiegoKong, WIDTH, HEIGHT, scale);
        this.handler = handler;
        this.estado = EstadoDK.REPOSO;
        
        cargarSprites();
        inicializarAnimaciones();
        
        System.out.println("[DIEGO KONG] Creado en (" + x + ", " + y + ")");
    }
    
    private void cargarSprites() {
        try {
            dkSprites = Mariotest.getTextura().getDiegoKongSprites();
            
            if (dkSprites != null && dkSprites.length > 0) {
                System.out.println("[DIEGO KONG] Sprites cargados: " + dkSprites.length);
            } else {
                System.err.println("[ERROR] Los sprites de Diego Kong no se cargaron.");
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudieron cargar sprites de Diego Kong: " + e.getMessage());
        }
    }
    
    /**
     * 🔥 FIX: Inicialización mejorada de animaciones
     */
    private void inicializarAnimaciones() {
        if (dkSprites == null || dkSprites.length < 8) {
            System.err.println("[ERROR] No hay suficientes sprites para DK.");
            return;
        }
        
        // Fila 1 (Índices 0-3): Animaciones normales
        dkReposo = new Animacion(15, dkSprites[0], dkSprites[1]);
        dkGolpeaPecho = new Animacion(8, dkSprites[2], dkSprites[3], dkSprites[2]);
        
        // Fila 2 (Índices 4-7): Agarrar barril y lanzar
        dkAgarra = new Animacion(8, dkSprites[5], dkSprites[5]);
        dkLanza = new Animacion(6, dkSprites[4], dkSprites[4], dkSprites[6], dkSprites[6]);
        
        // 🔥 FIX: ANIMACIÓN DE AGARRAR PRINCESA (6 frames, 48x40)
        BufferedImage[] spritesAgarrar = Mariotest.getTextura().getDKAgarraSprites();
        
        if (spritesAgarrar != null && spritesAgarrar.length == 6) {
            // Verificar que todos los frames son válidos
            boolean todosValidos = true;
            for (int i = 0; i < spritesAgarrar.length; i++) {
                if (spritesAgarrar[i] == null) {
                    System.err.println("[DK] ❌ Frame " + i + " es NULL");
                    todosValidos = false;
                    break;
                }
                
                // Verificar dimensiones
                if (spritesAgarrar[i].getWidth() != 48 || spritesAgarrar[i].getHeight() != 40) {
                    System.err.println("[DK] ⚠️ Frame " + i + " dimensiones incorrectas: " +
                        spritesAgarrar[i].getWidth() + "x" + spritesAgarrar[i].getHeight());
                }
            }
            
            if (todosValidos) {
                // 🔥 ANIMACIÓN CON 6 FRAMES (velocidad 8 = ~133ms por frame)
                dkAgarraPrincesa = new Animacion(8,
                    spritesAgarrar[0],
                    spritesAgarrar[1],
                    spritesAgarrar[2],
                    spritesAgarrar[3]
                );
                
                System.out.println("[DIEGO KONG] ✅ Animación agarrar princesa: 6 frames (48x40)");
            } else {
                // Fallback
                dkAgarraPrincesa = dkAgarra;
                System.err.println("[DIEGO KONG] ⚠️ Usando animación de agarrar como fallback");
            }
        } else {
            System.err.println("[DIEGO KONG] ❌ Array de sprites inválido: " + 
                (spritesAgarrar == null ? "NULL" : spritesAgarrar.length + " frames"));
            dkAgarraPrincesa = dkAgarra;
        }
        
        animacionActual = dkReposo;
        
        System.out.println("[DIEGO KONG] ✅ Animaciones inicializadas correctamente");
    }

    @Override
    public void tick() {
        // Ejecutar animación SIEMPRE
        if (animacionActual != null) {
            animacionActual.runAnimacion();
        }
        
        switch (estado) {
            case REPOSO:
                tickReposo();
                break;
            case AGARRANDO:
                tickAgarrando();
                break;
            case LANZANDO:
                tickLanzando();
                break;
            case GOLPEANDO_PECHO:
                tickGolpeandoPecho();
                break;
            case ENOJADO:
                tickEnojado();
                break;
            case AGARRANDO_PRINCESA:
                tickAgarrandoPrincesa();
                break;
        }
        
        verificarProximidadJugador();
    }
    
    private void tickReposo() {
        animacionActual = dkReposo;
        ticksDesdeUltimoLanzamiento++;
        
        if (ticksDesdeUltimoLanzamiento >= ticksEntrelanzamientos) {
            iniciarLanzamiento();
        }
    }
    
    private void tickAgarrando() {
        animacionActual = dkAgarra;
        ticksAnimacionLanzar++;
        
        if (ticksAnimacionLanzar >= 15) {
            estado = EstadoDK.LANZANDO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    private void tickLanzando() {
        animacionActual = dkLanza;
        ticksAnimacionLanzar++;
        
        if (ticksAnimacionLanzar >= DURACION_ANIMACION_LANZAR) {
            ticksAnimacionLanzar = 0;
            ticksDesdeUltimoLanzamiento = 0;
            
            if (Math.random() < 0.3) {
                estado = EstadoDK.GOLPEANDO_PECHO;
            } else {
                estado = EstadoDK.REPOSO;
            }
            
            ticksEntrelanzamientos = TICKS_MIN_LANZAMIENTO + 
                (int)(Math.random() * (TICKS_MAX_LANZAMIENTO - TICKS_MIN_LANZAMIENTO));
        }
    }
    
    private void tickGolpeandoPecho() {
        animacionActual = dkGolpeaPecho;
        ticksAnimacionLanzar++;
        
        if (ticksAnimacionLanzar >= 40) {
            estado = EstadoDK.REPOSO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    private void tickEnojado() {
        animacionActual = dkGolpeaPecho;
        ticksAnimacionLanzar++;
        
        if (ticksAnimacionLanzar >= 60) {
            iniciarLanzamiento();
            ticksAnimacionLanzar = 0;
        }
        
        if (!jugadorCerca()) {
            estado = EstadoDK.REPOSO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    /**
     * 🔥 FIX: Estado de agarrar princesa mejorado
     */
    private void tickAgarrandoPrincesa() {
        // ✅ Cambiar a la animación correcta
        animacionActual = dkAgarraPrincesa;
        ticksAnimacionLanzar++;
        
        // Debug cada 60 ticks
        if (ticksAnimacionLanzar % 60 == 0) {
            System.out.println("[DK] 🦍 Agarrando princesa... tick: " + ticksAnimacionLanzar +
                " | Animación: " + (animacionActual != null ? "OK" : "NULL"));
        }
    }
    
    private void iniciarLanzamiento() {
        estado = EstadoDK.AGARRANDO;
        ticksAnimacionLanzar = 0;
        System.out.println("[DIEGO KONG] Iniciando lanzamiento...");
    }
    
    private void verificarProximidadJugador() {
        Player player = handler.getPlayer();
        if (player == null) return;
        
        float distanciaX = Math.abs(player.getX() - getX());
        float distanciaY = Math.abs(player.getY() - getY());
        
        if (distanciaX < 200 && distanciaY < 100 && estado == EstadoDK.REPOSO) {
            if (Math.random() < 0.1) {
                estado = EstadoDK.ENOJADO;
                ticksAnimacionLanzar = 0;
            }
        }
        
        mirandoDerecha = player.getX() > getX();
    }
    
    private boolean jugadorCerca() {
        Player player = handler.getPlayer();
        if (player == null) return false;
        
        float distanciaX = Math.abs(player.getX() - getX());
        float distanciaY = Math.abs(player.getY() - getY());
        
        return distanciaX < 200 && distanciaY < 100;
    }
    
    /**
     * 🔥 Activa la animación de agarrar princesa
     */
    public void activarAnimacionAgarrar() {
        estado = EstadoDK.AGARRANDO_PRINCESA;
        ticksAnimacionLanzar = 0;
        animacionActual = dkAgarraPrincesa;
        System.out.println("[DIEGO KONG] 🦍 Activando animación de agarrar princesa");
    }
    
    public void volverAReposo() {
        estado = EstadoDK.REPOSO;
        ticksAnimacionLanzar = 0;
        animacionActual = dkReposo;
    }
    
    @Override
    public void aplicarGravedad() {
        // Diego Kong no tiene gravedad
    }

    /**
     * 🔥 FIX: Renderizado corregido
     */
    @Override
    public void render(Graphics g) {
        if (animacionActual != null) {
            int renderWidth = (int) getWidth();
            int renderHeight = (int) getHeight();
            int renderY = (int) getY();

            // 🔥 Si está agarrando princesa, usar altura de 40px
            if (estado == EstadoDK.AGARRANDO_PRINCESA) {
                renderHeight = (int) HEIGHT_AGARRA_PRINCESA;
                // Ajustar Y para que la base se mantenga
                renderY = (int) (getY() - (HEIGHT_AGARRA_PRINCESA - HEIGHT));
            }
            
            // Renderizar según dirección
            if (mirandoDerecha) {
                animacionActual.drawAnimacion(g, 
                    (int) getX(), renderY, 
                    renderWidth, renderHeight
                );
            } else {
                animacionActual.drawAnimacion(g, 
                    (int) (getX() + renderWidth), renderY, 
                    -renderWidth, renderHeight
                );
            }
            
        } else {
            // Placeholder
            g.setColor(new Color(89, 47, 20));
            g.fillRect((int) getX(), (int) getY(), 
                       (int) getWidth(), (int) getHeight());
            
            g.setColor(Color.YELLOW);
            g.drawString("DK", (int) getX() + 15, (int) getY() - 5);
        }
    }

    /**
     * 🔥 FIX: Hitbox ajustado para altura de 40px
     */
    @Override
    public Rectangle getBounds() {
        if (estado == EstadoDK.AGARRANDO_PRINCESA) {
            float boundsHeight = HEIGHT_AGARRA_PRINCESA;
            float boundsY = getY() - (HEIGHT_AGARRA_PRINCESA - HEIGHT);
            
            return new Rectangle(
                (int) getX(),
                (int) boundsY,
                (int) WIDTH,
                (int) boundsHeight
            );
        }
        
        return new Rectangle(
            (int) getX(),
            (int) getY(),
            (int) getWidth(),
            (int) getHeight()
        );
    }
    
    public void forzarLanzamiento() {
        if (estado == EstadoDK.REPOSO) {
            iniciarLanzamiento();
        }
    }
    
    public void setVelocidadLanzamiento(int ticksMin, int ticksMax) {
        if (ticksMin > 0 && ticksMax > ticksMin) {
            ticksEntrelanzamientos = ticksMin + 
                (int)(Math.random() * (ticksMax - ticksMin));
        }
    }
    
    public void activarModoEnojado() {
        if (estado == EstadoDK.REPOSO) {
            estado = EstadoDK.ENOJADO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    public EstadoDK getEstado() { return estado; }
    public boolean isMirandoDerecha() { return mirandoDerecha; }
    public void setMirandoDerecha(boolean mirandoDerecha) { this.mirandoDerecha = mirandoDerecha; }
    
    public int getTicksParaProximoLanzamiento() {
        return ticksEntrelanzamientos - ticksDesdeUltimoLanzamiento;
    }
}